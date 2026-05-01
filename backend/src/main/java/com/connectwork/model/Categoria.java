
package com.connectwork.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una categoría de servicios profesionales.
 * Ejemplos: Diseño Gráfico, Desarrollo Web, Marketing Digital.
 */
public class Categoria {

    private int idCategoria;
    private String nombre;
    private String descripcion;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    private int totalHabilidades;  // Calculado, NO está en la tabla

    public Categoria() {}

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activa = true;
    }

    // ============ GETTERS Y SETTERS ============
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getTotalHabilidades() { return totalHabilidades; }
    public void setTotalHabilidades(int totalHabilidades) { this.totalHabilidades = totalHabilidades; }
}