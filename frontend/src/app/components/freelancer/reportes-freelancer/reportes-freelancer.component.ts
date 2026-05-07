import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';

@Component({
  selector: 'app-reportes-freelancer',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './reportes-freelancer.component.html',
  styleUrl: './reportes-freelancer.component.css'
})
export class ReportesFreelancerComponent implements OnInit {

  private reporteService = inject(ReporteService);

  reporteActivo = signal('saldo');
  cargando = signal(false);
  fechaInicio = signal('2026-01-01');
  fechaFin = signal('2026-12-31');

  saldo = signal(0);
  historialContratos = signal<any[]>([]);
  topCategorias = signal<any[]>([]);
  propuestas = signal<any[]>([]);

  ngOnInit() { this.cargarReporte(); }

  cambiarReporte(reporte: string) {
    this.reporteActivo.set(reporte);
    this.cargarReporte();
  }

  cargarReporte() {
    this.cargando.set(true);
    const fi = this.fechaInicio();
    const ff = this.fechaFin();

    switch (this.reporteActivo()) {
      case 'saldo':
        this.reporteService.saldoFreelancer().subscribe({
          next: (data) => { this.saldo.set(data.saldo); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'historial-contratos':
        this.reporteService.historialContratos(fi, ff).subscribe({
          next: (data) => { this.historialContratos.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'top-categorias':
        this.reporteService.topCategoriasFreelancer().subscribe({
          next: (data) => { this.topCategorias.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'propuestas':
        this.reporteService.reportePropuestas(fi, ff).subscribe({
          next: (data: any[]) => { this.propuestas.set(data); this.cargando.set(false); },
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