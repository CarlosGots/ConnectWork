import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Modelo de Habilidad tal como lo devuelve el backend.
 */
export interface Habilidad {
  idHabilidad: number;
  nombre: string;
  idCategoria: number;
  nombreCategoria?: string;
  activa: boolean;
}

/**
 * Servicio para gestionar el CRUD de habilidades.
 */
@Injectable({
  providedIn: 'root'
})
export class HabilidadService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/habilidades';

  /** Lista todas las habilidades con el nombre de su categoría. */
  listarTodas(): Observable<Habilidad[]> {
    return this.http.get<Habilidad[]>(this.API_URL);
  }

  /** Lista habilidades activas filtradas por categoría. */
  listarPorCategoria(idCategoria: number): Observable<Habilidad[]> {
    return this.http.get<Habilidad[]>(`${this.API_URL}/categoria/${idCategoria}`);
  }

  /** Crea una nueva habilidad. */
  crear(habilidad: { nombre: string; idCategoria: number }): Observable<Habilidad> {
    return this.http.post<Habilidad>(this.API_URL, habilidad);
  }

  /** Actualiza una habilidad existente. */
  actualizar(id: number, habilidad: { nombre: string; idCategoria: number; activa: boolean }): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${id}`, habilidad);
  }

  /** Activa o desactiva una habilidad. */
  cambiarEstado(id: number, activa: boolean): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${id}/estado`, { activa });
  }
}