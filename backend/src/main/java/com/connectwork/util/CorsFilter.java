package com.connectwork.util;

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
 * Filtro CORS para permitir que el frontend Angular consuma la API REST.
 * Se aplica a todas las peticiones HTTP de la aplicación.
 */
@WebFilter(filterName = "CorsFilter", urlPatterns = {"/*"})
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No requiere inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Permitir el origen del frontend Angular
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");

        // Métodos HTTP permitidos
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // Headers permitidos en las peticiones (incluido Authorization para JWT)
        httpResponse.setHeader("Access-Control-Allow-Headers",
            "Content-Type, Authorization, X-Requested-With, Accept");

        // Permitir el envío de credenciales (cookies, auth headers)
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // Tiempo de cache de la respuesta preflight (1 hora)
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Manejar peticiones preflight OPTIONS (las que el navegador envía antes del POST/PUT/DELETE real)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Continuar con la cadena de filtros y ejecutar el servlet correspondiente
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No requiere limpieza
    }
}