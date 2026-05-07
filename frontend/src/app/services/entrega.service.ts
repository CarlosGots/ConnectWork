import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Entrega {
  idEntrega: number;
  idContrato: number;
  descripcion: string;
  archivosUrl: string;
  estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
  motivoRechazo?: string;
  fechaEntrega: string;
}

@Injectable({ providedIn: 'root' })
export class EntregaService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/entregas';

  subir(descripcion: string, archivosUrl: string): Observable<{ mensaje: string; idEntrega: number }> {
    return this.http.post<any>(this.API_URL, { descripcion, archivosUrl });
  }

  aprobar(idEntrega: number): Observable<{ mensaje: string }> {
    return this.http.put<any>(`${this.API_URL}/${idEntrega}/aprobar`, {});
  }

  rechazar(idEntrega: number, motivo: string): Observable<{ mensaje: string }> {
    return this.http.put<any>(`${this.API_URL}/${idEntrega}/rechazar`, { motivo });
  }

  obtenerMisContratos(): Observable<any[]> {
  return this.http.get<any[]>(`${this.API_URL}/mis-contratos`);
}

obtenerEntregasContrato(idContrato: number): Observable<Entrega[]> {
  return this.http.get<Entrega[]>(`${this.API_URL}/contrato/${idContrato}`);
}
obtenerMisEntregas(): Observable<any[]> {
  return this.http.get<any[]>(`${this.API_URL}/mis-entregas`);
}
cancelarContrato(idContrato: number, motivo: string): Observable<any> {
  return this.http.put<any>(`${this.API_URL}/${idContrato}/cancelar-contrato`, { motivo });
}
}