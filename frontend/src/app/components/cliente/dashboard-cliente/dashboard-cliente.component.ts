import { Component } from '@angular/core';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';

@Component({
  selector: 'app-dashboard-cliente',
  standalone: true,
  imports: [HeaderComponent, MenuLateralComponent],
  templateUrl: './dashboard-cliente.component.html',
  styleUrl: './dashboard-cliente.component.css'
})
export class DashboardClienteComponent {}