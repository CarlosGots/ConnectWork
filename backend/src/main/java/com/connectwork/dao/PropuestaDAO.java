package com.connectwork.dao;

import com.connectwork.model.Propuesta;
import com.connectwork.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla `propuestas`.
 */
public class PropuestaDAO {

    /**
     * Lista propuestas de un proyecto específico (para que el cliente las vea).
     */
    public List<Propuesta> listarPorProyecto(int idProyecto) {
        List<Propuesta> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.nombre_completo AS nombre_freelancer "
                   + "FROM propuestas p "
                   + "INNER JOIN usuarios u ON p.id_freelancer = u.id_usuario "
                   + "WHERE p.id_proyecto = ? "
                   + "ORDER BY p.fecha_envio DESC";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar propuestas: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Lista propuestas enviadas por un freelancer (para que vea su historial).
     */
    public List<Propuesta> listarPorFreelancer(int idFreelancer) {
        List<Propuesta> lista = new ArrayList<>();
        String sql = "SELECT p.*, pr.titulo AS titulo_proyecto "
                   + "FROM propuestas p "
                   + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                   + "WHERE p.id_freelancer = ? "
                   + "ORDER BY p.fecha_envio DESC";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idFreelancer);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar propuestas del freelancer: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Verifica si un freelancer ya envió propuesta a un proyecto.
     */
    public boolean yaEnvioPropuesta(int idProyecto, int idFreelancer) {
        String sql = "SELECT id_propuesta FROM propuestas "
                   + "WHERE id_proyecto = ? AND id_freelancer = ? "
                   + "AND estado != 'RETIRADA'";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                ps.setInt(2, idFreelancer);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar propuesta: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    /**
     * Inserta una nueva propuesta.
     */
    public int insertar(Propuesta propuesta) {
        String sql = "INSERT INTO propuestas "
                   + "(id_proyecto, id_freelancer, monto_ofertado, plazo_dias, carta_presentacion, estado) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDIENTE')";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, propuesta.getIdProyecto());
                ps.setInt(2, propuesta.getIdFreelancer());
                ps.setDouble(3, propuesta.getMontoOfertado());
                ps.setInt(4, propuesta.getPlazoDias());
                ps.setString(5, propuesta.getCartaPresentacion());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar propuesta: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    /**
     * Acepta una propuesta y genera el contrato automáticamente.
     * Transacción: acepta la propuesta, rechaza las demás, crea el contrato,
     * actualiza el estado del proyecto y bloquea el saldo del cliente.
     */
    public boolean aceptar(int idPropuesta, int idCliente, double porcentajeComision) {
        String sqlObtenerPropuesta = "SELECT p.*, pr.id_cliente, pr.presupuesto "
                                   + "FROM propuestas p "
                                   + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                                   + "WHERE p.id_propuesta = ?";
        String sqlAceptar = "UPDATE propuestas SET estado = 'ACEPTADA' WHERE id_propuesta = ?";
        String sqlRechazarOtras = "UPDATE propuestas SET estado = 'RECHAZADA' "
                                + "WHERE id_proyecto = ? AND id_propuesta != ? AND estado = 'PENDIENTE'";
        String sqlCrearContrato = "INSERT INTO contratos (id_propuesta, monto_bloqueado, porcentaje_comision, estado) "
                                + "VALUES (?, ?, ?, 'ACTIVO')";
        String sqlActualizarProyecto = "UPDATE proyectos SET estado = 'EN_PROGRESO' WHERE id_proyecto = ?";
        String sqlDescontarSaldo = "UPDATE usuarios SET saldo = saldo - ? WHERE id_usuario = ? AND saldo >= ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            // 1. Obtener datos de la propuesta
            int idProyecto;
            double montoOfertado;
            try (PreparedStatement ps = con.prepareStatement(sqlObtenerPropuesta)) {
                ps.setInt(1, idPropuesta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                    idProyecto = rs.getInt("id_proyecto");
                    montoOfertado = rs.getDouble("monto_ofertado");
                    // Verificar que es el cliente del proyecto
                    if (rs.getInt("id_cliente") != idCliente) { con.rollback(); return false; }
                }
            }

            // 2. Verificar que el cliente tiene saldo suficiente
            String sqlSaldo = "SELECT saldo FROM usuarios WHERE id_usuario = ?";
            double saldoCliente = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) saldoCliente = rs.getDouble("saldo");
                }
            }
            if (saldoCliente < montoOfertado) {
                con.rollback();
                return false;
            }

            // 3. Aceptar la propuesta
            try (PreparedStatement ps = con.prepareStatement(sqlAceptar)) {
                ps.setInt(1, idPropuesta);
                ps.executeUpdate();
            }

            // 4. Rechazar las otras propuestas del proyecto
            try (PreparedStatement ps = con.prepareStatement(sqlRechazarOtras)) {
                ps.setInt(1, idProyecto);
                ps.setInt(2, idPropuesta);
                ps.executeUpdate();
            }

            // 5. Crear el contrato
            try (PreparedStatement ps = con.prepareStatement(sqlCrearContrato)) {
                ps.setInt(1, idPropuesta);
                ps.setDouble(2, montoOfertado);
                ps.setDouble(3, porcentajeComision);
                ps.executeUpdate();
            }

            // 6. Actualizar estado del proyecto a EN_PROGRESO
            try (PreparedStatement ps = con.prepareStatement(sqlActualizarProyecto)) {
                ps.setInt(1, idProyecto);
                ps.executeUpdate();
            }

            // 7. Descontar saldo del cliente (bloquearlo)
            try (PreparedStatement ps = con.prepareStatement(sqlDescontarSaldo)) {
                ps.setDouble(1, montoOfertado);
                ps.setInt(2, idCliente);
                ps.setDouble(3, montoOfertado);
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al aceptar propuesta: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    /**
     * Rechaza una propuesta específica.
     */
    public boolean rechazar(int idPropuesta, int idCliente) {
        String sql = "UPDATE propuestas p "
                   + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
                   + "SET p.estado = 'RECHAZADA' "
                   + "WHERE p.id_propuesta = ? AND pr.id_cliente = ? AND p.estado = 'PENDIENTE'";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idPropuesta);
                ps.setInt(2, idCliente);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al rechazar propuesta: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    private Propuesta mapear(ResultSet rs) throws SQLException {
        Propuesta p = new Propuesta();
        p.setIdPropuesta(rs.getInt("id_propuesta"));
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setIdFreelancer(rs.getInt("id_freelancer"));
        p.setMontoOfertado(rs.getDouble("monto_ofertado"));
        p.setPlazoDias(rs.getInt("plazo_dias"));
        p.setCartaPresentacion(rs.getString("carta_presentacion"));
        p.setEstado(rs.getString("estado"));
        if (rs.getTimestamp("fecha_envio") != null) {
            p.setFechaEnvio(rs.getTimestamp("fecha_envio").toLocalDateTime());
        }
        // Campos opcionales según la query
        try { p.setNombreFreelancer(rs.getString("nombre_freelancer")); } catch (SQLException ignored) {}
        try { p.setTituloProyecto(rs.getString("titulo_proyecto")); } catch (SQLException ignored) {}
        return p;
    }
    
    /**
 * Retira una propuesta del freelancer (solo si el proyecto está ABIERTO).
 */
public boolean retirar(int idPropuesta, int idFreelancer) {
    String sql = "UPDATE propuestas p "
               + "INNER JOIN proyectos pr ON p.id_proyecto = pr.id_proyecto "
               + "SET p.estado = 'RETIRADA' "
               + "WHERE p.id_propuesta = ? AND p.id_freelancer = ? "
               + "AND p.estado = 'PENDIENTE' AND pr.estado = 'ABIERTO'";
    Connection con = null;
    try {
        con = ConexionBD.obtenerConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPropuesta);
            ps.setInt(2, idFreelancer);
            return ps.executeUpdate() > 0;
        }
    } catch (SQLException e) {
        System.err.println("Error al retirar propuesta: " + e.getMessage());
    } finally {
        ConexionBD.cerrarConexion(con);
    }
    return false;
}
}