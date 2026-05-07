import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';

@Component({
  selector: 'app-dashboard-cliente',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent, MenuLateralComponent],
  templateUrl: './dashboard-cliente.component.html',
  styleUrl: './dashboard-cliente.component.css'
})
export class DashboardClienteComponent implements OnInit {
  private reporteService = inject(ReporteService);
  data = signal<any>(null);

  ngOnInit() {
    this.reporteService.dashboardCliente().subscribe({
      next: (d: any) => this.data.set(d),
      error: () => {}
    });
  }
  estadoKeys(): string[] {
  const estados = this.data()?.proyectosPorEstado;
  return estados ? Object.keys(estados) : [];
}
}