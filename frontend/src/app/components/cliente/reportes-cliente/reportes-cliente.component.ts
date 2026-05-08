import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

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
          next: (data: any[]) => { this.historialProyectos.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'historial-recargas':
        this.reporteService.historialRecargas().subscribe({
          next: (data: any[]) => { this.historialRecargas.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'gasto-categorias':
        this.reporteService.gastoCategoriasCliente(fi, ff).subscribe({
          next: (data: any[]) => { this.gastoCategorias.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
    }
  }

  exportarPDF() {
    const doc = new jsPDF();
    const titulo = this.getTitulo();
    doc.setFontSize(16);
    doc.text('ConnectWork - Reporte Cliente', 14, 15);
    doc.setFontSize(12);
    doc.text(titulo, 14, 25);
    doc.setFontSize(9);
    doc.text(`Generado: ${new Date().toLocaleString('es-GT')}`, 14, 32);

    switch (this.reporteActivo()) {
      case 'historial-proyectos':
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['Proyecto', 'Estado', 'Presupuesto', 'Freelancer', 'Fecha']],
          body: this.historialProyectos().map(p => [
            p.titulo, p.estado, 'Q' + p.presupuesto.toFixed(2), p.freelancer || '—', this.formatearFecha(p.fecha)
          ])
        });
        break;
      case 'historial-recargas':
        autoTable(doc, {
          startY: 38,
          head: [['Monto', 'Fecha y hora']],
          body: this.historialRecargas().map(r => [
            'Q' + r.monto.toFixed(2), this.formatearFecha(r.fecha)
          ])
        });
        break;
      case 'gasto-categorias':
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['Categoría', 'Contratos', 'Total gastado']],
          body: this.gastoCategorias().map(g => [
            g.categoria, g.contratos, 'Q' + g.totalGastado.toFixed(2)
          ])
        });
        break;
    }

    doc.save(`reporte-cliente-${this.reporteActivo()}.pdf`);
  }

  private getTitulo(): string {
    switch (this.reporteActivo()) {
      case 'historial-proyectos': return 'Historial de proyectos';
      case 'historial-recargas': return 'Historial de recargas';
      case 'gasto-categorias': return 'Gasto por categoría';
      default: return '';
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}