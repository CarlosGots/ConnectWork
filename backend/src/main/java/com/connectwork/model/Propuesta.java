package com.connectwork.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una propuesta enviada por un freelancer a un proyecto.
 * Estados: PENDIENTE, ACEPTADA, RECHAZADA, RETIRADA
 */
public class Propuesta {

    private int idPropuesta;
    private int idProyecto;
    private int idFreelancer;
    private double montoOfertado;
    private int plazoDias;
    private String cartaPresentacion;
    private String estado;
    private LocalDateTime fechaEnvio;

    // Calculados con JOIN
    private String nombreFreelancer;
    private String tituloProyecto;

    public Propuesta() {}

    public int getIdPropuesta() { return idPropuesta; }
    public void setIdPropuesta(int idPropuesta) { this.idPropuesta = idPropuesta; }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public int getIdFreelancer() { return idFreelancer; }
    public void setIdFreelancer(int idFreelancer) { this.idFreelancer = idFreelancer; }

    public double getMontoOfertado() { return montoOfertado; }
    public void setMontoOfertado(double montoOfertado) { this.montoOfertado = montoOfertado; }

    public int getPlazoDias() { return plazoDias; }
    public void setPlazoDias(int plazoDias) { this.plazoDias = plazoDias; }

    public String getCartaPresentacion() { return cartaPresentacion; }
    public void setCartaPresentacion(String cartaPresentacion) { this.cartaPresentacion = cartaPresentacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getNombreFreelancer() { return nombreFreelancer; }
    public void setNombreFreelancer(String nombreFreelancer) { this.nombreFreelancer = nombreFreelancer; }

    public String getTituloProyecto() { return tituloProyecto; }
    public void setTituloProyecto(String tituloProyecto) { this.tituloProyecto = tituloProyecto; }
}