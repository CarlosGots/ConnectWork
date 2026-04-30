import { Component } from '@angular/core';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';

@Component({
  selector: 'app-dashboard-freelancer',
  standalone: true,
  imports: [HeaderComponent, MenuLateralComponent],
  templateUrl: './dashboard-freelancer.component.html',
  styleUrl: './dashboard-freelancer.component.css'
})
export class DashboardFreelancerComponent {}