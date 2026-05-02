package com.connectwork.servlets;

import com.connectwork.dao.RecargaDAO;
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
 * Servlet para gestión de saldos y recargas.
 *
 * Endpoints:
 *   GET  /saldos/mi-saldo   -> consultar saldo actual del usuario
 *   POST /saldos/recargar   -> recargar saldo (solo clientes)
 */
@WebServlet(name = "SaldoServlet", urlPatterns = {"/saldos/*"})
public class SaldoServlet extends HttpServlet {

    private final RecargaDAO recargaDAO = new RecargaDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        int idUsuario = (int) request.getAttribute("idUsuario");

        if ("/mi-saldo".equals(path)) {
            double saldo = recargaDAO.obtenerSaldo(idUsuario);
            JsonObject resp = new JsonObject();
            resp.addProperty("saldo", saldo);
            enviarOk(response, gson.toJson(resp));
        } else {
            enviarError(response, 404, "Endpoint no encontrado");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String rol = (String) request.getAttribute("rol");
        int idUsuario = (int) request.getAttribute("idUsuario");

        if (!"CLIENTE".equals(rol)) {
            enviarError(response, 403, "Solo los clientes pueden recargar saldo");
            return;
        }

        String path = request.getPathInfo();
        if (!"/recargar".equals(path)) {
            enviarError(response, 404, "Endpoint no encontrado");
            return;
        }

        try {
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (!payload.has("monto")) {
                enviarError(response, 400, "El monto es obligatorio");
                return;
            }

            double monto = payload.get("monto").getAsDouble();
            if (monto <= 0) {
                enviarError(response, 400, "El monto debe ser mayor a Q0");
                return;
            }
            if (monto > 50000) {
                enviarError(response, 400, "El monto máximo por recarga es Q50,000");
                return;
            }

            boolean exito = recargaDAO.recargar(idUsuario, monto);
            if (!exito) {
                enviarError(response, 500, "No se pudo procesar la recarga");
                return;
            }

            double nuevoSaldo = recargaDAO.obtenerSaldo(idUsuario);
            JsonObject resp = new JsonObject();
            resp.addProperty("mensaje", "Recarga exitosa");
            resp.addProperty("montoRecargado", monto);
            resp.addProperty("saldoActual", nuevoSaldo);
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(resp));
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error al procesar la recarga: " + e.getMessage());
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