import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import {
  Usuario,
  LoginRequest,
  LoginResponse,
  RegistroResponse
} from '../models/usuario.model';

/**
 * Servicio de autenticación.
 * Centraliza todas las operaciones relacionadas con login, registro y sesión.
 * Mantiene el estado del usuario actual usando signals (reactivo).
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  // URL base del backend (luego la sacaremos a un archivo de configuración)
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/auth';

  // Claves usadas para guardar datos en localStorage
  private readonly TOKEN_KEY = 'connectwork_token';
  private readonly USUARIO_KEY = 'connectwork_usuario';

  // Estado del usuario actual (signal reactivo)
  private usuarioActualSignal = signal<LoginResponse | null>(this.cargarUsuarioGuardado());

  // Signal computado: true si hay sesión iniciada
  estaAutenticado = computed(() => this.usuarioActualSignal() !== null);

  // Signal computado: rol del usuario actual (o null si no hay sesión)
  rolActual = computed(() => this.usuarioActualSignal()?.rol ?? null);

  // Signal computado: nombre del usuario actual
  nombreActual = computed(() => this.usuarioActualSignal()?.nombreCompleto ?? '');

  // ============================================================
  // OPERACIONES DE AUTENTICACIÓN
  // ============================================================

  /**
   * Registra un nuevo usuario (cliente o freelancer) en el sistema.
   */
  registrar(usuario: Usuario): Observable<RegistroResponse> {
    return this.http.post<RegistroResponse>(`${this.API_URL}/registro`, usuario);
  }

  /**
   * Inicia sesión con username y password.
   * Si es exitoso, guarda el token y los datos del usuario.
   */
  login(credenciales: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credenciales).pipe(
      tap(respuesta => this.guardarSesion(respuesta))
    );
  }

  /**
   * Cierra la sesión actual: borra el token y limpia el estado.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USUARIO_KEY);
    this.usuarioActualSignal.set(null);
  }

  // ============================================================
  // ACCESO A DATOS DE LA SESIÓN
  // ============================================================

  /**
   * Obtiene el token JWT actual (o null si no hay sesión).
   */
  obtenerToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Obtiene los datos del usuario actual.
   */
  obtenerUsuarioActual(): LoginResponse | null {
    return this.usuarioActualSignal();
  }

  // ============================================================
  // PRIVADOS
  // ============================================================

  /**
   * Guarda la sesión en localStorage y actualiza el signal.
   */
  private guardarSesion(respuesta: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, respuesta.token);
    localStorage.setItem(this.USUARIO_KEY, JSON.stringify(respuesta));
    this.usuarioActualSignal.set(respuesta);
  }

  /**
   * Carga el usuario guardado en localStorage al iniciar la app.
   * Útil para mantener la sesión cuando se refresca la página.
   */
  private cargarUsuarioGuardado(): LoginResponse | null {
    const usuarioGuardado = localStorage.getItem(this.USUARIO_KEY);
    if (!usuarioGuardado) return null;

    try {
      return JSON.parse(usuarioGuardado);
    } catch {
      return null;
    }
  }
}