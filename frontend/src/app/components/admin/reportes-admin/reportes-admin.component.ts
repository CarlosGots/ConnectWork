import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';

@Component({
  selector: 'app-reportes-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './reportes-admin.component.html',
  styleUrl: './reportes-admin.component.css'
})
export class ReportesAdminComponent implements OnInit {

  private reporteService = inject(ReporteService);

  reporteActivo = signal('historial-comisiones');
  cargando = signal(false);
  fechaInicio = signal('2026-01-01');
  fechaFin = signal('2026-12-31');

  historialComisiones = signal<any[]>([]);
  topFreelancers = signal<any[]>([]);
  topCategorias = signal<any[]>([]);
  ingresosPlataforma = signal<any>(null);

  ngOnInit() {
    this.cargarReporte();
  }

  cambiarReporte(reporte: string) {
    this.reporteActivo.set(reporte);
    this.cargarReporte();
  }

  cargarReporte() {
    this.cargando.set(true);
    const fi = this.fechaInicio();
    const ff = this.fechaFin();

    switch (this.reporteActivo()) {
      case 'historial-comisiones':
        this.reporteService.historialComisiones().subscribe({
          next: (data) => { this.historialComisiones.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'top-freelancers':
        this.reporteService.topFreelancers(fi, ff).subscribe({
          next: (data) => { this.topFreelancers.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'top-categorias':
        this.reporteService.topCategoriasAdmin(fi, ff).subscribe({
          next: (data) => { this.topCategorias.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'ingresos-plataforma':
        this.reporteService.ingresosPlataforma(fi, ff).subscribe({
          next: (data) => { this.ingresosPlataforma.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}