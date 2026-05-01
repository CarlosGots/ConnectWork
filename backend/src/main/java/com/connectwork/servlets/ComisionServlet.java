package com.connectwork.servlets;

import com.connectwork.dao.ComisionDAO;
import com.connectwork.model.Comision;
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
 * Servlet para gestión de la comisión de la plataforma.
 *
 * Endpoints:
 *   GET  /admin/comision           -> obtener comisión vigente
 *   GET  /admin/comision/historial -> listar todas las comisiones (vigente + pasadas)
 *   PUT  /admin/comision           -> cambiar comisión (solo admin)
 */
@WebServlet(name = "ComisionServlet", urlPatterns = {"/admin/comision/*", "/admin/comision"})
public class ComisionServlet extends HttpServlet {

    private final ComisionDAO comisionDAO = new ComisionDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        try {
            if (path != null && path.equals("/historial")) {
                List<Comision> historial = comisionDAO.listarHistorial();
                response.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(historial));
                }
            } else {
                Comision activa = comisionDAO.obtenerActiva();
                if (activa == null) {
                    enviarError(response, 404, "No hay comisión activa configurada");
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(activa));
                }
            }
        } catch (Exception e) {
            enviarError(response, 500, "Error al consultar comisión: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        if (!esAdministrador(request)) {
            enviarError(response, 403, "Solo el administrador puede cambiar la comisión");
            return;
        }

        try {
            Integer idAdmin = (Integer) request.getAttribute("idUsuario");
            if (idAdmin == null) {
                enviarError(response, 401, "Token inválido");
                return;
            }

            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (payload == null || !payload.has("porcentaje")) {
                enviarError(response, 400, "Falta el campo 'porcentaje'");
                return;
            }

            double porcentaje = payload.get("porcentaje").getAsDouble();

            if (porcentaje < 0 || porcentaje > 100) {
                enviarError(response, 400, "El porcentaje debe estar entre 0 y 100");
                return;
            }

            Comision actual = comisionDAO.obtenerActiva();
            if (actual != null && actual.getPorcentaje() == porcentaje) {
                enviarError(response, 400, "El porcentaje es el mismo que el actual");
                return;
            }

            boolean exito = comisionDAO.cambiarComision(porcentaje, idAdmin);
            if (!exito) {
                enviarError(response, 500, "No se pudo cambiar la comisión");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Comisión actualizada exitosamente");
            resp.addProperty("nuevoPorcentaje", porcentaje);
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error al cambiar comisión: " + e.getMessage());
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private boolean esAdministrador(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        return "ADMINISTRADOR".equals(rol);
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