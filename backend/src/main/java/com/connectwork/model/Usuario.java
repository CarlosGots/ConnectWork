package com.connectwork.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase modelo que representa un usuario de la plataforma.
 * Contiene los datos comunes a Cliente, Freelancer y Administrador.
 */
public class Usuario {
    
    private int idUsuario;
    private String nombreCompleto;
    private String username;
    private String password;
    private String email;
    private String telefono;
    private String direccion;
    private String cui;
    private LocalDate fechaNacimiento;
    private String rol;            // CLIENTE, FREELANCER, ADMINISTRADOR
    private double saldo;
    private boolean activo;
    private boolean primeraVez;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public Usuario() {}

    // Constructor para registro (sin id, sin saldo, sin fechas)
    public Usuario(String nombreCompleto, String username, String password, String email,
                   String telefono, String direccion, String cui, LocalDate fechaNacimiento, String rol) {
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.password = password;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.cui = cui;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.saldo = 0.0;
        this.activo = true;
        this.primeraVez = true;
    }

    // ============ GETTERS Y SETTERS ============
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isPrimeraVez() { return primeraVez; }
    public void setPrimeraVez(boolean primeraVez) { this.primeraVez = primeraVez; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}