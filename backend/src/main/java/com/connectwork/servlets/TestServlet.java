package com.connectwork.servlets;

import com.connectwork.util.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet de prueba para verificar que la conexión a la base de datos funciona.
 * Acceso: http://localhost:8080/connectwork-backend/test
 */
@WebServlet(name = "TestServlet", urlPatterns = {"/test"})
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>ConnectWork - Test</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; padding: 40px; max-width: 600px; margin: 0 auto; }");
            out.println("h1 { color: #2E4057; }");
            out.println(".success { color: #2e7d32; font-size: 18px; padding: 20px; background: #e8f5e9; border-radius: 8px; }");
            out.println(".error { color: #c62828; font-size: 18px; padding: 20px; background: #ffebee; border-radius: 8px; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>ConnectWork Backend</h1>");
            out.println("<p>Servidor Tomcat funcionando correctamente.</p>");

            if (ConexionBD.probarConexion()) {
                out.println("<div class='success'>");
                out.println("✓ Conexión a MySQL <strong>exitosa</strong><br>");
                out.println("Base de datos: connectwork");
                out.println("</div>");
            } else {
                out.println("<div class='error'>");
                out.println("✗ Error al conectar a MySQL<br>");
                out.println("Verifica que MySQL esté corriendo y que el password sea correcto en ConexionBD.java");
                out.println("</div>");
            }

            out.println("</body>");
            out.println("</html>");
        }
    }
}
