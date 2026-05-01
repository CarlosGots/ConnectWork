package com.connectwork.dao;

import com.connectwork.model.Habilidad;
import com.connectwork.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla `habilidades`.
 * Cada habilidad pertenece a una categoría.
 */
public class HabilidadDAO {

    /**
     * Lista TODAS las habilidades con el nombre de su categoría.
     */
    public List<Habilidad> listarTodas() {
        List<Habilidad> lista = new ArrayList<>();
        String sql = "SELECT h.*, c.nombre AS nombre_categoria "
                   + "FROM habilidades h "
                   + "INNER JOIN categorias c ON h.id_categoria = c.id_categoria "
                   + "ORDER BY c.nombre, h.nombre";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar habilidades: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Lista habilidades activas filtradas por categoría.
     */
    public List<Habilidad> listarPorCategoria(int idCategoria) {
        List<Habilidad> lista = new ArrayList<>();
        String sql = "SELECT h.*, c.nombre AS nombre_categoria "
                   + "FROM habilidades h "
                   + "INNER JOIN categorias c ON h.id_categoria = c.id_categoria "
                   + "WHERE h.id_categoria = ? AND h.activa = true "
                   + "ORDER BY h.nombre";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCategoria);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar habilidades por categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    public int insertar(Habilidad habilidad) {
        String sql = "INSERT INTO habilidades (nombre, id_categoria, activa) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, habilidad.getNombre());
                ps.setInt(2, habilidad.getIdCategoria());
                ps.setBoolean(3, habilidad.isActiva());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    public boolean actualizar(Habilidad habilidad) {
        String sql = "UPDATE habilidades SET nombre = ?, id_categoria = ?, activa = ? WHERE id_habilidad = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, habilidad.getNombre());
                ps.setInt(2, habilidad.getIdCategoria());
                ps.setBoolean(3, habilidad.isActiva());
                ps.setInt(4, habilidad.getIdHabilidad());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    public boolean cambiarEstado(int idHabilidad, boolean activa) {
        String sql = "UPDATE habilidades SET activa = ? WHERE id_habilidad = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, activa);
                ps.setInt(2, idHabilidad);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    public boolean existeNombreEnCategoria(String nombre, int idCategoria, int idExcluir) {
        String sql = "SELECT id_habilidad FROM habilidades WHERE nombre = ? AND id_categoria = ? AND id_habilidad != ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setInt(2, idCategoria);
                ps.setInt(3, idExcluir);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar nombre de habilidad: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    private Habilidad mapear(ResultSet rs) throws SQLException {
        Habilidad h = new Habilidad();
        h.setIdHabilidad(rs.getInt("id_habilidad"));
        h.setNombre(rs.getString("nombre"));
        h.setIdCategoria(rs.getInt("id_categoria"));
        h.setNombreCategoria(rs.getString("nombre_categoria"));
        h.setActiva(rs.getBoolean("activa"));
        return h;
    }
}