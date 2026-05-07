package com.connectwork.dao;

import com.connectwork.util.ConexionBD;
import java.sql.*;
import java.util.*;

public class ReporteDAO {

    // ==================== ADMIN ====================

    /**
     * Historial de porcentajes de comisión.
     */
    public List<Map<String, Object>> historialComisiones() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT porcentaje, fecha_inicio, fecha_fin, id_admin "
                + "FROM historial_comisiones ORDER BY fecha_inicio DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("porcentaje", rs.getDouble("porcentaje"));
                    m.put("fechaInicio", rs.getTimestamp("fecha_inicio") != null ? rs.getTimestamp("fecha_inicio").toString() : null);
                    m.put("fechaFin", rs.getTimestamp("fecha_fin") != null ? rs.getTimestamp("fecha_fin").toString() : null);
                    m.put("idAdmin", rs.getInt("id_admin"));
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error historial comisiones: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Top 5 freelancers con más ingresos en un rango.
     */
    public List<Map<String, Object>> topFreelancersIngresos(String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT u.nombre_completo, COUNT(c.id_contrato) AS contratos, "
                + "SUM(c.monto_bloqueado - (c.monto_bloqueado * c.porcentaje_comision / 100)) AS total_ganado, "
                + "SUM(c.monto_bloqueado * c.porcentaje_comision / 100) AS comision_plataforma "
                + "FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN usuarios u ON p.id_freelancer = u.id_usuario "
                + "WHERE c.estado = 'COMPLETADO' AND c.fecha_cierre BETWEEN ? AND ? "
                + "GROUP BY u.id_usuario, u.nombre_completo "
                + "ORDER BY total_ganado DESC LIMIT 5";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, fechaInicio + " 00:00:00");
                ps.setString(2, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("nombre", rs.getString("nombre_completo"));
                        m.put("contratos", rs.getInt("contratos"));
                        m.put("totalGanado", rs.getDouble("total_ganado"));
                        m.put("comisionPlataforma", rs.getDouble("comision_plataforma"));
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error top freelancers: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Top 5 categorías con más actividad en un rango.
     */
    public List<Map<String, Object>> topCategoriasActividad(String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT cat.nombre, COUNT(c.id_contrato) AS contratos, "
                + "SUM(c.monto_bloqueado * c.porcentaje_comision / 100) AS comisiones "
                + "FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "INNER JOIN categorias cat ON pr.id_categoria = cat.id_categoria "
                + "WHERE c.estado = 'COMPLETADO' AND c.fecha_cierre BETWEEN ? AND ? "
                + "GROUP BY cat.id_categoria, cat.nombre "
                + "ORDER BY contratos DESC LIMIT 5";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, fechaInicio + " 00:00:00");
                ps.setString(2, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("categoria", rs.getString("nombre"));
                        m.put("contratos", rs.getInt("contratos"));
                        m.put("comisiones", rs.getDouble("comisiones"));
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error top categorías: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Total ingresos plataforma en un rango.
     */
    public Map<String, Object> ingresosPlataforma(String fechaInicio, String fechaFin) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String sql = "SELECT COUNT(c.id_contrato) AS contratos, "
                + "SUM(c.monto_bloqueado * c.porcentaje_comision / 100) AS total_comisiones "
                + "FROM contratos c "
                + "WHERE c.estado = 'COMPLETADO' AND c.fecha_cierre BETWEEN ? AND ?";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, fechaInicio + " 00:00:00");
                ps.setString(2, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resultado.put("contratos", rs.getInt("contratos"));
                        resultado.put("totalComisiones", rs.getDouble("total_comisiones"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ingresos plataforma: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return resultado;
    }

    // ==================== CLIENTE ====================

    /**
     * Historial de proyectos del cliente en un rango.
     */
    public List<Map<String, Object>> historialProyectosCliente(int idCliente, String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT pr.titulo, pr.estado, pr.presupuesto, pr.fecha_creacion, "
                + "u.nombre_completo AS freelancer "
                + "FROM proyectos pr "
                + "LEFT JOIN contratos c ON c.id_contrato = (SELECT c2.id_contrato FROM contratos c2 "
                + "  INNER JOIN propuestas p2 ON c2.id_propuesta = p2.id_propuesta "
                + "  WHERE p2.id_proyecto = pr.id_proyecto LIMIT 1) "
                + "LEFT JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "LEFT JOIN usuarios u ON p.id_freelancer = u.id_usuario "
                + "WHERE pr.id_cliente = ? AND pr.fecha_creacion BETWEEN ? AND ? "
                + "ORDER BY pr.fecha_creacion DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.setString(2, fechaInicio + " 00:00:00");
                ps.setString(3, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("titulo", rs.getString("titulo"));
                        m.put("estado", rs.getString("estado"));
                        m.put("presupuesto", rs.getDouble("presupuesto"));
                        m.put("fecha", rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toString() : null);
                        m.put("freelancer", rs.getString("freelancer"));
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error historial proyectos cliente: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Historial de recargas del cliente.
     */
    public List<Map<String, Object>> historialRecargas(int idCliente) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT monto, fecha FROM recargas WHERE id_cliente = ? ORDER BY fecha DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("monto", rs.getDouble("monto"));
                        m.put("fecha", rs.getTimestamp("fecha") != null ? rs.getTimestamp("fecha").toString() : null);
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error historial recargas: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Gasto por categoría del cliente en un rango.
     */
    public List<Map<String, Object>> gastoPorCategoria(int idCliente, String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT cat.nombre, SUM(c.monto_bloqueado) AS total_gastado, COUNT(c.id_contrato) AS contratos "
                + "FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "INNER JOIN categorias cat ON pr.id_categoria = cat.id_categoria "
                + "WHERE pr.id_cliente = ? AND c.estado = 'COMPLETADO' "
                + "AND c.fecha_cierre BETWEEN ? AND ? "
                + "GROUP BY cat.id_categoria, cat.nombre "
                + "ORDER BY total_gastado DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.setString(2, fechaInicio + " 00:00:00");
                ps.setString(3, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("categoria", rs.getString("nombre"));
                        m.put("totalGastado", rs.getDouble("total_gastado"));
                        m.put("contratos", rs.getInt("contratos"));
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error gasto por categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    // ==================== FREELANCER ====================

    /**
     * Historial de contratos completados del freelancer en un rango.
     */
    public List<Map<String, Object>> historialContratosFreelancer(int idFreelancer, String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT pr.titulo, u.nombre_completo AS cliente, "
                + "c.monto_bloqueado - (c.monto_bloqueado * c.porcentaje_comision / 100) AS monto_recibido, "
                + "cal.estrellas, c.fecha_cierre "
                + "FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "INNER JOIN usuarios u ON pr.id_cliente = u.id_usuario "
                + "LEFT JOIN calificaciones cal ON cal.id_contrato = c.id_contrato "
                + "WHERE p.id_freelancer = ? AND c.estado = 'COMPLETADO' "
                + "AND c.fecha_cierre BETWEEN ? AND ? "
                + "ORDER BY c.fecha_cierre DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                ps.setString(2, fechaInicio + " 00:00:00");
                ps.setString(3, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("proyecto", rs.getString("titulo"));
                        m.put("cliente", rs.getString("cliente"));
                        m.put("montoRecibido", rs.getDouble("monto_recibido"));
                        m.put("estrellas", rs.getObject("estrellas"));
                        m.put("fechaCierre", rs.getTimestamp("fecha_cierre") != null ? rs.getTimestamp("fecha_cierre").toString() : null);
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error historial contratos freelancer: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Top 5 categorías en las que más ha trabajado el freelancer.
     */
    public List<Map<String, Object>> topCategoriasFreelancer(int idFreelancer) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT cat.nombre, COUNT(c.id_contrato) AS contratos, "
                + "SUM(c.monto_bloqueado - (c.monto_bloqueado * c.porcentaje_comision / 100)) AS ingresos "
                + "FROM contratos c "
                + "INNER JOIN propuestas p ON c.id_propuesta = p.id_propuesta "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "INNER JOIN categorias cat ON pr.id_categoria = cat.id_categoria "
                + "WHERE p.id_freelancer = ? AND c.estado = 'COMPLETADO' "
                + "GROUP BY cat.id_categoria, cat.nombre "
                + "ORDER BY contratos DESC LIMIT 5";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("categoria", rs.getString("nombre"));
                        m.put("contratos", rs.getInt("contratos"));
                        m.put("ingresos", rs.getDouble("ingresos"));
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error top categorías freelancer: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Propuestas enviadas por el freelancer en un rango.
     */
    public List<Map<String, Object>> reportePropuestasFreelancer(int idFreelancer, String fechaInicio, String fechaFin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT pr.titulo, p.monto_ofertado, p.estado, p.fecha_envio "
                + "FROM propuestas p "
                + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                + "WHERE p.id_freelancer = ? AND p.fecha_envio BETWEEN ? AND ? "
                + "ORDER BY p.fecha_envio DESC";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                ps.setString(2, fechaInicio + " 00:00:00");
                ps.setString(3, fechaFin + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("proyecto", rs.getString("titulo"));
                        m.put("montoOfertado", rs.getDouble("monto_ofertado"));
                        m.put("estado", rs.getString("estado"));
                        m.put("fechaEnvio", rs.getTimestamp("fecha_envio") != null ? rs.getTimestamp("fecha_envio").toString() : null);
                        lista.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reporte propuestas: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Saldo actual del freelancer.
     */
    public double saldoFreelancer(int idFreelancer) {
        String sql = "SELECT saldo FROM usuarios WHERE id_usuario = ?";
        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble("saldo");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saldo freelancer: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return 0;
    }
}