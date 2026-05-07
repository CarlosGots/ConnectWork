import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SolicitudService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/solicitudes';

  // Admin
  listarTodas(estado?: string): Observable<any[]> {
    const params = estado ? `?estado=${estado}` : '';
    return this.http.get<any[]>(`${this.API_URL}/todas${params}`);
  }

  aceptarCategoria(id: number): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${id}/aceptar-categoria`, {});
  }
  rechazarCategoria(id: number): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${id}/rechazar-categoria`, {});
  }
  aceptarHabilidad(id: number): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${id}/aceptar-habilidad`, {});
  }
  rechazarHabilidad(id: number): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${id}/rechazar-habilidad`, {});
  }

  // Cliente
  solicitarCategoria(nombre: string, descripcion: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/categoria`, { nombre, descripcion });
  }

  // Freelancer
  solicitarHabilidad(nombre: string, idCategoria: number, descripcion: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/habilidad`, { nombre, idCategoria, descripcion });
  }
}