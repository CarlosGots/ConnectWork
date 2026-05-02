package com.connectwork.dao;

import com.connectwork.model.Usuario;
import com.connectwork.util.ConexionBD;
import com.connectwork.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/**
 * Acceso a datos para la entidad Usuario.
 * Contiene los métodos necesarios para registro, login y consultas básicas.
 */
public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * Encripta la contraseña antes de almacenarla.
     * @return el ID generado, o -1 si falló
     */
    public int insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_completo, username, password, email, telefono, direccion, cui, fecha_nacimiento, rol, saldo, activo, primera_vez) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Encriptar la contraseña antes de guardarla
            String passwordEncriptada = PasswordUtil.encriptar(usuario.getPassword());

            ps.setString(1, usuario.getNombreCompleto());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, passwordEncriptada);
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getDireccion());
            ps.setString(7, usuario.getCui());
            ps.setObject(8, usuario.getFechaNacimiento());
            ps.setString(9, usuario.getRol());
            ps.setDouble(10, usuario.getSaldo());
            ps.setBoolean(11, usuario.isActivo());
            ps.setBoolean(12, usuario.isPrimeraVez());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    /**
     * Busca un usuario por su username.
     * @return el usuario encontrado, o null si no existe
     */
    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return null;
    }

    /**
     * Verifica si ya existe un username, email o cui en la base de datos.
     * Útil para validar el registro antes de insertar.
     */
    public boolean existeUsernameOEmailOCui(String username, String email, String cui) {
        String sql = "SELECT id_usuario FROM usuarios WHERE username = ? OR email = ? OR cui = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, cui);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error al validar duplicados: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    /**
     * Valida las credenciales de un usuario.
     * @return el usuario si las credenciales son correctas y la cuenta está activa, null en caso contrario
     */
    public Usuario validarCredenciales(String username, String passwordPlano) {
        Usuario usuario = buscarPorUsername(username);
        if (usuario == null) return null;
        if (!usuario.isActivo()) return null;

        boolean passwordValida = PasswordUtil.validar(passwordPlano, usuario.getPassword());
        return passwordValida ? usuario : null;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Usuario.
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        u.setDireccion(rs.getString("direccion"));
        u.setCui(rs.getString("cui"));
        if (rs.getDate("fecha_nacimiento") != null) {
            u.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        }
        u.setRol(rs.getString("rol"));
        u.setSaldo(rs.getDouble("saldo"));
        u.setActivo(rs.getBoolean("activo"));
        u.setPrimeraVez(rs.getBoolean("primera_vez"));
        if (rs.getTimestamp("fecha_creacion") != null) {
            u.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        }
        return u;
    }
    /**
 * Lista todos los usuarios del sistema.
 */
public List<Usuario> listarTodos() {
    List<Usuario> lista = new ArrayList<>();
    String sql = "SELECT * FROM usuarios ORDER BY fecha_creacion DESC";

    Connection con = null;
    try {
        con = ConexionBD.obtenerConexion();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            lista.add(mapearUsuario(rs));
        }
    } catch (SQLException e) {
        System.err.println("Error al listar usuarios: " + e.getMessage());
    } finally {
        ConexionBD.cerrarConexion(con);
    }
    return lista;
}

/**
 * Activa o desactiva un usuario.
 */
public boolean cambiarEstado(int idUsuario, boolean activo) {
    String sql = "UPDATE usuarios SET activo = ? WHERE id_usuario = ?";

    Connection con = null;
    try {
        con = ConexionBD.obtenerConexion();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setBoolean(1, activo);
        ps.setInt(2, idUsuario);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error al cambiar estado: " + e.getMessage());
    } finally {
        ConexionBD.cerrarConexion(con);
    }
    return false;
}
}