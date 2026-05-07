import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { SaldoService } from '../../../services/saldo.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-recargar-saldo',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './recargar-saldo.component.html',
  styleUrl: './recargar-saldo.component.css'
})
export class RecargarSaldoComponent {

  private saldoService = inject(SaldoService);
  private authService = inject(AuthService);

  monto = signal<number | null>(null);
  cargando = signal<boolean>(false);
  mensajeExito = signal<string>('');
  mensajeError = signal<string>('');
  saldoActual = signal<number | null>(null);

  constructor() {
    this.cargarSaldo();
  }

  cargarSaldo(): void {
    this.saldoService.miSaldo().subscribe({
      next: (resp) => this.saldoActual.set(resp.saldo),
      error: () => {}
    });
  }

  recargar(): void {
    if (!this.monto() || this.monto()! <= 0) {
      this.mensajeError.set('Ingresa un monto válido mayor a Q0.'); return;
    }
    if (this.monto()! > 50000) {
      this.mensajeError.set('El máximo por recarga es Q50,000.'); return;
    }

    this.cargando.set(true);
    this.mensajeError.set('');

    this.saldoService.recargar(this.monto()!).subscribe({
      next: (resp) => {
        this.cargando.set(false);
        this.saldoActual.set(resp.saldoActual);
        this.monto.set(null);
        this.mensajeExito.set(`¡Recarga exitosa! Tu nuevo saldo es Q${resp.saldoActual.toFixed(2)}`);
        setTimeout(() => this.mensajeExito.set(''), 5000);
      },
      error: (err) => {
        this.cargando.set(false);
        this.mensajeError.set(err.error?.error || 'Error al procesar la recarga.');
      }
    });
  }
}