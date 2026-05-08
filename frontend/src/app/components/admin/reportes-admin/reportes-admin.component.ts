import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

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
      case 'historial-comisiones':
        this.reporteService.historialComisiones().subscribe({
          next: (data: any[]) => { this.historialComisiones.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'top-freelancers':
        this.reporteService.topFreelancers(fi, ff).subscribe({
          next: (data: any[]) => { this.topFreelancers.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'top-categorias':
        this.reporteService.topCategoriasAdmin(fi, ff).subscribe({
          next: (data: any[]) => { this.topCategorias.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
      case 'ingresos-plataforma':
        this.reporteService.ingresosPlataforma(fi, ff).subscribe({
          next: (data: any) => { this.ingresosPlataforma.set(data); this.cargando.set(false); },
          error: () => this.cargando.set(false)
        });
        break;
    }
  }

  exportarPDF() {
    const doc = new jsPDF();
    const titulo = this.getTitulo();
    doc.setFontSize(16);
    doc.text('ConnectWork - Reporte Administrador', 14, 15);
    doc.setFontSize(12);
    doc.text(titulo, 14, 25);
    doc.setFontSize(9);
    doc.text(`Generado: ${new Date().toLocaleString('es-GT')}`, 14, 32);

    switch (this.reporteActivo()) {
      case 'historial-comisiones':
        autoTable(doc, {
          startY: 38,
          head: [['Porcentaje', 'Fecha inicio', 'Fecha fin']],
          body: this.historialComisiones().map(c => [
            c.porcentaje + '%',
            this.formatearFecha(c.fechaInicio),
            c.fechaFin ? this.formatearFecha(c.fechaFin) : 'Vigente'
          ])
        });
        break;
      case 'top-freelancers':
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['#', 'Freelancer', 'Contratos', 'Total ganado', 'Comisión plataforma']],
          body: this.topFreelancers().map((f, i) => [
            i + 1, f.nombre, f.contratos, 'Q' + f.totalGanado.toFixed(2), 'Q' + f.comisionPlataforma.toFixed(2)
          ])
        });
        break;
      case 'top-categorias':
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['#', 'Categoría', 'Contratos', 'Comisiones']],
          body: this.topCategorias().map((c, i) => [
            i + 1, c.categoria, c.contratos, 'Q' + c.comisiones.toFixed(2)
          ])
        });
        break;
      case 'ingresos-plataforma':
        const ing = this.ingresosPlataforma();
        doc.text(`Período: ${this.fechaInicio()} a ${this.fechaFin()}`, 14, 38);
        autoTable(doc, {
          startY: 44,
          head: [['Métrica', 'Valor']],
          body: [
            ['Contratos completados', ing?.contratos || 0],
            ['Total comisiones', 'Q' + (ing?.totalComisiones || 0).toFixed(2)]
          ]
        });
        break;
    }

    doc.save(`reporte-admin-${this.reporteActivo()}.pdf`);
  }

  private getTitulo(): string {
    switch (this.reporteActivo()) {
      case 'historial-comisiones': return 'Historial de porcentajes de comisión';
      case 'top-freelancers': return 'Top 5 freelancers con más ingresos';
      case 'top-categorias': return 'Top 5 categorías con más actividad';
      case 'ingresos-plataforma': return 'Total de ingresos de la plataforma';
      default: return '';
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}