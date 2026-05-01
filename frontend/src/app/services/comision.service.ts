import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comision {
  idComision: number;
  porcentaje: number;
  fechaInicio: string;
  activa: boolean;
  nombreAdminCambio?: string;
}

@Injectable({ providedIn: 'root' })
export class ComisionService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/admin/comision';

  obtenerActiva(): Observable<Comision> {
    return this.http.get<Comision>(this.API_URL);
  }

  listarHistorial(): Observable<Comision[]> {
    return this.http.get<Comision[]>(`${this.API_URL}/historial`);
  }

  cambiarComision(porcentaje: number): Observable<{ mensaje: string; nuevoPorcentaje: number }> {
    return this.http.put<{ mensaje: string; nuevoPorcentaje: number }>(this.API_URL, { porcentaje });
  }
}