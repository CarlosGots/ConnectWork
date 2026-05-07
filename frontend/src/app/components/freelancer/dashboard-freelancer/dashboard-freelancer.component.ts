import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ReporteService } from '../../../services/reporte.service';

@Component({
  selector: 'app-dashboard-freelancer',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent, MenuLateralComponent],
  templateUrl: './dashboard-freelancer.component.html',
  styleUrl: './dashboard-freelancer.component.css'
})
export class DashboardFreelancerComponent implements OnInit {
  private reporteService = inject(ReporteService);
  data = signal<any>(null);

  ngOnInit() {
    this.reporteService.dashboardFreelancer().subscribe({
      next: (d: any) => this.data.set(d),
      error: () => {}
    });
  }
}