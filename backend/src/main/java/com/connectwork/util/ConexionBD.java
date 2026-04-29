package com.connectwork.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para gestionar la conexión a la base de datos MySQL.
 * Maneja el ciclo de vida de las conexiones de manera centralizada.
 */
public class ConexionBD {

    // ============ CONFIGURACIÓN DE CONEXIÓN ============
    private static final String URL = "jdbc:mysql://localhost:3306/connectwork?useSSL=false&serverTimezone=America/Guatemala&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String PASSWORD = ""; 
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Obtiene una nueva conexión a la base de datos.
     * @return Connection activa
     * @throws SQLException si falla la conexión
     */
    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    /**
     * Cierra la conexión a la base de datos de forma segura.
     * @param conexion la conexión a cerrar
     */
    public static void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Prueba la conexión a la base de datos.
     * Útil para verificar que todo está bien configurado.
     */
    public static boolean probarConexion() {
        try (Connection con = obtenerConexion()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }
}