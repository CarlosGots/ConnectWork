package com.connectwork.dao;

import com.connectwork.model.Proyecto;
import com.connectwork.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla `proyectos` y `proyecto_habilidades`.
 */
public class ProyectoDAO {

    /**
     * Lista todos los proyectos ABIERTOS (para que los freelancers los vean).
     */
    public List<Proyecto> listarAbiertos() {
        List<Proyecto> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.nombre_completo AS nombre_cliente, "
                   + "c.nombre AS nombre_categoria "
                   + "FROM proyectos p "
                   + "INNER JOIN usuarios u ON p.id_cliente = u.id_usuario "
                   + "INNER JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "WHERE p.estado = 'ABIERTO' "
                   + "ORDER BY p.fecha_creacion DESC";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar proyectos: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Lista proyectos de un cliente específico.
     */
    public List<Proyecto> listarPorCliente(int idCliente) {
        List<Proyecto> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.nombre_completo AS nombre_cliente, "
                   + "c.nombre AS nombre_categoria "
                   + "FROM proyectos p "
                   + "INNER JOIN usuarios u ON p.id_cliente = u.id_usuario "
                   + "INNER JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "WHERE p.id_cliente = ? "
                   + "ORDER BY p.fecha_creacion DESC";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar proyectos del cliente: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return lista;
    }

    /**
     * Obtiene un proyecto por su ID.
     */
    public Proyecto obtenerPorId(int idProyecto) {
        String sql = "SELECT p.*, u.nombre_completo AS nombre_cliente, "
                   + "c.nombre AS nombre_categoria "
                   + "FROM proyectos p "
                   + "INNER JOIN usuarios u ON p.id_cliente = u.id_usuario "
                   + "INNER JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "WHERE p.id_proyecto = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proyecto: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return null;
    }

    /**
     * Inserta un nuevo proyecto con sus habilidades requeridas.
     * Operación transaccional.
     * @return el ID generado, o -1 si falló
     */
    public int insertar(Proyecto proyecto, List<Integer> idsHabilidades) {
       String sqlProyecto = "INSERT INTO proyectos "
        + "(id_cliente, id_categoria, titulo, descripcion, presupuesto, fecha_limite, estado) "
        + "VALUES (?, ?, ?, ?, ?, ?, 'ABIERTO')";
        String sqlHabilidad = "INSERT INTO proyecto_habilidades (id_proyecto, id_habilidad) VALUES (?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            con.setAutoCommit(false);

            int idGenerado;
            try (PreparedStatement ps = con.prepareStatement(sqlProyecto, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, proyecto.getIdCliente());
                ps.setInt(2, proyecto.getIdCategoria());
                ps.setString(3, proyecto.getTitulo());
                ps.setString(4, proyecto.getDescripcion());
                ps.setDouble(5, proyecto.getPresupuesto());
                ps.setObject(6, proyecto.getFechaLimite());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        return -1;
                    }
                    idGenerado = rs.getInt(1);
                }
            }

            // Insertar habilidades requeridas
            if (idsHabilidades != null && !idsHabilidades.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlHabilidad)) {
                    for (int idHabilidad : idsHabilidades) {
                        ps.setInt(1, idGenerado);
                        ps.setInt(2, idHabilidad);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            con.commit();
            return idGenerado;

        } catch (SQLException e) {
            System.err.println("Error al insertar proyecto: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConexionBD.cerrarConexion(con);
        }
    }

    /**
     * Obtiene las IDs de habilidades requeridas por un proyecto.
     */
    public List<Integer> obtenerHabilidades(int idProyecto) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_habilidad FROM proyecto_habilidades WHERE id_proyecto = ?";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(rs.getInt("id_habilidad"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener habilidades: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return ids;
    }

    /**
     * Cancela un proyecto (solo si está ABIERTO y es del cliente que lo pide).
     */
    public boolean cancelar(int idProyecto, int idCliente) {
        String sql = "UPDATE proyectos SET estado = 'CANCELADO' "
                   + "WHERE id_proyecto = ? AND id_cliente = ? AND estado = 'ABIERTO'";

        Connection con = null;
        try {
            con = ConexionBD.obtenerConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProyecto);
                ps.setInt(2, idCliente);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cancelar proyecto: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion(con);
        }
        return false;
    }

    private Proyecto mapear(ResultSet rs) throws SQLException {
        Proyecto p = new Proyecto();
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setTitulo(rs.getString("titulo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPresupuesto(rs.getDouble("presupuesto"));
        p.setEstado(rs.getString("estado"));
        p.setNombreCliente(rs.getString("nombre_cliente"));
        p.setNombreCategoria(rs.getString("nombre_categoria"));
        if (rs.getDate("fecha_limite") != null) {
            p.setFechaLimite(rs.getDate("fecha_limite").toLocalDate());
        }
       if (rs.getTimestamp("fecha_creacion") != null) {
    p.setFechaPublicacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
}
        return p;
    }
}