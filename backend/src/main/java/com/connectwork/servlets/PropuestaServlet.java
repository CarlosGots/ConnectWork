package com.connectwork.servlets;

import com.connectwork.dao.ComisionDAO;
import com.connectwork.dao.PropuestaDAO;
import com.connectwork.model.Comision;
import com.connectwork.model.Propuesta;
import com.connectwork.util.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet para gestión de propuestas.
 *
 * Endpoints:
 *   GET  /propuestas/proyecto/{id}   -> propuestas de un proyecto (cliente)
 *   GET  /propuestas/mis             -> mis propuestas (freelancer)
 *   POST /propuestas                 -> enviar propuesta (freelancer)
 *   PUT  /propuestas/{id}/aceptar    -> aceptar propuesta (cliente)
 *   PUT  /propuestas/{id}/rechazar   -> rechazar propuesta (cliente)
 */
@WebServlet(name = "PropuestaServlet", urlPatterns = {"/propuestas/*"})
public class PropuestaServlet extends HttpServlet {

    private final PropuestaDAO propuestaDAO = new PropuestaDAO();
    private final ComisionDAO comisionDAO = new ComisionDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        int idUsuario = (int) request.getAttribute("idUsuario");
        String rol = (String) request.getAttribute("rol");

        try {
            if (path != null && path.startsWith("/proyecto/")) {
                // Propuestas de un proyecto (cliente)
                if (!"CLIENTE".equals(rol)) {
                    enviarError(response, 403, "Solo clientes pueden ver propuestas de sus proyectos");
                    return;
                }
                int idProyecto = Integer.parseInt(path.substring("/proyecto/".length()));
                List<Propuesta> lista = propuestaDAO.listarPorProyecto(idProyecto);
                enviarOk(response, gson.toJson(lista));

            } else if (path != null && path.equals("/mis")) {
                // Mis propuestas (freelancer)
                if (!"FREELANCER".equals(rol)) {
                    enviarError(response, 403, "Solo freelancers pueden ver sus propuestas");
                    return;
                }
                List<Propuesta> lista = propuestaDAO.listarPorFreelancer(idUsuario);
                enviarOk(response, gson.toJson(lista));

            } else {
                enviarError(response, 404, "Endpoint no encontrado");
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String rol = (String) request.getAttribute("rol");

        if (!"FREELANCER".equals(rol)) {
            enviarError(response, 403, "Solo freelancers pueden enviar propuestas");
            return;
        }

        int idFreelancer = (int) request.getAttribute("idUsuario");

        try {
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            // Validaciones
            if (!payload.has("idProyecto") || payload.get("idProyecto").getAsInt() <= 0) {
                enviarError(response, 400, "ID de proyecto inválido"); return;
            }
            if (!payload.has("montoOfertado") || payload.get("montoOfertado").getAsDouble() <= 0) {
                enviarError(response, 400, "El monto ofertado debe ser mayor a 0"); return;
            }
            if (!payload.has("plazoDias") || payload.get("plazoDias").getAsInt() <= 0) {
                enviarError(response, 400, "El plazo debe ser mayor a 0 días"); return;
            }
            if (!payload.has("cartaPresentacion") || payload.get("cartaPresentacion").getAsString().isBlank()) {
                enviarError(response, 400, "La carta de presentación es obligatoria"); return;
            }

            int idProyecto = payload.get("idProyecto").getAsInt();

            // Verificar que no haya enviado propuesta antes
            if (propuestaDAO.yaEnvioPropuesta(idProyecto, idFreelancer)) {
                enviarError(response, 409, "Ya enviaste una propuesta a este proyecto"); return;
            }

            Propuesta propuesta = new Propuesta();
            propuesta.setIdProyecto(idProyecto);
            propuesta.setIdFreelancer(idFreelancer);
            propuesta.setMontoOfertado(payload.get("montoOfertado").getAsDouble());
            propuesta.setPlazoDias(payload.get("plazoDias").getAsInt());
            propuesta.setCartaPresentacion(payload.get("cartaPresentacion").getAsString().trim());

            int idGenerado = propuestaDAO.insertar(propuesta);
            if (idGenerado == -1) {
                enviarError(response, 500, "No se pudo enviar la propuesta"); return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Propuesta enviada exitosamente");
            resp.addProperty("idPropuesta", idGenerado);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error al enviar propuesta: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        String rol = (String) request.getAttribute("rol");
        int idUsuario = (int) request.getAttribute("idUsuario");

        try {
            if (path != null && path.endsWith("/aceptar")) {
                if (!"CLIENTE".equals(rol)) {
                    enviarError(response, 403, "Solo clientes pueden aceptar propuestas"); return;
                }
                String idStr = path.replace("/aceptar", "").replace("/", "");
                int idPropuesta = Integer.parseInt(idStr);

                // Obtener comisión vigente
                Comision comision = comisionDAO.obtenerActiva();
                double porcentaje = comision != null ? comision.getPorcentaje() : 10.0;

                boolean exito = propuestaDAO.aceptar(idPropuesta, idUsuario, porcentaje);
                if (!exito) {
                    enviarError(response, 400, "No se pudo aceptar. Verifica que tengas saldo suficiente.");
                    return;
                }

                JsonObject resp = new JsonObject();
                resp.addProperty("mensaje", "Propuesta aceptada. Contrato generado exitosamente.");
                enviarOk(response, gson.toJson(resp));

            } else if (path != null && path.endsWith("/rechazar")) {
                if (!"CLIENTE".equals(rol)) {
                    enviarError(response, 403, "Solo clientes pueden rechazar propuestas"); return;
                }
                String idStr = path.replace("/rechazar", "").replace("/", "");
                int idPropuesta = Integer.parseInt(idStr);

                boolean exito = propuestaDAO.rechazar(idPropuesta, idUsuario);
                if (!exito) {
                    enviarError(response, 400, "No se pudo rechazar la propuesta."); return;
                }

                JsonObject resp = new JsonObject();
                resp.addProperty("mensaje", "Propuesta rechazada.");
                enviarOk(response, gson.toJson(resp));

            } else {
                enviarError(response, 404, "Endpoint no encontrado");
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error: " + e.getMessage());
        }
    }

    private void enviarOk(HttpServletResponse response, String json) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) { out.print(json); }
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", mensaje);
        try (PrintWriter out = response.getWriter()) { out.print(gson.toJson(error)); }
    }

    private String leerBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) sb.append(linea);
        }
        return sb.toString();
    }
}