package com.connectwork.dao;

import com.connectwork.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos de la tabla `recargas`.
 * Gestiona las recargas de saldo de los clientes.
 */
public class RecargaDAO {

    /**
     * Registra una recarga y actualiza el saldo del cliente.
     * Operación transaccional.
     * @return true si fue exitoso
     */
    public boolean recargar(int idCliente, double monto) {
        String sqlRecarga  = "INSERT INTO recargas (id_cliente, monto) VALUES (?, ?)";
        String sqlSaldo    = "UPDATE usuarios SET saldo = saldo + ? WHERE id_usuario = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            // 1. Registrar la recarga
            try (PreparedStatement ps = con.prepareStatement(sqlRecarga)) {
                ps.setInt(1, idCliente);
                ps.setDouble(2, monto);
                ps.executeUpdate();
            }

            // 2. Actualizar saldo del usuario
            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setDouble(1, monto);
                ps.setInt(2, idCliente);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al recargar: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    /**
     * Obtiene el saldo actual de un usuario.
     */
    public double obtenerSaldo(int idUsuario) {
        String sql = "SELECT saldo FROM usuarios WHERE id_usuario = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble("saldo");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener saldo: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return 0.0;
    }
}