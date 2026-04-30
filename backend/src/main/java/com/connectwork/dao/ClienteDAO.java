package com.connectwork.dao;

import com.connectwork.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Acceso a datos de la tabla `clientes`.
 * Maneja la información específica del rol Cliente, complementaria a `usuarios`.
 */
public class ClienteDAO {

    /**
     * Inserta los datos iniciales del cliente y marca al usuario como ya configurado.
     * @return true si todo salió bien, false en caso contrario
     */
    public boolean insertarInfoInicial(int idCliente, String descripcionEmpresa,
                                       String sector, String sitioWeb) {
        String sqlCliente = "INSERT INTO clientes (id_cliente, descripcion_empresa, sector, sitio_web) "
                          + "VALUES (?, ?, ?, ?) "
                          + "ON DUPLICATE KEY UPDATE descripcion_empresa = VALUES(descripcion_empresa), "
                          + "sector = VALUES(sector), sitio_web = VALUES(sitio_web)";

        String sqlUsuario = "UPDATE usuarios SET primera_vez = false WHERE id_usuario = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);  // Transacción

            // Insertar/actualizar datos del cliente
            try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                ps.setInt(1, idCliente);
                ps.setString(2, descripcionEmpresa);
                ps.setString(3, sector);
                ps.setString(4, sitioWeb);
                ps.executeUpdate();
            }

            // Marcar primera_vez = false
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setInt(1, idCliente);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al insertar info del cliente: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            ConexionBD.cerrarConexion(con);
        }
    }
}