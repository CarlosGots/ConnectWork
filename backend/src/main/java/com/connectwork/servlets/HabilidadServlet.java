package com.connectwork.servlets;

import com.connectwork.dao.HabilidadDAO;
import com.connectwork.model.Habilidad;
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
 * Servlet que gestiona el CRUD de habilidades.
 *
 * Endpoints:
 *   GET    /habilidades                    -> listar todas con su categoría
 *   GET    /habilidades/categoria/{id}     -> listar habilidades activas de una categoría
 *   POST   /habilidades                    -> crear nueva (solo admin)
 *   PUT    /habilidades/{id}               -> actualizar (solo admin)
 *   PUT    /habilidades/{id}/estado        -> activar/desactivar (solo admin)
 */
@WebServlet(name = "HabilidadServlet", urlPatterns = {"/habilidades/*"})
public class HabilidadServlet extends HttpServlet {

    private final HabilidadDAO habilidadDAO = new HabilidadDAO();
    private final Gson gson = GsonUtil.getGson();

    // ============================================================
    // GET
    // ============================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        try {
            List<Habilidad> lista;

            if (path == null || path.equals("/")) {
                lista = habilidadDAO.listarTodas();
            } else if (path.startsWith("/categoria/")) {
                int idCategoria = Integer.parseInt(path.substring("/categoria/".length()));
                lista = habilidadDAO.listarPorCategoria(idCategoria);
            } else {
                enviarError(response, 404, "Endpoint no encontrado");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(lista));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al listar habilidades: " + e.getMessage());
        }
    }

    // ============================================================
    // POST
    // ============================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        if (!esAdministrador(request)) {
            enviarError(response, 403, "Solo el administrador puede crear habilidades");
            return;
        }

        try {
            String json = leerBody(request);
            Habilidad habilidad = gson.fromJson(json, Habilidad.class);

            if (habilidad == null || habilidad.getNombre() == null || habilidad.getNombre().isBlank()) {
                enviarError(response, 400, "El nombre es obligatorio");
                return;
            }
            if (habilidad.getIdCategoria() <= 0) {
                enviarError(response, 400, "Debe seleccionar una categoría");
                return;
            }

            if (habilidadDAO.existeNombreEnCategoria(habilidad.getNombre(), habilidad.getIdCategoria(), 0)) {
                enviarError(response, 409, "Ya existe una habilidad con ese nombre en esa categoría");
                return;
            }

            habilidad.setActiva(true);
            int idGenerado = habilidadDAO.insertar(habilidad);

            if (idGenerado == -1) {
                enviarError(response, 500, "No se pudo crear la habilidad");
                return;
            }

            habilidad.setIdHabilidad(idGenerado);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(habilidad));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al crear habilidad: " + e.getMessage());
        }
    }

    // ============================================================
    // PUT — Actualizar O cambiar estado (según la URL)
    // ============================================================
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        if (!esAdministrador(request)) {
            enviarError(response, 403, "Solo el administrador puede modificar habilidades");
            return;
        }

        String path = request.getPathInfo();

        // Si la URL termina en "/estado", es un cambio de estado
        if (path != null && path.endsWith("/estado")) {
            cambiarEstado(request, response);
            return;
        }

        try {
            int idHabilidad = extraerIdDesdePath(request);
            if (idHabilidad == -1) {
                enviarError(response, 400, "ID de habilidad inválido");
                return;
            }

            String json = leerBody(request);
            Habilidad habilidad = gson.fromJson(json, Habilidad.class);

            if (habilidad.getNombre() == null || habilidad.getNombre().isBlank()) {
                enviarError(response, 400, "El nombre es obligatorio");
                return;
            }
            if (habilidad.getIdCategoria() <= 0) {
                enviarError(response, 400, "Debe seleccionar una categoría");
                return;
            }

            if (habilidadDAO.existeNombreEnCategoria(habilidad.getNombre(), habilidad.getIdCategoria(), idHabilidad)) {
                enviarError(response, 409, "Ya existe otra habilidad con ese nombre en esa categoría");
                return;
            }

            habilidad.setIdHabilidad(idHabilidad);
            boolean exito = habilidadDAO.actualizar(habilidad);

            if (!exito) {
                enviarError(response, 500, "No se pudo actualizar la habilidad");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Habilidad actualizada exitosamente");
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al actualizar habilidad: " + e.getMessage());
        }
    }

    /**
     * Cambia el estado (activa/inactiva) de una habilidad.
     */
    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String path = request.getPathInfo();
            String idStr = path.replace("/estado", "").replace("/", "");
            int idHabilidad = Integer.parseInt(idStr);

            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);
            boolean activa = payload.get("activa").getAsBoolean();

            boolean exito = habilidadDAO.cambiarEstado(idHabilidad, activa);
            if (!exito) {
                enviarError(response, 500, "No se pudo cambiar el estado");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", activa ? "Habilidad activada" : "Habilidad desactivada");
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al cambiar estado: " + e.getMessage());
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private boolean esAdministrador(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        return "ADMINISTRADOR".equals(rol);
    }

    private int extraerIdDesdePath(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.length() <= 1) return -1;
        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException e) {
            return -1;
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

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", mensaje);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(error));
        }
    }
}