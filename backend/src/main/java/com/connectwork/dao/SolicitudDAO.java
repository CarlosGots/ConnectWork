package com.connectwork.dao;

import com.connectwork.util.ConexionBD;
import java.sql.*;
import java.util.*;

public class SolicitudDAO {

    // ========== SOLICITUDES DE CATEGORÍA (clientes) ==========

    public int crearSolicitudCategoria(int idCliente, String nombre, String descripcion) {
        String sql = "INSERT INTO solicitudes_categoria (id_cliente, nombre_propuesto, descripcion) VALUES (?, ?, ?)";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.setString(2, nombre);
                ps.setString(3, descripcion);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error crear solicitud categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    public List<Map<String, Object>> listarSolicitudesCategoria(String estado) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT sc.*, u.nombre_completo AS nombre_cliente FROM solicitudes_categoria sc "
                + "INNER JOIN usuarios u ON sc.id_cliente = u.id_usuario "
                + (estado != null ? "WHERE sc.estado = ? " : "")
                + "ORDER BY sc.fecha_solicitud DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (estado != null) ps.setString(1, estado);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("idSolicitud", rs.getInt("id_solicitud"));
                        m.put("idCliente", rs.getInt("id_cliente"));
                        m.put("nombreCliente", rs.getString("nombre_cliente"));
                        m.put("nombrePropuesto", rs.getString("nombre_propuesto"));
                        m.put("descripcion", rs.getString("descripcion"));
                        m.put("estado", rs.getString("estado"));
                        m.put("fechaSolicitud", rs.getTimestamp("fecha_solicitud") != null ? rs.getTimestamp("fecha_solicitud").toString() : null);
                        m.put("tipo", "CATEGORIA");
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listar solicitudes categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    public boolean aceptarSolicitudCategoria(int idSolicitud) {
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            // Obtener datos de la solicitud
            String sqlGet = "SELECT nombre_propuesto, descripcion FROM solicitudes_categoria WHERE id_solicitud = ? AND estado = 'PENDIENTE'";
            String nombre, descripcion;
            try (PreparedStatement ps = con.prepareStatement(sqlGet)) {
                ps.setInt(1, idSolicitud);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                    nombre = rs.getString("nombre_propuesto");
                    descripcion = rs.getString("descripcion");
                }
            }

            // Crear la categoría
            String sqlCat = "INSERT INTO categorias (nombre, descripcion, activa) VALUES (?, ?, 1)";
            try (PreparedStatement ps = con.prepareStatement(sqlCat)) {
                ps.setString(1, nombre);
                ps.setString(2, descripcion != null ? descripcion : "");
                ps.executeUpdate();
            }

            // Actualizar estado
            String sqlUpdate = "UPDATE solicitudes_categoria SET estado = 'ACEPTADA', fecha_revision = NOW() WHERE id_solicitud = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setInt(1, idSolicitud);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error aceptar solicitud categoría: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    public boolean rechazarSolicitudCategoria(int idSolicitud) {
        String sql = "UPDATE solicitudes_categoria SET estado = 'RECHAZADA', fecha_revision = NOW() WHERE id_solicitud = ? AND estado = 'PENDIENTE'";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idSolicitud);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error rechazar solicitud categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    // ========== SOLICITUDES DE HABILIDAD (freelancers) ==========

    public int crearSolicitudHabilidad(int idFreelancer, int idCategoria, String nombre, String descripcion) {
        String sql = "INSERT INTO solicitudes_habilidad (id_freelancer, id_categoria, nombre_propuesto, descripcion) VALUES (?, ?, ?, ?)";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idFreelancer);
                ps.setInt(2, idCategoria);
                ps.setString(3, nombre);
                ps.setString(4, descripcion);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error crear solicitud habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    public List<Map<String, Object>> listarSolicitudesHabilidad(String estado) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT sh.*, u.nombre_completo AS nombre_freelancer, c.nombre AS nombre_categoria "
                + "FROM solicitudes_habilidad sh "
                + "INNER JOIN usuarios u ON sh.id_freelancer = u.id_usuario "
                + "INNER JOIN categorias c ON sh.id_categoria = c.id_categoria "
                + (estado != null ? "WHERE sh.estado = ? " : "")
                + "ORDER BY sh.fecha_solicitud DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (estado != null) ps.setString(1, estado);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("idSolicitud", rs.getInt("id_solicitud"));
                        m.put("idFreelancer", rs.getInt("id_freelancer"));
                        m.put("nombreFreelancer", rs.getString("nombre_freelancer"));
                        m.put("idCategoria", rs.getInt("id_categoria"));
                        m.put("nombreCategoria", rs.getString("nombre_categoria"));
                        m.put("nombrePropuesto", rs.getString("nombre_propuesto"));
                        m.put("descripcion", rs.getString("descripcion"));
                        m.put("estado", rs.getString("estado"));
                        m.put("fechaSolicitud", rs.getTimestamp("fecha_solicitud") != null ? rs.getTimestamp("fecha_solicitud").toString() : null);
                        m.put("tipo", "HABILIDAD");
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listar solicitudes habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    public boolean aceptarSolicitudHabilidad(int idSolicitud) {
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            String sqlGet = "SELECT nombre_propuesto, id_categoria FROM solicitudes_habilidad WHERE id_solicitud = ? AND estado = 'PENDIENTE'";
            String nombre;
            int idCategoria;
            try (PreparedStatement ps = con.prepareStatement(sqlGet)) {
                ps.setInt(1, idSolicitud);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                    nombre = rs.getString("nombre_propuesto");
                    idCategoria = rs.getInt("id_categoria");
                }
            }

            String sqlHab = "INSERT INTO habilidades (nombre, id_categoria, activa) VALUES (?, ?, 1)";
            try (PreparedStatement ps = con.prepareStatement(sqlHab)) {
                ps.setString(1, nombre);
                ps.setInt(2, idCategoria);
                ps.executeUpdate();
            }

            String sqlUpdate = "UPDATE solicitudes_habilidad SET estado = 'ACEPTADA', fecha_revision = NOW() WHERE id_solicitud = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setInt(1, idSolicitud);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error aceptar solicitud habilidad: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    public boolean rechazarSolicitudHabilidad(int idSolicitud) {
        String sql = "UPDATE solicitudes_habilidad SET estado = 'RECHAZADA', fecha_revision = NOW() WHERE id_solicitud = ? AND estado = 'PENDIENTE'";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idSolicitud);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error rechazar solicitud habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }
}