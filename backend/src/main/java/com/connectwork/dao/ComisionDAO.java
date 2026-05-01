package com.connectwork.dao;

import com.connectwork.model.Comision;
import com.connectwork.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de configuracion_comision e historial_comisiones.
 */
public class ComisionDAO {

    /**
     * Obtiene la comisión actualmente vigente.
     */
    public Comision obtenerActiva() {
        String sql = "SELECT c.*, u.nombre_completo AS nombre_admin "
                   + "FROM configuracion_comision c "
                   + "LEFT JOIN usuarios u ON c.id_admin_cambio = u.id_usuario "
                   + "WHERE c.activa = 1 "
                   + "ORDER BY c.fecha_inicio DESC LIMIT 1";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener comisión activa: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return null;
    }

    /**
     * Lista el historial completo de comisiones.
     */
    public List<Comision> listarHistorial() {
        List<Comision> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.nombre_completo AS nombre_admin "
                   + "FROM configuracion_comision c "
                   + "LEFT JOIN usuarios u ON c.id_admin_cambio = u.id_usuario "
                   + "ORDER BY c.fecha_inicio DESC";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar historial: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Cambia la comisión vigente con transacción.
     */
    public boolean cambiarComision(double nuevoPorcentaje, int idAdmin) {
        String sqlObtenerActual = "SELECT porcentaje FROM configuracion_comision WHERE activa = 1 LIMIT 1";
        String sqlDesactivar    = "UPDATE configuracion_comision SET activa = 0 WHERE activa = 1";
        String sqlInsertarNueva = "INSERT INTO configuracion_comision (porcentaje, activa, id_admin_cambio) VALUES (?, 1, ?)";
        String sqlHistorial     = "INSERT INTO historial_comisiones (porcentaje, fecha_inicio, id_admin) VALUES (?, NOW(), ?)";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            // 1. Obtener porcentaje anterior
            double porcentajeAnterior = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlObtenerActual);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) porcentajeAnterior = rs.getDouble("porcentaje");
            }

            // 2. Guardar en historial el anterior
            try (PreparedStatement ps = con.prepareStatement(sqlHistorial)) {
                ps.setDouble(1, porcentajeAnterior);
                ps.setInt(2, idAdmin);
                ps.executeUpdate();
            }

            // 3. Desactivar comisión anterior
            try (PreparedStatement ps = con.prepareStatement(sqlDesactivar)) {
                ps.executeUpdate();
            }

            // 4. Insertar nueva comisión activa
            try (PreparedStatement ps = con.prepareStatement(sqlInsertarNueva)) {
                ps.setDouble(1, nuevoPorcentaje);
                ps.setInt(2, idAdmin);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al cambiar comisión: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    private Comision mapear(ResultSet rs) throws SQLException {
        Comision c = new Comision();
        c.setIdComision(rs.getInt("id_config"));
        c.setPorcentaje(rs.getDouble("porcentaje"));
        if (rs.getTimestamp("fecha_inicio") != null) {
            c.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        }
        c.setActiva(rs.getBoolean("activa"));
        int idAdmin = rs.getInt("id_admin_cambio");
        if (!rs.wasNull()) c.setIdAdminCambio(idAdmin);
        c.setNombreAdminCambio(rs.getString("nombre_admin"));
        return c;
    }
}