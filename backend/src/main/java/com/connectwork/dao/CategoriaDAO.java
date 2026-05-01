package com.connectwork.dao;

import com.connectwork.model.Categoria;
import com.connectwork.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla `categorias`.
 * Maneja CRUD completo + listado con conteo de habilidades.
 */
public class CategoriaDAO {

    /**
     * Lista todas las categorías junto con el total de habilidades de cada una.
     */
    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT c.*, "
                   + "(SELECT COUNT(*) FROM habilidades h WHERE h.id_categoria = c.id_categoria) AS total_habilidades "
                   + "FROM categorias c "
                   + "ORDER BY c.nombre";

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
            System.err.println("Error al listar categorías: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Lista solo categorías activas (útil para selectores en publicar proyecto).
     */
    public List<Categoria> listarActivas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE activa = true ORDER BY nombre";

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
            System.err.println("Error al listar categorías activas: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Inserta una nueva categoría.
     * @return el ID generado, o -1 si falló
     */
    public int insertar(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre, descripcion, activa) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, categoria.getNombre());
                ps.setString(2, categoria.getDescripcion());
                ps.setBoolean(3, categoria.isActiva());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return -1;
    }

    /**
     * Actualiza nombre y descripción de una categoría.
     */
    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ?, activa = ? WHERE id_categoria = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, categoria.getNombre());
                ps.setString(2, categoria.getDescripcion());
                ps.setBoolean(3, categoria.isActiva());
                ps.setInt(4, categoria.getIdCategoria());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    /**
     * Activa o desactiva una categoría (soft delete).
     */
    public boolean cambiarEstado(int idCategoria, boolean activa) {
        String sql = "UPDATE categorias SET activa = ? WHERE id_categoria = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, activa);
                ps.setInt(2, idCategoria);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    /**
     * Verifica si ya existe una categoría con el mismo nombre.
     * @param idExcluir si se está actualizando, ignorar la propia categoría
     */
    public boolean existeNombre(String nombre, int idExcluir) {
        String sql = "SELECT id_categoria FROM categorias WHERE nombre = ? AND id_categoria != ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setInt(2, idExcluir);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar nombre: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setActiva(rs.getBoolean("activa"));
        if (rs.getTimestamp("fecha_creacion") != null) {
            c.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        }
        // Solo si la query lo trae
        try {
            c.setTotalHabilidades(rs.getInt("total_habilidades"));
        } catch (SQLException ignored) {
            c.setTotalHabilidades(0);
        }
        return c;
    }
}