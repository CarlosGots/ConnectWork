import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReporteService {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/reportes';

  // Admin
  historialComisiones(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/admin/historial-comisiones`);
  }
  topFreelancers(fi: string, ff: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/admin/top-freelancers?fechaInicio=${fi}&fechaFin=${ff}`);
  }
  topCategoriasAdmin(fi: string, ff: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/admin/top-categorias?fechaInicio=${fi}&fechaFin=${ff}`);
  }
  ingresosPlataforma(fi: string, ff: string): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/admin/ingresos-plataforma?fechaInicio=${fi}&fechaFin=${ff}`);
  }

  // Cliente
  historialProyectos(fi: string, ff: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/cliente/historial-proyectos?fechaInicio=${fi}&fechaFin=${ff}`);
  }
  historialRecargas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/cliente/historial-recargas`);
  }
  gastoCategoriasCliente(fi: string, ff: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/cliente/gasto-categorias?fechaInicio=${fi}&fechaFin=${ff}`);
  }

  // Freelancer
  saldoFreelancer(): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/freelancer/saldo`);
  }
  historialContratos(fi: string, ff: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/freelancer/historial-contratos?fechaInicio=${fi}&fechaFin=${ff}`);
  }
  topCategoriasFreelancer(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/freelancer/top-categorias`);
  }
  reportePropuestas(fi: string, ff: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/freelancer/propuestas?fechaInicio=${fi}&fechaFin=${ff}`);
  }
  dashboardAdmin(): Observable<any> {
  return this.http.get<any>(`${this.API_URL}/dashboard/admin`);
}
dashboardCliente(): Observable<any> {
  return this.http.get<any>(`${this.API_URL}/dashboard/cliente`);
}
dashboardFreelancer(): Observable<any> {
  return this.http.get<any>(`${this.API_URL}/dashboard/freelancer`);
}
}