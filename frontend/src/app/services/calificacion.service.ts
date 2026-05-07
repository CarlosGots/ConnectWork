import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class CalificacionService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/connectwork-backend/calificaciones';

  obtenerPendientes() {
    return this.http.get<any[]>(`${this.apiUrl}/pendientes`);
  }

  calificar(idContrato: number, estrellas: number, comentario: string) {
    return this.http.post<any>(this.apiUrl, { idContrato, estrellas, comentario });
  }
}