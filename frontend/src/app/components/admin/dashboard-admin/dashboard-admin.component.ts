import { Component } from '@angular/core';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [HeaderComponent, MenuLateralComponent],
  templateUrl: './dashboard-admin.component.html',
  styleUrl: './dashboard-admin.component.css'
})
export class DashboardAdminComponent {}