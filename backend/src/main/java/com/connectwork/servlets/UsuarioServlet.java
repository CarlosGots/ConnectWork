package com.connectwork.servlets;

import com.connectwork.dao.ClienteDAO;
import com.connectwork.dao.FreelancerDAO;
import com.connectwork.util.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import com.connectwork.dao.UsuarioDAO;
import com.connectwork.model.Usuario;
import java.util.List;
/**
 * Servlet que gestiona operaciones del usuario autenticado.
 * Endpoints:
 *   PUT /usuarios/info  -> completar/actualizar info inicial según el rol
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuarios/*"})
public class UsuarioServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final FreelancerDAO freelancerDAO = new FreelancerDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = GsonUtil.getGson();

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json;charset=UTF-8");

    String rol = (String) request.getAttribute("rol");
    if (!"ADMINISTRADOR".equals(rol)) {
        enviarError(response, 403, "Solo el administrador puede listar usuarios");
        return;
    }

    try {
        List<Usuario> lista = usuarioDAO.listarTodos();
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(lista));
        }
    } catch (Exception e) {
        enviarError(response, 500, "Error al listar usuarios: " + e.getMessage());
    }
}
    
   @Override
protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json;charset=UTF-8");
    String path = request.getPathInfo();

    if ("/info".equals(path)) {
        completarInfo(request, response);
    } else if (path != null && path.endsWith("/estado")) {
        cambiarEstadoUsuario(request, response);
    } else {
        enviarError(response, 404, "Endpoint no encontrado: " + path);
    }
}

    /**
     * Recibe los datos iniciales del usuario y los guarda según su rol.
     * El ID del usuario se obtiene del token JWT (lo deja JWTFilter en el request).
     */
    private void completarInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            // Obtener identidad desde el token (la dejó JWTFilter)
            Integer idUsuario = (Integer) request.getAttribute("idUsuario");
            String rol = (String) request.getAttribute("rol");

            if (idUsuario == null || rol == null) {
                enviarError(response, 401, "Token inválido");
                return;
            }

            // Leer body
            String json = leerBody(request);
            JsonObject payload = gson.fromJson(json, JsonObject.class);

            if (payload == null) {
                enviarError(response, 400, "Body vacío o malformado");
                return;
            }

            boolean exito = switch (rol) {
                case "CLIENTE"    -> guardarInfoCliente(idUsuario, payload, response);
                case "FREELANCER" -> guardarInfoFreelancer(idUsuario, payload, response);
                default           -> {
                    enviarError(response, 403, "Solo clientes y freelancers pueden completar info");
                    yield false;
                }
            };

            if (exito) {
                JsonObject resp = new JsonObject();
                resp.addProperty("mensaje", "Información guardada exitosamente");
                resp.addProperty("primeraVez", false);
                response.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(resp));
                }
            }

        } catch (Exception e) {
            enviarError(response, 500, "Error al guardar la información: " + e.getMessage());
        }
    }

    private boolean guardarInfoCliente(int idUsuario, JsonObject payload, HttpServletResponse response)
            throws IOException {

        if (!payload.has("descripcionEmpresa") || payload.get("descripcionEmpresa").getAsString().isBlank()) {
            enviarError(response, 400, "La descripción de la empresa es obligatoria");
            return false;
        }
        if (!payload.has("sector") || payload.get("sector").getAsString().isBlank()) {
            enviarError(response, 400, "El sector es obligatorio");
            return false;
        }

        String descripcion = payload.get("descripcionEmpresa").getAsString();
        String sector = payload.get("sector").getAsString();
        String sitioWeb = payload.has("sitioWeb") && !payload.get("sitioWeb").isJsonNull()
                          ? payload.get("sitioWeb").getAsString() : null;

        boolean exito = clienteDAO.insertarInfoInicial(idUsuario, descripcion, sector, sitioWeb);
        if (!exito) {
            enviarError(response, 500, "No se pudo guardar la información del cliente");
            return false;
        }
        return true;
    }

    private boolean guardarInfoFreelancer(int idUsuario, JsonObject payload, HttpServletResponse response)
            throws IOException {

        if (!payload.has("biografia") || payload.get("biografia").getAsString().isBlank()) {
            enviarError(response, 400, "La biografía es obligatoria");
            return false;
        }
        if (!payload.has("nivelExperiencia") || payload.get("nivelExperiencia").getAsString().isBlank()) {
            enviarError(response, 400, "El nivel de experiencia es obligatorio");
            return false;
        }
        if (!payload.has("tarifaHora") || payload.get("tarifaHora").getAsDouble() <= 0) {
            enviarError(response, 400, "La tarifa por hora debe ser mayor a 0");
            return false;
        }
        if (!payload.has("habilidades") || !payload.get("habilidades").isJsonArray()) {
            enviarError(response, 400, "Debes seleccionar al menos una habilidad");
            return false;
        }

        String biografia = payload.get("biografia").getAsString();
        String nivel = payload.get("nivelExperiencia").getAsString();
        double tarifa = payload.get("tarifaHora").getAsDouble();

        JsonArray arrHabilidades = payload.getAsJsonArray("habilidades");
        if (arrHabilidades.size() == 0) {
            enviarError(response, 400, "Debes seleccionar al menos una habilidad");
            return false;
        }

        List<Integer> idsHabilidades = new ArrayList<>();
        for (int i = 0; i < arrHabilidades.size(); i++) {
            idsHabilidades.add(arrHabilidades.get(i).getAsInt());
        }

        boolean exito = freelancerDAO.insertarInfoInicial(idUsuario, biografia, nivel, tarifa, idsHabilidades);
        if (!exito) {
            enviarError(response, 500, "No se pudo guardar la información del freelancer");
            return false;
        }
        return true;
    }

    private String leerBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) sb.append(linea);
        }
        return sb.toString();
    }
    
    /**
 * Activa o desactiva un usuario. Solo admin.
 * PUT /usuarios/{id}/estado
 */
private void cambiarEstadoUsuario(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    String rolSolicitante = (String) request.getAttribute("rol");
    if (!"ADMINISTRADOR".equals(rolSolicitante)) {
        enviarError(response, 403, "Solo el administrador puede cambiar el estado de usuarios");
        return;
    }

    try {
        String path = request.getPathInfo();
        String idStr = path.replace("/estado", "").replace("/", "");
        int idUsuario = Integer.parseInt(idStr);

        String json = leerBody(request);
        JsonObject payload = gson.fromJson(json, JsonObject.class);
        boolean activo = payload.get("activo").getAsBoolean();

        boolean exito = usuarioDAO.cambiarEstado(idUsuario, activo);
        if (!exito) {
            enviarError(response, 500, "No se pudo cambiar el estado del usuario");
            return;
        }

        JsonObject resp = new JsonObject();
        resp.addProperty("mensaje", activo ? "Usuario activado" : "Usuario desactivado");
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(resp));
        }
    } catch (Exception e) {
        enviarError(response, 500, "Error al cambiar estado: " + e.getMessage());
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
}
