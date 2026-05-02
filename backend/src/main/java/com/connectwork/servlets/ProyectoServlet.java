package com.connectwork.servlets;

import com.connectwork.dao.ProyectoDAO;
import com.connectwork.model.Proyecto;
import com.connectwork.util.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet para gestión de proyectos.
 *
 * Endpoints:
 *   GET  /proyectos              -> listar proyectos abiertos (freelancers)
 *   GET  /proyectos/mis          -> mis proyectos (cliente)
 *   GET  /proyectos/{id}         -> detalle de un proyecto
 *   POST /proyectos              -> publicar nuevo proyecto (cliente)
 *   PUT  /proyectos/{id}/cancelar -> cancelar proyecto (cliente)
 */
@WebServlet(name = "ProyectoServlet", urlPatterns = {"/proyectos/*"})
public class ProyectoServlet extends HttpServlet {

    private final ProyectoDAO proyectoDAO = new ProyectoDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        int idUsuario = (int) request.getAttribute("idUsuario");
        String rol = (String) request.getAttribute("rol");

        try {
            if (path == null || path.equals("/")) {
                // Listar proyectos abiertos (para freelancers y clientes)
                List<Proyecto> lista = proyectoDAO.listarAbiertos();
                enviarOk(response, gson.toJson(lista));

           } else if (path.equals("/mis")) {
    // Mis proyectos (solo clientes)
    if (!"CLIENTE".equals(rol)) {
        enviarError(response, 403, "Solo los clientes pueden ver sus proyectos");
        return;
    }
    System.out.println("=== DEBUG idUsuario: " + idUsuario);
    List<Proyecto> lista = proyectoDAO.listarPorCliente(idUsuario);
    System.out.println("=== DEBUG proyectos encontrados: " + lista.size());
    enviarOk(response, gson.toJson(lista));
} else {
    // Detalle de un proyecto por ID
    try {
        int idProyecto = Integer.parseInt(path.substring(1));
        Proyecto proyecto = proyectoDAO.obtenerPorId(idProyecto);
        if (proyecto == null) {
            enviarError(response, 404, "Proyecto no encontrado");
            return;
                    }
                    // Agregar habilidades al JSON
                    List<Integer> habilidades = proyectoDAO.obtenerHabilidades(idProyecto);
                    JsonObject pJson = gson.toJsonTree(proyecto).getAsJsonObject();
                    pJson.add("habilidades", gson.toJsonTree(habilidades));
                    enviarOk(response, gson.toJson(pJson));
                } catch (NumberFormatException e) {
                    enviarError(response, 400, "ID de proyecto inválido");
                }
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String rol = (String) request.getAttribute("rol");

        if (!"CLIENTE".equals(rol)) {
            enviarError(response, 403, "Solo los clientes pueden publicar proyectos");
            return;
        }

        int idCliente = (int) request.getAttribute("idUsuario");

        try {
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            // Validaciones
            if (!payload.has("titulo") || payload.get("titulo").getAsString().isBlank()) {
                enviarError(response, 400, "El título es obligatorio"); return;
            }
            if (!payload.has("descripcion") || payload.get("descripcion").getAsString().isBlank()) {
                enviarError(response, 400, "La descripción es obligatoria"); return;
            }
            if (!payload.has("idCategoria") || payload.get("idCategoria").getAsInt() <= 0) {
                enviarError(response, 400, "Debe seleccionar una categoría"); return;
            }
            if (!payload.has("presupuesto") || payload.get("presupuesto").getAsDouble() <= 0) {
                enviarError(response, 400, "El presupuesto debe ser mayor a 0"); return;
            }
            if (!payload.has("fechaLimite") || payload.get("fechaLimite").getAsString().isBlank()) {
                enviarError(response, 400, "La fecha límite es obligatoria"); return;
            }

            LocalDate fechaLimite = LocalDate.parse(payload.get("fechaLimite").getAsString());
            if (fechaLimite.isBefore(LocalDate.now().plusDays(1))) {
                enviarError(response, 400, "La fecha límite debe ser al menos mañana"); return;
            }

            Proyecto proyecto = new Proyecto();
            proyecto.setIdCliente(idCliente);
            proyecto.setIdCategoria(payload.get("idCategoria").getAsInt());
            proyecto.setTitulo(payload.get("titulo").getAsString().trim());
            proyecto.setDescripcion(payload.get("descripcion").getAsString().trim());
            proyecto.setPresupuesto(payload.get("presupuesto").getAsDouble());
            proyecto.setFechaLimite(fechaLimite);

            // Habilidades requeridas (opcional)
            List<Integer> habilidades = new ArrayList<>();
            if (payload.has("habilidades") && payload.get("habilidades").isJsonArray()) {
                JsonArray arr = payload.getAsJsonArray("habilidades");
                for (int i = 0; i < arr.size(); i++) {
                    habilidades.add(arr.get(i).getAsInt());
                }
            }

            int idGenerado = proyectoDAO.insertar(proyecto, habilidades);
            if (idGenerado == -1) {
                enviarError(response, 500, "No se pudo publicar el proyecto"); return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Proyecto publicado exitosamente");
            resp.addProperty("idProyecto", idGenerado);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error al publicar proyecto: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        if (path != null && path.endsWith("/cancelar")) {
            cancelarProyecto(request, response);
        } else {
            enviarError(response, 404, "Endpoint no encontrado");
        }
    }

    private void cancelarProyecto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String rol = (String) request.getAttribute("rol");
        if (!"CLIENTE".equals(rol)) {
            enviarError(response, 403, "Solo los clientes pueden cancelar proyectos");
            return;
        }

        try {
            String path = request.getPathInfo();
            String idStr = path.replace("/cancelar", "").replace("/", "");
            int idProyecto = Integer.parseInt(idStr);
            int idCliente = (int) request.getAttribute("idUsuario");

            boolean exito = proyectoDAO.cancelar(idProyecto, idCliente);
            if (!exito) {
                enviarError(response, 400, "No se pudo cancelar. El proyecto no existe, no es tuyo, o ya no está abierto.");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Proyecto cancelado exitosamente");
            enviarOk(response, gson.toJson(resp));

        } catch (Exception e) {
            enviarError(response, 500, "Error al cancelar: " + e.getMessage());
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private void enviarOk(HttpServletResponse response, String json) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        }
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", mensaje);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(error));
        }
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