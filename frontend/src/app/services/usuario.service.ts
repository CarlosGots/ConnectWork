import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Datos para completar la información inicial del Cliente.
 */
export interface InfoClienteRequest {
  descripcionEmpresa: string;
  sector: string;
  sitioWeb?: string;
}

/**
 * Datos para completar la información inicial del Freelancer.
 */
export interface InfoFreelancerRequest {
  biografia: string;
  nivelExperiencia: 'JUNIOR' | 'SEMI_SENIOR' | 'SENIOR';
  tarifaHora: number;
  habilidades: number[];
}

/**
 * Respuesta genérica del backend al completar info.
 */
export interface InfoResponse {
  mensaje: string;
  primeraVez: boolean;
}

/**
 * Servicio para operaciones del usuario autenticado.
 * Centraliza llamadas al backend relacionadas con el perfil del usuario.
 */
@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/usuarios';

  /**
   * Completa o actualiza la información inicial del usuario autenticado.
   * El backend identifica al usuario por el token JWT.
   * El cuerpo varía según el rol (cliente o freelancer).
   */
  completarInfo(datos: InfoClienteRequest | InfoFreelancerRequest): Observable<InfoResponse> {
    return this.http.put<InfoResponse>(`${this.API_URL}/info`, datos);
  }
}