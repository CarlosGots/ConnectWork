package com.connectwork.servlets;

import com.connectwork.dao.SolicitudDAO;
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

@WebServlet(name = "SolicitudServlet", urlPatterns = {"/solicitudes/*"})
public class SolicitudServlet extends HttpServlet {

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        String rol = (String) request.getAttribute("rol");
        String estado = request.getParameter("estado");

        try {
            if (path.equals("/categorias")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                enviarOk(response, gson.toJson(solicitudDAO.listarSolicitudesCategoria(estado)));

            } else if (path.equals("/habilidades")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                enviarOk(response, gson.toJson(solicitudDAO.listarSolicitudesHabilidad(estado)));

            } else if (path.equals("/todas")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                var todas = new java.util.ArrayList<>();
                todas.addAll(solicitudDAO.listarSolicitudesCategoria(estado));
                todas.addAll(solicitudDAO.listarSolicitudesHabilidad(estado));
                enviarOk(response, gson.toJson(todas));

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
        String path = request.getPathInfo();
        String rol = (String) request.getAttribute("rol");
        int idUsuario = (int) request.getAttribute("idUsuario");

        try {
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (path.equals("/categoria")) {
                if (!"CLIENTE".equals(rol)) { enviarError(response, 403, "Solo clientes pueden solicitar categorías"); return; }
                String nombre = payload.get("nombre").getAsString().trim();
                String desc = payload.has("descripcion") ? payload.get("descripcion").getAsString().trim() : "";
                int id = solicitudDAO.crearSolicitudCategoria(idUsuario, nombre, desc);
                if (id == -1) { enviarError(response, 500, "Error al crear solicitud"); return; }
                JsonObject r = new JsonObject();
                r.addProperty("mensaje", "Solicitud de categoría enviada");
                r.addProperty("idSolicitud", id);
                response.setStatus(201);
                enviarOk(response, gson.toJson(r));

            } else if (path.equals("/habilidad")) {
                if (!"FREELANCER".equals(rol)) { enviarError(response, 403, "Solo freelancers pueden solicitar habilidades"); return; }
                String nombre = payload.get("nombre").getAsString().trim();
                int idCategoria = payload.get("idCategoria").getAsInt();
                String desc = payload.has("descripcion") ? payload.get("descripcion").getAsString().trim() : "";
                int id = solicitudDAO.crearSolicitudHabilidad(idUsuario, idCategoria, nombre, desc);
                if (id == -1) { enviarError(response, 500, "Error al crear solicitud"); return; }
                JsonObject r = new JsonObject();
                r.addProperty("mensaje", "Solicitud de habilidad enviada");
                r.addProperty("idSolicitud", id);
                response.setStatus(201);
                enviarOk(response, gson.toJson(r));

            } else {
                enviarError(response, 404, "Endpoint no encontrado");
            }
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

        if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }

        try {
            boolean exito = false;
            if (path.endsWith("/aceptar-categoria")) {
                int id = Integer.parseInt(path.replace("/aceptar-categoria", "").replace("/", ""));
                exito = solicitudDAO.aceptarSolicitudCategoria(id);
            } else if (path.endsWith("/rechazar-categoria")) {
                int id = Integer.parseInt(path.replace("/rechazar-categoria", "").replace("/", ""));
                exito = solicitudDAO.rechazarSolicitudCategoria(id);
            } else if (path.endsWith("/aceptar-habilidad")) {
                int id = Integer.parseInt(path.replace("/aceptar-habilidad", "").replace("/", ""));
                exito = solicitudDAO.aceptarSolicitudHabilidad(id);
            } else if (path.endsWith("/rechazar-habilidad")) {
                int id = Integer.parseInt(path.replace("/rechazar-habilidad", "").replace("/", ""));
                exito = solicitudDAO.rechazarSolicitudHabilidad(id);
            } else {
                enviarError(response, 404, "Endpoint no encontrado"); return;
            }

            if (!exito) { enviarError(response, 400, "No se pudo procesar la solicitud"); return; }
            JsonObject r = new JsonObject();
            r.addProperty("mensaje", "Solicitud procesada exitosamente");
            enviarOk(response, gson.toJson(r));
        } catch (Exception e) {
            enviarError(response, 500, "Error: " + e.getMessage());
        }
    }

    private void enviarOk(HttpServletResponse response, String json) throws IOException {
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