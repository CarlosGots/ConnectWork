package com.connectwork.servlets;

import com.connectwork.dao.CategoriaDAO;
import com.connectwork.model.Categoria;
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
 * Servlet que gestiona el CRUD de categorías.
 *
 * Endpoints:
 *   GET    /categorias              -> listar todas (con conteo de habilidades)
 *   GET    /categorias/activas      -> listar solo activas
 *   POST   /categorias              -> crear nueva (solo admin)
 *   PUT    /categorias/{id}         -> actualizar (solo admin)
 *   PUT    /categorias/{id}/estado  -> activar/desactivar (solo admin)
 */
@WebServlet(name = "CategoriaServlet", urlPatterns = {"/categorias/*"})
public class CategoriaServlet extends HttpServlet {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final Gson gson = GsonUtil.getGson();

    // ============================================================
    // GET — Listar
    // ============================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        try {
            List<Categoria> lista;
            if (path == null || path.equals("/")) {
                lista = categoriaDAO.listarTodas();
            } else if (path.equals("/activas")) {
                lista = categoriaDAO.listarActivas();
            } else {
                enviarError(response, 404, "Endpoint no encontrado");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(lista));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al listar categorías: " + e.getMessage());
        }
    }

    // ============================================================
    // POST — Crear
    // ============================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        if (!esAdministrador(request)) {
            enviarError(response, 403, "Solo el administrador puede crear categorías");
            return;
        }

        try {
            String json = leerBody(request);
            Categoria categoria = gson.fromJson(json, Categoria.class);

            if (categoria == null || categoria.getNombre() == null || categoria.getNombre().isBlank()) {
                enviarError(response, 400, "El nombre es obligatorio");
                return;
            }
            if (categoria.getDescripcion() == null) {
                categoria.setDescripcion("");
            }

            if (categoriaDAO.existeNombre(categoria.getNombre(), 0)) {
                enviarError(response, 409, "Ya existe una categoría con ese nombre");
                return;
            }

            categoria.setActiva(true);
            int idGenerado = categoriaDAO.insertar(categoria);

            if (idGenerado == -1) {
                enviarError(response, 500, "No se pudo crear la categoría");
                return;
            }

            categoria.setIdCategoria(idGenerado);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(categoria));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al crear categoría: " + e.getMessage());
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
            enviarError(response, 403, "Solo el administrador puede modificar categorías");
            return;
        }

        String path = request.getPathInfo();

        // Si la URL termina en "/estado", es un cambio de estado
        if (path != null && path.endsWith("/estado")) {
            cambiarEstado(request, response);
            return;
        }

        // Si no, es una actualización completa
        try {
            int idCategoria = extraerIdDesdePath(request);
            if (idCategoria == -1) {
                enviarError(response, 400, "ID de categoría inválido");
                return;
            }

            String json = leerBody(request);
            Categoria categoria = gson.fromJson(json, Categoria.class);

            if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
                enviarError(response, 400, "El nombre es obligatorio");
                return;
            }
            if (categoria.getDescripcion() == null) {
                categoria.setDescripcion("");
            }

            if (categoriaDAO.existeNombre(categoria.getNombre(), idCategoria)) {
                enviarError(response, 409, "Ya existe otra categoría con ese nombre");
                return;
            }

            categoria.setIdCategoria(idCategoria);
            boolean exito = categoriaDAO.actualizar(categoria);

            if (!exito) {
                enviarError(response, 500, "No se pudo actualizar la categoría");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Categoría actualizada exitosamente");
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al actualizar categoría: " + e.getMessage());
        }
    }

    /**
     * Cambia el estado (activa/inactiva) de una categoría.
     */
    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String path = request.getPathInfo();
            String idStr = path.replace("/estado", "").replace("/", "");
            int idCategoria = Integer.parseInt(idStr);

            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);
            boolean activa = payload.get("activa").getAsBoolean();

            boolean exito = categoriaDAO.cambiarEstado(idCategoria, activa);
            if (!exito) {
                enviarError(response, 500, "No se pudo cambiar el estado");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", activa ? "Categoría activada" : "Categoría desactivada");
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