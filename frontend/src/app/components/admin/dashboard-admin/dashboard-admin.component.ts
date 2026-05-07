import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent, MenuLateralComponent],
  templateUrl: './dashboard-admin.component.html',
  styleUrl: './dashboard-admin.component.css'
})
export class DashboardAdminComponent implements OnInit {
  private reporteService = inject(ReporteService);
  data = signal<any>(null);

  ngOnInit() {
    this.reporteService.dashboardAdmin().subscribe({
      next: (d: any) => this.data.set(d),
      error: () => {}
    });
  }
}