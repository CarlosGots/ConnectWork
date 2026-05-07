package com.connectwork.model;

import java.time.LocalDateTime;

public class Calificacion {
    private int idCalificacion;
    private int idContrato;
    private int estrellas;
    private String comentario;
    private LocalDateTime fecha;

    // Campos JOIN
    private String nombreFreelancer;
    private String tituloProyecto;

    public Calificacion() {}

    public int getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(int idCalificacion) { this.idCalificacion = idCalificacion; }

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    public int getEstrellas() { return estrellas; }
    public void setEstrellas(int estrellas) { this.estrellas = estrellas; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getNombreFreelancer() { return nombreFreelancer; }
    public void setNombreFreelancer(String nombreFreelancer) { this.nombreFreelancer = nombreFreelancer; }

    public String getTituloProyecto() { return tituloProyecto; }
    public void setTituloProyecto(String tituloProyecto) { this.tituloProyecto = tituloProyecto; }
}