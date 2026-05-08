import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

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
          next: (data: any) => { this.saldo.set(data.saldo); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'historial-contratos':
        this.reporteService.historialContratos(fi, ff).subscribe({
          next: (data: any[]) => { this.historialContratos.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'top-categorias':
        this.reporteService.topCategoriasFreelancer().subscribe({
          next: (data: any[]) => { this.topCategorias.set(data); this.cargando.set(false); },
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

  exportarPDF() {
    const doc = new jsPDF();
    const titulo = this.getTitulo();
    doc.setFontSize(16);
    doc.text('ConnectWork - Reporte Freelancer', 14, 15);
    doc.setFontSize(12);
    doc.text(titulo, 14, 25);
    doc.setFontSize(9);
    doc.text(`Generado: ${new Date().toLocaleString('es-GT')}`, 14, 32);

    switch (this.reporteActivo()) {
      case 'saldo':
        autoTable(doc, {
          startY: 38,
          head: [['Métrica', 'Valor']],
          body: [['Saldo disponible', 'Q' + this.saldo().toFixed(2)]]
        });
        break;
      case 'historial-contratos':
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['Proyecto', 'Cliente', 'Monto recibido', 'Calificación', 'Fecha']],
          body: this.historialContratos().map(c => [
            c.proyecto, c.cliente, 'Q' + c.montoRecibido.toFixed(2),
            c.estrellas ? c.estrellas + '/5' : 'Sin calif.', this.formatearFecha(c.fechaCierre)
          ])
        });
        break;
      case 'top-categorias':
        autoTable(doc, {
          startY: 38,
          head: [['#', 'Categoría', 'Contratos', 'Ingresos']],
          body: this.topCategorias().map((c, i) => [
            i + 1, c.categoria, c.contratos, 'Q' + c.ingresos.toFixed(2)
          ])
        });
        break;
      case 'propuestas':
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['Proyecto', 'Monto ofertado', 'Estado', 'Fecha']],
          body: this.propuestas().map(p => [
            p.proyecto, 'Q' + p.montoOfertado.toFixed(2), p.estado, this.formatearFecha(p.fechaEnvio)
          ])
        });
        break;
    }

    doc.save(`reporte-freelancer-${this.reporteActivo()}.pdf`);
  }

  private getTitulo(): string {
    switch (this.reporteActivo()) {
      case 'saldo': return 'Saldo actual';
      case 'historial-contratos': return 'Historial de contratos completados';
      case 'top-categorias': return 'Top 5 categorías trabajadas';
      case 'propuestas': return 'Propuestas enviadas';
      default: return '';
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}