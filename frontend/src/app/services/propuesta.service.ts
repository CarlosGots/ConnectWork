import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Propuesta {
  idPropuesta: number;
  idProyecto: number;
  idFreelancer: number;
  montoOfertado: number;
  plazoDias: number;
  cartaPresentacion: string;
  estado: 'PENDIENTE' | 'ACEPTADA' | 'RECHAZADA' | 'RETIRADA';
  fechaEnvio: string;
  nombreFreelancer?: string;
  tituloProyecto?: string;
}

export interface EnviarPropuestaRequest {
  idProyecto: number;
  montoOfertado: number;
  plazoDias: number;
  cartaPresentacion: string;
}

@Injectable({ providedIn: 'root' })
export class PropuestaService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/propuestas';

  listarPorProyecto(idProyecto: number): Observable<Propuesta[]> {
    return this.http.get<Propuesta[]>(`${this.API_URL}/proyecto/${idProyecto}`);
  }

  misPropuestas(): Observable<Propuesta[]> {
    return this.http.get<Propuesta[]>(`${this.API_URL}/mis`);
  }

  enviar(propuesta: EnviarPropuestaRequest): Observable<{ mensaje: string; idPropuesta: number }> {
    return this.http.post<{ mensaje: string; idPropuesta: number }>(this.API_URL, propuesta);
  }

  aceptar(idPropuesta: number): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${idPropuesta}/aceptar`, {});
  }

  rechazar(idPropuesta: number): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.API_URL}/${idPropuesta}/rechazar`, {});
  }
  retirar(idPropuesta: number): Observable<{ mensaje: string }> {
  return this.http.put<{ mensaje: string }>(`${this.API_URL}/${idPropuesta}/retirar`, {});
}
}