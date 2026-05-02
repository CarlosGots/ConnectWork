import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Proyecto {
  idProyecto: number;
  idCliente: number;
  idCategoria: number;
  titulo: string;
  descripcion: string;
  presupuesto: number;
  fechaLimite: string;
  estado: 'ABIERTO' | 'EN_REVISION' | 'EN_PROGRESO' | 'ENTREGA_PENDIENTE' | 'COMPLETADO' | 'CANCELADO';
  fechaCreacion: string;
  nombreCliente?: string;
  nombreCategoria?: string;
  habilidades?: number[];
}

export interface PublicarProyectoRequest {
  titulo: string;
  descripcion: string;
  idCategoria: number;
  presupuesto: number;
  fechaLimite: string;
  habilidades: number[];
}

@Injectable({ providedIn: 'root' })
export class ProyectoService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/proyectos';

  /** Lista proyectos abiertos (para freelancers) */
  listarAbiertos(): Observable<Proyecto[]> {
    return this.http.get<Proyecto[]>(this.API_URL);
  }

  /** Mis proyectos (para el cliente logueado) */
  misProyectos(): Observable<Proyecto[]> {
    return this.http.get<Proyecto[]>(`${this.API_URL}/mis`);
  }

  /** Detalle de un proyecto */
  obtenerPorId(id: number): Observable<Proyecto> {
    return this.http.get<Proyecto>(`${this.API_URL}/${id}`);
  }

  /** Publicar un nuevo proyecto */
  publicar(proyecto: PublicarProyectoRequest): Observable<{ mensaje: string; idProyecto: number }> {
    return this.http.post<{ mensaje: string; idProyecto: number }>(this.API_URL, proyecto);
  }

  /** Cancelar un proyecto */
  cancelar(id: number): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${id}/cancelar`, {});
  }
}