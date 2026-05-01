package com.connectwork.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa la comisión vigente de la plataforma.
 * Se guarda como porcentaje (ej. 10.5 = 10.5%).
 */
public class Comision {

    private int idComision;
    private double porcentaje;
    private LocalDateTime fechaInicio;
    private boolean activa;
    private Integer idAdminCambio;       // quién la cambió
    private String nombreAdminCambio;    // calculado con JOIN

    public Comision() {}

    public int getIdComision() { return idComision; }
    public void setIdComision(int idComision) { this.idComision = idComision; }

    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public Integer getIdAdminCambio() { return idAdminCambio; }
    public void setIdAdminCambio(Integer idAdminCambio) { this.idAdminCambio = idAdminCambio; }

    public String getNombreAdminCambio() { return nombreAdminCambio; }
    public void setNombreAdminCambio(String nombreAdminCambio) { this.nombreAdminCambio = nombreAdminCambio; }
}