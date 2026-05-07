package com.connectwork.dao;

import com.connectwork.util.ConexionBD;

import java.sql.*;

/**
 * Acceso a datos de la tabla `entregas`.
 */
public class EntregaDAO {

    /**
     * Obtiene la entrega activa de un contrato.
     */
    public int obtenerIdContratoPorFreelancer(int idFreelancer) {
        String sql = "SELECT c.id_contrato FROM contratos c "
                   + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                   + "WHERE p.id_freelancer = ? AND c.estado = 'ACTIVO'";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("id_contrato");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    /**
     * Sube una nueva entrega para un contrato.
     */
    public int subirEntrega(int idContrato, String descripcion, String archivosUrl) {
        String sqlEntrega = "INSERT INTO entregas (id_contrato, descripcion, archivos_url, estado) "
                          + "VALUES (?, ?, ?, 'PENDIENTE')";
        String sqlProyecto = "UPDATE proyectos p "
                           + "INNER JOIN propuestas pr ON pr.id_proyecto = p.id_proyecto "
                           + "INNER JOIN contratos c ON c.id_propuesta = pr.id_propuesta "
                           + "SET p.estado = 'ENTREGA_PENDIENTE' "
                           + "WHERE c.id_contrato = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            int idGenerado;
            try (PreparedStatement ps = con.prepareStatement(sqlEntrega, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idContrato);
                ps.setString(2, descripcion);
                ps.setString(3, archivosUrl);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) { con.rollback(); return -1; }
                    idGenerado = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlProyecto)) {
                ps.setInt(1, idContrato);
                ps.executeUpdate();
            }

            con.commit();
            return idGenerado;

        } catch (SQLException e) {
            System.err.println("Error al subir entrega: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    /**
     * Aprueba una entrega: libera el pago al freelancer y completa el contrato.
     */
    public boolean aprobar(int idEntrega, int idCliente) {
        String sqlObtener = "SELECT e.id_contrato, c.monto_bloqueado, c.porcentaje_comision, "
                          + "p.id_freelancer "
                          + "FROM entregas e "
                          + "INNER JOIN contratos c ON e.id_contrato = c.id_contrato "
                          + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                          + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                          + "WHERE e.id_entrega = ? AND pr.id_cliente = ? AND e.estado = 'PENDIENTE'";
        String sqlEntrega   = "UPDATE entregas SET estado = 'APROBADA', fecha_revision = NOW() WHERE id_entrega = ?";
        String sqlContrato  = "UPDATE contratos SET estado = 'COMPLETADO', fecha_cierre = NOW() WHERE id_contrato = ?";
        String sqlProyecto  = "UPDATE proyectos p "
                            + "INNER JOIN propuestas pr ON pr.id_proyecto = p.id_proyecto "
                            + "INNER JOIN contratos c ON c.id_propuesta = pr.id_propuesta "
                            + "SET p.estado = 'COMPLETADO' WHERE c.id_contrato = ?";
        String sqlPagoFreelancer = "UPDATE usuarios SET saldo = saldo + ? WHERE id_usuario = ?";
        String sqlSaldoPlataforma = "UPDATE saldo_plataforma SET saldo_actual = saldo_actual + ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            int idContrato, idFreelancer;
            double montoBloqueado, porcentajeComision;

            try (PreparedStatement ps = con.prepareStatement(sqlObtener)) {
                ps.setInt(1, idEntrega);
                ps.setInt(2, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                    idContrato = rs.getInt("id_contrato");
                    montoBloqueado = rs.getDouble("monto_bloqueado");
                    porcentajeComision = rs.getDouble("porcentaje_comision");
                    idFreelancer = rs.getInt("id_freelancer");
                }
            }

            double comision = montoBloqueado * (porcentajeComision / 100);
            double pagoFreelancer = montoBloqueado - comision;

            // Aprobar entrega
            try (PreparedStatement ps = con.prepareStatement(sqlEntrega)) {
                ps.setInt(1, idEntrega); ps.executeUpdate();
            }
            // Completar contrato
            try (PreparedStatement ps = con.prepareStatement(sqlContrato)) {
                ps.setInt(1, idContrato); ps.executeUpdate();
            }
            // Completar proyecto
            try (PreparedStatement ps = con.prepareStatement(sqlProyecto)) {
                ps.setInt(1, idContrato); ps.executeUpdate();
            }
            // Pagar al freelancer
            try (PreparedStatement ps = con.prepareStatement(sqlPagoFreelancer)) {
                ps.setDouble(1, pagoFreelancer);
                ps.setInt(2, idFreelancer);
                ps.executeUpdate();
            }
            // Acreditar comisión a la plataforma
            try (PreparedStatement ps = con.prepareStatement(sqlSaldoPlataforma)) {
                ps.setDouble(1, comision); ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al aprobar entrega: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    /**
     * Rechaza una entrega con motivo.
     */
    public boolean rechazar(int idEntrega, int idCliente, String motivo) {
        String sqlVerificar = "SELECT e.id_contrato FROM entregas e "
                            + "INNER JOIN contratos c ON e.id_contrato = c.id_contrato "
                            + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                            + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                            + "WHERE e.id_entrega = ? AND pr.id_cliente = ? AND e.estado = 'PENDIENTE'";
        String sqlEntrega  = "UPDATE entregas SET estado = 'RECHAZADA', motivo_rechazo = ?, "
                           + "fecha_revision = NOW() WHERE id_entrega = ?";
        String sqlProyecto = "UPDATE proyectos p "
                           + "INNER JOIN propuestas pr ON pr.id_proyecto = p.id_proyecto "
                           + "INNER JOIN contratos c ON c.id_propuesta = pr.id_propuesta "
                           + "SET p.estado = 'EN_PROGRESO' "
                           + "WHERE c.id_contrato = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            int idContrato;
            try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                ps.setInt(1, idEntrega);
                ps.setInt(2, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                    idContrato = rs.getInt("id_contrato");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlEntrega)) {
                ps.setString(1, motivo);
                ps.setInt(2, idEntrega);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlProyecto)) {
                ps.setInt(1, idContrato);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al rechazar entrega: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    /**
     * Lista entregas de un contrato.
     */
    public java.util.List<java.util.Map<String, Object>> listarPorContrato(int idContrato) {
        java.util.List<java.util.Map<String, Object>> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM entregas WHERE id_contrato = ? ORDER BY fecha_entrega DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.util.Map<String, Object> e = new java.util.LinkedHashMap<>();
                        e.put("idEntrega", rs.getInt("id_entrega"));
                        e.put("idContrato", rs.getInt("id_contrato"));
                        e.put("descripcion", rs.getString("descripcion"));
                        e.put("archivosUrl", rs.getString("archivos_url"));
                        e.put("estado", rs.getString("estado"));
                        e.put("motivoRechazo", rs.getString("motivo_rechazo"));
                        e.put("fechaEntrega", rs.getTimestamp("fecha_entrega") != null ? rs.getTimestamp("fecha_entrega").toString() : null);
                        lista.add(e);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar entregas: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }
    /**
 * Lista contratos activos de un freelancer con info del proyecto.
 */
public java.util.List<java.util.Map<String, Object>> contratosActivosFreelancer(int idFreelancer) {
    java.util.List<java.util.Map<String, Object>> lista = new java.util.ArrayList<>();
    String sql = "SELECT c.id_contrato, c.monto_bloqueado, c.fecha_firma, c.estado AS estado_contrato, "
            + "pr.id_proyecto, pr.titulo, pr.descripcion AS desc_proyecto, pr.fecha_limite, pr.estado AS estado_proyecto, "
            + "u.nombre_completo AS nombre_cliente "
            + "FROM contratos c "
            + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
            + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
            + "INNER JOIN usuarios u ON pr.id_cliente = u.id_usuario "
            + "WHERE p.id_freelancer = ? AND c.estado = 'ACTIVO' "
            + "ORDER BY c.fecha_firma DESC";

    Connection con = null;
    try {
        con = ConexionBD.obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFreelancer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("idContrato", rs.getInt("id_contrato"));
                    m.put("montoBloqueado", rs.getDouble("monto_bloqueado"));
                    m.put("fechaFirma", rs.getTimestamp("fecha_firma") != null ? rs.getTimestamp("fecha_firma").toString() : null);
                    m.put("estadoContrato", rs.getString("estado_contrato"));
                    m.put("idProyecto", rs.getInt("id_proyecto"));
                    m.put("tituloProyecto", rs.getString("titulo"));
                    m.put("descripcionProyecto", rs.getString("desc_proyecto"));
                    m.put("fechaLimite", rs.getDate("fecha_limite") != null ? rs.getDate("fecha_limite").toString() : null);
                    m.put("estadoProyecto", rs.getString("estado_proyecto"));
                    m.put("nombreCliente", rs.getString("nombre_cliente"));
                    lista.add(m);
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al listar contratos activos: " + e.getMessage());
    } finally {
        ConexionBD.cerrarConexion(con);
    }
    return lista;
}
/**
 * Lista entregas pendientes de revisión para un cliente.
 * Muestra entregas PENDIENTE de contratos de proyectos del cliente.
 */
public java.util.List<java.util.Map<String, Object>> entregasPendientesCliente(int idCliente) {
    java.util.List<java.util.Map<String, Object>> lista = new java.util.ArrayList<>();
    String sql = "SELECT e.id_entrega, e.descripcion, e.archivos_url, e.estado, "
            + "e.motivo_rechazo, e.fecha_entrega, "
            + "pr.titulo AS titulo_proyecto, pr.id_proyecto, "
            + "u.nombre_completo AS nombre_freelancer, "
            + "c.id_contrato, c.monto_bloqueado "
            + "FROM entregas e "
            + "INNER JOIN contratos c ON e.id_contrato = c.id_contrato "
            + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
            + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
            + "INNER JOIN usuarios u ON p.id_freelancer = u.id_usuario "
            + "WHERE pr.id_cliente = ? "
            + "ORDER BY e.fecha_entrega DESC";

    Connection con = null;
    try {
        con = ConexionBD.obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("idEntrega", rs.getInt("id_entrega"));
                    m.put("descripcion", rs.getString("descripcion"));
                    m.put("archivosUrl", rs.getString("archivos_url"));
                    m.put("estado", rs.getString("estado"));
                    m.put("motivoRechazo", rs.getString("motivo_rechazo"));
                    m.put("fechaEntrega", rs.getTimestamp("fecha_entrega") != null ? rs.getTimestamp("fecha_entrega").toString() : null);
                    m.put("tituloProyecto", rs.getString("titulo_proyecto"));
                    m.put("idProyecto", rs.getInt("id_proyecto"));
                    m.put("nombreFreelancer", rs.getString("nombre_freelancer"));
                    m.put("idContrato", rs.getInt("id_contrato"));
                    m.put("montoBloqueado", rs.getDouble("monto_bloqueado"));
                    lista.add(m);
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al listar entregas del cliente: " + e.getMessage());
    } finally {
        ConexionBD.cerrarConexion(con);
    }
    return lista;
}
}