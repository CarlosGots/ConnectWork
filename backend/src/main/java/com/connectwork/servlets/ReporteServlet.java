package com.connectwork.servlets;

import com.connectwork.dao.ReporteDAO;
import com.connectwork.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ReporteServlet", urlPatterns = {"/reportes/*"})
public class ReporteServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();
        String rol = (String) request.getAttribute("rol");
        int idUsuario = (int) request.getAttribute("idUsuario");
        String fi = request.getParameter("fechaInicio");
        String ff = request.getParameter("fechaFin");

        try {
            // ========== ADMIN ==========
            if (path.equals("/admin/historial-comisiones")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                enviarOk(response, gson.toJson(reporteDAO.historialComisiones()));

            } else if (path.equals("/admin/top-freelancers")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.topFreelancersIngresos(fi, ff)));

            } else if (path.equals("/admin/top-categorias")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.topCategoriasActividad(fi, ff)));

            } else if (path.equals("/admin/ingresos-plataforma")) {
                if (!"ADMINISTRADOR".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.ingresosPlataforma(fi, ff)));

            // ========== CLIENTE ==========
            } else if (path.equals("/cliente/historial-proyectos")) {
                if (!"CLIENTE".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.historialProyectosCliente(idUsuario, fi, ff)));

            } else if (path.equals("/cliente/historial-recargas")) {
                if (!"CLIENTE".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                enviarOk(response, gson.toJson(reporteDAO.historialRecargas(idUsuario)));

            } else if (path.equals("/cliente/gasto-categorias")) {
                if (!"CLIENTE".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.gastoPorCategoria(idUsuario, fi, ff)));

            // ========== FREELANCER ==========
            } else if (path.equals("/freelancer/saldo")) {
                if (!"FREELANCER".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                JsonObject r = new JsonObject();
                r.addProperty("saldo", reporteDAO.saldoFreelancer(idUsuario));
                enviarOk(response, gson.toJson(r));

            } else if (path.equals("/freelancer/historial-contratos")) {
                if (!"FREELANCER".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.historialContratosFreelancer(idUsuario, fi, ff)));

            } else if (path.equals("/freelancer/top-categorias")) {
                if (!"FREELANCER".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                enviarOk(response, gson.toJson(reporteDAO.topCategoriasFreelancer(idUsuario)));

            } else if (path.equals("/freelancer/propuestas")) {
                if (!"FREELANCER".equals(rol)) { enviarError(response, 403, "Sin permisos"); return; }
                if (fi == null || ff == null) { enviarError(response, 400, "Fechas requeridas"); return; }
                enviarOk(response, gson.toJson(reporteDAO.reportePropuestasFreelancer(idUsuario, fi, ff)));

            } else {
                enviarError(response, 404, "Reporte no encontrado");
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
}