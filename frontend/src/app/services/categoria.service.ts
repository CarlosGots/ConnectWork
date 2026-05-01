import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Modelo de Categoría tal como lo devuelve el backend.
 */
export interface Categoria {
  idCategoria: number;
  nombre: string;
  descripcion: string;
  activa: boolean;
  fechaCreacion?: string;
  totalHabilidades?: number;
}

/**
 * Servicio para gestionar el CRUD de categorías.
 * Solo el admin puede crear, actualizar y cambiar estado.
 * Cualquier usuario autenticado puede listar.
 */
@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/categorias';

  /** Lista todas las categorías (incluye totalHabilidades). */
  listarTodas(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.API_URL);
  }

  /** Lista solo las categorías activas (útil para selectores). */
  listarActivas(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.API_URL}/activas`);
  }

  /** Crea una nueva categoría. */
  crear(categoria: { nombre: string; descripcion: string }): Observable<Categoria> {
    return this.http.post<Categoria>(this.API_URL, categoria);
  }

  /** Actualiza una categoría existente. */
  actualizar(id: number, categoria: { nombre: string; descripcion: string; activa: boolean }): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${id}`, categoria);
  }

  /** Activa o desactiva una categoría (soft toggle). */
  cambiarEstado(id: number, activa: boolean): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${id}/estado`, { activa });
  }
}