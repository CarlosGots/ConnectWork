package com.connectwork.dao;

import com.connectwork.util.ConexionBD;
import java.sql.*;

public class CalificacionDAO {

    /**
     * Inserta una calificación. Verifica que el contrato esté COMPLETADO
     * y que el cliente sea el dueño del proyecto.
     */
    public int insertar(int idContrato, int idCliente, int estrellas, String comentario) {
        String sqlVerificar = "SELECT c.id_contrato FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "WHERE c.id_contrato = ? AND pr.id_cliente = ? AND c.estado = 'COMPLETADO'";
        String sqlYaExiste = "SELECT id_calificacion FROM calificaciones WHERE id_contrato = ?";
        String sqlInsertar = "INSERT INTO calificaciones (id_contrato, estrellas, comentario) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();

            // Verificar que el contrato pertenece al cliente y está completado
            try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                ps.setInt(1, idContrato);
                ps.setInt(2, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return -1;
                }
            }

            // Verificar que no exista calificación previa
            try (PreparedStatement ps = con.prepareStatement(sqlYaExiste)) {
                ps.setInt(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return -2; // Ya calificado
                }
            }

            // Insertar
            try (PreparedStatement ps = con.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idContrato);
                ps.setInt(2, estrellas);
                ps.setString(3, comentario);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar calificación: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    /**
     * Verifica si un contrato ya fue calificado.
     */
    public boolean yaCalificado(int idContrato) {
        String sql = "SELECT id_calificacion FROM calificaciones WHERE id_contrato = ?";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    /**
     * Obtiene el promedio de estrellas de un freelancer.
     */
    public double promedioFreelancer(int idFreelancer) {
        String sql = "SELECT AVG(cal.estrellas) AS promedio FROM calificaciones cal "
                + "INNER JOIN contratos c ON cal.id_contrato = c.id_contrato "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "WHERE p.id_freelancer = ?";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble("promedio");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return 0;
    }

    /**
     * Obtiene los contratos completados de un cliente que aún no han sido calificados.
     */
    public java.util.List<java.util.Map<String, Object>> contratosParaCalificar(int idCliente) {
        java.util.List<java.util.Map<String, Object>> lista = new java.util.ArrayList<>();
        String sql = "SELECT c.id_contrato, c.monto_bloqueado, c.fecha_cierre, "
                + "pr.titulo, u.nombre_completo AS nombre_freelancer "
                + "FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "INNER JOIN usuarios u ON p.id_freelancer = u.id_usuario "
                + "WHERE pr.id_cliente = ? AND c.estado = 'COMPLETADO' "
                + "AND c.id_contrato NOT IN (SELECT id_contrato FROM calificaciones) "
                + "ORDER BY c.fecha_cierre DESC";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("idContrato", rs.getInt("id_contrato"));
                        m.put("montoBloqueado", rs.getDouble("monto_bloqueado"));
                        m.put("fechaCierre", rs.getTimestamp("fecha_cierre") != null ? rs.getTimestamp("fecha_cierre").toString() : null);
                        m.put("tituloProyecto", rs.getString("titulo"));
                        m.put("nombreFreelancer", rs.getString("nombre_freelancer"));
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }
}