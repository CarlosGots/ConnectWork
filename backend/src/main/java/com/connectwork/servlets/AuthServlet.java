package com.connectwork.servlets;

import com.connectwork.dao.UsuarioDAO;
import com.connectwork.model.Usuario;
import com.connectwork.util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.connectwork.util.GsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Servlet que gestiona la autenticación: registro de usuarios y login.
 * 
 * Endpoints disponibles:
 *   POST /auth/registro  -> registrar nuevo cliente o freelancer
 *   POST /auth/login     -> autenticar usuario y obtener token JWT
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
public class AuthServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        if (path == null) {
            enviarError(response, 404, "Endpoint no encontrado");
            return;
        }

        switch (path) {
            case "/registro" -> registrarUsuario(request, response);
            case "/login"    -> iniciarSesion(request, response);
            default          -> enviarError(response, 404, "Endpoint no encontrado: " + path);
        }
    }

    // ============================================================
    // REGISTRO
    // ============================================================
    private void registrarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            // Leer JSON del body
            String json = leerBody(request);
            Usuario usuario = gson.fromJson(json, Usuario.class);

            // Validaciones básicas
            if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
                enviarError(response, 400, "El username es obligatorio");
                return;
            }
            if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
                enviarError(response, 400, "La contraseña debe tener al menos 6 caracteres");
                return;
            }
            if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
                enviarError(response, 400, "El email no es válido");
                return;
            }
            if (usuario.getCui() == null || usuario.getCui().isBlank()) {
                enviarError(response, 400, "El CUI es obligatorio");
                return;
            }
            if (usuario.getRol() == null
                    || (!usuario.getRol().equals("CLIENTE") && !usuario.getRol().equals("FREELANCER"))) {
                enviarError(response, 400, "El rol debe ser CLIENTE o FREELANCER");
                return;
            }

            // Validar duplicados
            if (usuarioDAO.existeUsernameOEmailOCui(
                    usuario.getUsername(), usuario.getEmail(), usuario.getCui())) {
                enviarError(response, 409, "Ya existe un usuario con ese username, email o CUI");
                return;
            }

            // Establecer valores por defecto
            usuario.setSaldo(0.0);
            usuario.setActivo(true);
            usuario.setPrimeraVez(true);

            // Insertar
            int idGenerado = usuarioDAO.insertar(usuario);
            if (idGenerado == -1) {
                enviarError(response, 500, "Error al registrar el usuario");
                return;
            }

            // Respuesta exitosa
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("mensaje", "Usuario registrado exitosamente");
            respuesta.addProperty("idUsuario", idGenerado);
            respuesta.addProperty("username", usuario.getUsername());
            respuesta.addProperty("rol", usuario.getRol());

            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(respuesta));
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error en el registro: " + e.getMessage());
        }
    }

    // ============================================================
    // LOGIN
    // ============================================================
    private void iniciarSesion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            // Leer JSON del body
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (payload == null || !payload.has("username") || !payload.has("password")) {
                enviarError(response, 400, "Username y password son obligatorios");
                return;
            }

            String username = payload.get("username").getAsString();
            String password = payload.get("password").getAsString();

            // Validar credenciales
            Usuario usuario = usuarioDAO.validarCredenciales(username, password);
            if (usuario == null) {
                enviarError(response, 401, "Credenciales inválidas o cuenta desactivada");
                return;
            }

            // Generar token JWT
            String token = JWTUtil.generarToken(usuario);

            // Respuesta con datos del usuario y token
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("mensaje", "Login exitoso");
            respuesta.addProperty("token", token);
            respuesta.addProperty("idUsuario", usuario.getIdUsuario());
            respuesta.addProperty("username", usuario.getUsername());
            respuesta.addProperty("nombreCompleto", usuario.getNombreCompleto());
            respuesta.addProperty("rol", usuario.getRol());
            respuesta.addProperty("primeraVez", usuario.isPrimeraVez());

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(respuesta));
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error en el login: " + e.getMessage());
        }
    }

    // ============================================================
    // UTILIDADES PRIVADAS
    // ============================================================

    /** Lee el body de la petición como String. */
    private String leerBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea);
            }
        }
        return sb.toString();
    }

    /** Envía una respuesta de error en formato JSON. */
    private void enviarError(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("error", mensaje);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(error));
        }
    }
}