package com.connectwork.servlets;

import com.connectwork.dao.CalificacionDAO;
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
 * POST /calificaciones              -> calificar freelancer (cliente)
 * GET  /calificaciones/pendientes   -> contratos sin calificar (cliente)
 */
@WebServlet(name = "CalificacionServlet", urlPatterns = {"/calificaciones/*"})
public class CalificacionServlet extends HttpServlet {

    private final CalificacionDAO calificacionDAO = new CalificacionDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        String rol = (String) request.getAttribute("rol");

        if (!"CLIENTE".equals(rol)) {
            enviarError(response, 403, "Solo clientes pueden ver calificaciones pendientes");
            return;
        }

        if (path != null && path.equals("/pendientes")) {
            int idCliente = (int) request.getAttribute("idUsuario");
            var lista = calificacionDAO.contratosParaCalificar(idCliente);
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

        if (!"CLIENTE".equals(rol)) {
            enviarError(response, 403, "Solo clientes pueden calificar");
            return;
        }

        int idCliente = (int) request.getAttribute("idUsuario");

        try {
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (!payload.has("idContrato") || !payload.has("estrellas")) {
                enviarError(response, 400, "idContrato y estrellas son obligatorios");
                return;
            }

            int idContrato = payload.get("idContrato").getAsInt();
            int estrellas = payload.get("estrellas").getAsInt();
            String comentario = payload.has("comentario") ? payload.get("comentario").getAsString().trim() : "";

            if (estrellas < 1 || estrellas > 5) {
                enviarError(response, 400, "Las estrellas deben ser entre 1 y 5");
                return;
            }

            int resultado = calificacionDAO.insertar(idContrato, idCliente, estrellas, comentario);

            if (resultado == -2) {
                enviarError(response, 400, "Este contrato ya fue calificado");
                return;
            }
            if (resultado == -1) {
                enviarError(response, 400, "No se pudo calificar. Verifica que el contrato esté completado.");
                return;
            }

            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Calificación registrada exitosamente");
            resp.addProperty("idCalificacion", resultado);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) { out.print(gson.toJson(resp)); }

        } catch (Exception e) {
            enviarError(response, 500, "Error: " + e.getMessage());
        }
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