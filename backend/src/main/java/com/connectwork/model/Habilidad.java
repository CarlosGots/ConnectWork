package com.connectwork.model;

/**
 * Modelo que representa una habilidad específica dentro de una categoría.
 * Ejemplos: Photoshop (Diseño), Angular (Web), SEO (Marketing).
 */
public class Habilidad {

    private int idHabilidad;
    private String nombre;
    private int idCategoria;
    private String nombreCategoria;  // Calculado con JOIN
    private boolean activa;

    public Habilidad() {}

    public Habilidad(String nombre, int idCategoria) {
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.activa = true;
    }

    public int getIdHabilidad() { return idHabilidad; }
    public void setIdHabilidad(int idHabilidad) { this.idHabilidad = idHabilidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}