package com.connectwork.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para encriptar y validar contraseñas usando BCrypt.
 * BCrypt aplica un hash de un solo sentido con un "salt" automático,
 * por lo que la misma contraseña genera hashes distintos cada vez.
 */
public class PasswordUtil {

    private static final int FACTOR_TRABAJO = 12;

    /**
     * Encripta una contraseña en texto plano.
     * @param passwordPlano la contraseña sin encriptar
     * @return el hash BCrypt de la contraseña
     */
    public static String encriptar(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(FACTOR_TRABAJO));
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt.
     * @param passwordPlano la contraseña que el usuario ingresó
     * @param hashAlmacenado el hash guardado en la base de datos
     * @return true si coinciden, false en caso contrario
     */
    public static boolean validar(String passwordPlano, String hashAlmacenado) {
        try {
            return BCrypt.checkpw(passwordPlano, hashAlmacenado);
        } catch (Exception e) {
            return false;
        }
    }
}