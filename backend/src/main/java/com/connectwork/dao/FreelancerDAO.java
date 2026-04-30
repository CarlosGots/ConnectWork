package com.connectwork.dao;

import com.connectwork.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a datos de la tabla `freelancers` y `freelancer_habilidades`.
 * Maneja la información específica del rol Freelancer.
 */
public class FreelancerDAO {

    /**
     * Inserta los datos iniciales del freelancer (incluyendo habilidades)
     * y marca al usuario como ya configurado.
     */
    public boolean insertarInfoInicial(int idFreelancer, String biografia,
                                       String nivelExperiencia, double tarifaHora,
                                       List<Integer> idsHabilidades) {
        String sqlFreelancer = "INSERT INTO freelancers (id_freelancer, biografia, nivel_experiencia, tarifa_hora) "
                             + "VALUES (?, ?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE biografia = VALUES(biografia), "
                             + "nivel_experiencia = VALUES(nivel_experiencia), tarifa_hora = VALUES(tarifa_hora)";

        String sqlBorrarHabilidades = "DELETE FROM freelancer_habilidades WHERE id_freelancer = ?";
        String sqlInsertarHabilidad = "INSERT INTO freelancer_habilidades (id_freelancer, id_habilidad) VALUES (?, ?)";
        String sqlUsuario = "UPDATE usuarios SET primera_vez = false WHERE id_usuario = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            // Insertar/actualizar datos del freelancer
            try (PreparedStatement ps = con.prepareStatement(sqlFreelancer)) {
                ps.setInt(1, idFreelancer);
                ps.setString(2, biografia);
                ps.setString(3, nivelExperiencia);
                ps.setDouble(4, tarifaHora);
                ps.executeUpdate();
            }

            // Limpiar habilidades anteriores
            try (PreparedStatement ps = con.prepareStatement(sqlBorrarHabilidades)) {
                ps.setInt(1, idFreelancer);
                ps.executeUpdate();
            }

            // Insertar habilidades nuevas
            if (idsHabilidades != null && !idsHabilidades.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlInsertarHabilidad)) {
                    for (Integer idHabilidad : idsHabilidades) {
                        ps.setInt(1, idFreelancer);
                        ps.setInt(2, idHabilidad);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // Marcar primera_vez = false
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setInt(1, idFreelancer);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al insertar info del freelancer: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            ConexionBD.cerrarConexion(con);
        }
    }
}