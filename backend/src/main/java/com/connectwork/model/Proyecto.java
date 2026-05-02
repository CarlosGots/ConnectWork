package com.connectwork.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa un proyecto publicado por un cliente.
 * Estados posibles: ABIERTO, EN_PROGRESO, COMPLETADO, CANCELADO
 */
public class Proyecto {

    private int idProyecto;
    private int idCliente;
    private int idCategoria;
    private String titulo;
    private String descripcion;
    private double presupuesto;
    private LocalDate fechaLimite;
    private String estado;
    private LocalDateTime fechaPublicacion;

    // Campos calculados con JOIN
    private String nombreCliente;
    private String nombreCategoria;

    public Proyecto() {}

    // ============ GETTERS Y SETTERS ============
    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(double presupuesto) { this.presupuesto = presupuesto; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
}