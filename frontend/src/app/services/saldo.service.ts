import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SaldoService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/saldos';

  miSaldo(): Observable<{ saldo: number }> {
    return this.http.get<{ saldo: number }>(`${this.API_URL}/mi-saldo`);
  }

  recargar(monto: number): Observable<{ mensaje: string; montoRecargado: number; saldoActual: number }> {
    return this.http.post<any>(`${this.API_URL}/recargar`, { monto });
  }
}