package com.connectwork.servlets;

import com.connectwork.dao.EntregaDAO;
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

/**
 * Endpoints:
 *   POST /entregas                    -> subir entrega (freelancer)
 *   PUT  /entregas/{id}/aprobar       -> aprobar entrega (cliente)
 *   PUT  /entregas/{id}/rechazar      -> rechazar entrega (cliente)
 */
@WebServlet(name = "EntregaServlet", urlPatterns = {"/entregas/*"})
public class EntregaServlet extends HttpServlet {

    private final EntregaDAO entregaDAO = new EntregaDAO();
    private final Gson gson = GsonUtil.getGson();

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    String path = request.getPathInfo();
    String rol = (String) request.getAttribute("rol");

    if (path != null && path.equals("/mis-contratos")) {
        if (!"FREELANCER".equals(rol)) {
            enviarError(response, 403, "Solo freelancers pueden ver sus contratos");
            return;
        }
        int idFreelancer = (int) request.getAttribute("idUsuario");
        var lista = entregaDAO.contratosActivosFreelancer(idFreelancer);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(lista));
        }
    } else if (path != null && path.startsWith("/contrato/")) {
        try {
            int idContrato = Integer.parseInt(path.replace("/contrato/", ""));
            var lista = entregaDAO.listarPorContrato(idContrato);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(lista));
            }
        } catch (NumberFormatException e) {
            enviarError(response, 400, "ID de contrato inválido");
        }
    } else if (path != null && path.equals("/mis-entregas")) {
        if (!"CLIENTE".equals(rol)) {
            enviarError(response, 403, "Solo clientes pueden ver entregas de sus proyectos");
            return;
        }
        int idClienteActual = (int) request.getAttribute("idUsuario");
        var lista = entregaDAO.entregasPendientesCliente(idClienteActual);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(lista));
        }
    } else {
        enviarError(response, 404, "Endpoint no encontrado");
    }
}

@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String rol = (String) request.getAttribute("rol");

        if (!"FREELANCER".equals(rol)) {
            enviarError(response, 403, "Solo freelancers pueden subir entregas");
            return;
        }

        int idFreelancer = (int) request.getAttribute("idUsuario");

        try {
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (!payload.has("descripcion") || payload.get("descripcion").getAsString().isBlank()) {
                enviarError(response, 400, "La descripción es obligatoria"); return;
            }

            String descripcion = payload.get("descripcion").getAsString().trim();
            String archivosUrl = payload.has("archivosUrl") ? payload.get("archivosUrl").getAsString() : "";

            // Obtener contrato activo del freelancer
            int idContrato = entregaDAO.obtenerIdContratoPorFreelancer(idFreelancer);
            if (idContrato == -1) {
                enviarError(response, 404, "No tienes contratos activos"); return;
            }

            int idGenerado = entregaDAO.subirEntrega(idContrato, descripcion, archivosUrl);
            if (idGenerado == -1) {
                enviarError(response, 500, "No se pudo subir la entrega"); return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Entrega subida exitosamente");
            resp.addProperty("idEntrega", idGenerado);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) { out.print(gson.toJson(resp)); }

        } catch (Exception e) {
            enviarError(response, 500, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        String rol = (String) request.getAttribute("rol");
        int idCliente = (int) request.getAttribute("idUsuario");

        try {
            if (path != null && path.endsWith("/aprobar")) {
                if (!"CLIENTE".equals(rol)) {
                    enviarError(response, 403, "Solo clientes pueden aprobar"); return;
                }
                int idEntrega = Integer.parseInt(path.replace("/aprobar", "").replace("/", ""));
                boolean exito = entregaDAO.aprobar(idEntrega, idCliente);
                if (!exito) { enviarError(response, 400, "No se pudo aprobar la entrega"); return; }

                JsonObject resp = new JsonObject();
                resp.addProperty("mensaje", "Entrega aprobada. Pago liberado al freelancer.");
                enviarOk(response, gson.toJson(resp));

            } else if (path != null && path.endsWith("/rechazar")) {
                if (!"CLIENTE".equals(rol)) {
                    enviarError(response, 403, "Solo clientes pueden rechazar"); return;
                }
                int idEntrega = Integer.parseInt(path.replace("/rechazar", "").replace("/", ""));
                String json = leerBody(request);
                JsonObject payload = gson.fromJson(json, JsonObject.class);
                String motivo = payload.has("motivo") ? payload.get("motivo").getAsString() : "Sin motivo especificado";

                boolean exito = entregaDAO.rechazar(idEntrega, idCliente, motivo);
                if (!exito) { enviarError(response, 400, "No se pudo rechazar la entrega"); return; }

                JsonObject resp = new JsonObject();
                resp.addProperty("mensaje", "Entrega rechazada. El freelancer puede subir una nueva.");
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