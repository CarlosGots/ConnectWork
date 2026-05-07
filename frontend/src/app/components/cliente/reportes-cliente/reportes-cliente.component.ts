import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';

@Component({
  selector: 'app-reportes-cliente',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './reportes-cliente.component.html',
  styleUrl: './reportes-cliente.component.css'
})
export class ReportesClienteComponent implements OnInit {

  private reporteService = inject(ReporteService);

  reporteActivo = signal('historial-proyectos');
  cargando = signal(false);
  fechaInicio = signal('2026-01-01');
  fechaFin = signal('2026-12-31');

  historialProyectos = signal<any[]>([]);
  historialRecargas = signal<any[]>([]);
  gastoCategorias = signal<any[]>([]);

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
      case 'historial-proyectos':
        this.reporteService.historialProyectos(fi, ff).subscribe({
          next: (data) => { this.historialProyectos.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'historial-recargas':
        this.reporteService.historialRecargas().subscribe({
          next: (data) => { this.historialRecargas.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'gasto-categorias':
        this.reporteService.gastoCategoriasCliente(fi, ff).subscribe({
          next: (data) => { this.gastoCategorias.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}