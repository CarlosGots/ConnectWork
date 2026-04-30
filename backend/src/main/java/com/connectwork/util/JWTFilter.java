package com.connectwork.util;

import io.jsonwebtoken.Claims;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtro que valida el token JWT en las rutas protegidas.
 * Extrae el ID y rol del usuario del token y los deja como atributos del request,
 * para que los servlets puedan acceder a ellos sin repetir la lógica.
 */
@WebFilter(filterName = "JWTFilter", urlPatterns = {"/usuarios/*", "/proyectos/*", "/propuestas/*",
                                                     "/contratos/*", "/entregas/*", "/categorias/*",
                                                     "/habilidades/*", "/saldos/*", "/reportes/*",
                                                     "/admin/*"})
public class JWTFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Permitir peticiones OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Extraer el header Authorization
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"error\":\"Token no proporcionado\"}");
            return;
        }

        // Validar el token
        String token = authHeader.substring(7);
        Claims claims = JWTUtil.validarToken(token);

        if (claims == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"error\":\"Token inválido o expirado\"}");
            return;
        }

        // Dejar los datos del usuario disponibles en el request
        httpRequest.setAttribute("idUsuario", Integer.parseInt(claims.getSubject()));
        httpRequest.setAttribute("rol", claims.get("rol", String.class));
        httpRequest.setAttribute("username", claims.get("username", String.class));

        // Continuar con la cadena
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
