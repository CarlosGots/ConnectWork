package com.connectwork.util;

import com.connectwork.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT (JSON Web Tokens).
 * El token incluye el ID del usuario, su username y su rol,
 * y se firma con una clave secreta para garantizar su integridad.
 */
public class JWTUtil {

    // Clave secreta (en producción debería estar en una variable de entorno)
    private static final String CLAVE_SECRETA_TEXTO = "ConnectWork2026SecretKeyForJWTTokenGenerationAndValidation";
    private static final SecretKey CLAVE_SECRETA = Keys.hmacShaKeyFor(CLAVE_SECRETA_TEXTO.getBytes());

    // Duración del token: 24 horas
    private static final long DURACION_MS = 24 * 60 * 60 * 1000L;

    /**
     * Genera un token JWT para el usuario indicado.
     */
    public static String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + DURACION_MS);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim("username", usuario.getUsername())
                .claim("rol", usuario.getRol())
                .claim("primeraVez", usuario.isPrimeraVez())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(CLAVE_SECRETA)
                .compact();
    }

    /**
     * Valida un token JWT y devuelve sus claims.
     * @return los claims del token, o null si el token es inválido o expiró
     */
    public static Claims validarToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(CLAVE_SECRETA)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae el ID del usuario desde un token válido.
     */
    public static int extraerIdUsuario(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return -1;
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extrae el rol del usuario desde un token válido.
     */
    public static String extraerRol(String token) {
        Claims claims = validarToken(token);
        if (claims == null) return null;
        return claims.get("rol", String.class);
    }
}