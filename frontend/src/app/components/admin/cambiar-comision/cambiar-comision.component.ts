import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ComisionService, Comision } from '../../../services/comision.service';

@Component({
  selector: 'app-cambiar-comision',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './cambiar-comision.component.html',
  styleUrl: './cambiar-comision.component.css'
})
export class CambiarComisionComponent implements OnInit {

  private comisionService = inject(ComisionService);

  comisionActiva = signal<Comision | null>(null);
  historial = signal<Comision[]>([]);
  cargando = signal<boolean>(false);
  guardando = signal<boolean>(false);

  nuevoPorcentaje = signal<number | null>(null);
  mensajeError = signal<string>('');
  mensajeExito = signal<string>('');

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);

    this.comisionService.obtenerActiva().subscribe({
      next: (comision) => {
        this.comisionActiva.set(comision);
        this.nuevoPorcentaje.set(comision.porcentaje);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudo cargar la comisión actual.');
        this.cargando.set(false);
      }
    });

    this.comisionService.listarHistorial().subscribe({
      next: (lista) => this.historial.set(lista),
      error: () => {}
    });
  }

  guardar(): void {
    const porcentaje = this.nuevoPorcentaje();

    if (porcentaje === null || isNaN(porcentaje)) {
      this.mensajeError.set('Ingresa un porcentaje válido.');
      return;
    }
    if (porcentaje < 0 || porcentaje > 100) {
      this.mensajeError.set('El porcentaje debe estar entre 0 y 100.');
      return;
    }
    if (porcentaje === this.comisionActiva()?.porcentaje) {
      this.mensajeError.set('El porcentaje es igual al actual.');
      return;
    }

    this.guardando.set(true);
    this.mensajeError.set('');

    this.comisionService.cambiarComision(porcentaje).subscribe({
      next: () => {
        this.guardando.set(false);
        this.mensajeExito.set(`Comisión actualizada a ${porcentaje}% exitosamente.`);
        setTimeout(() => this.mensajeExito.set(''), 4000);
        this.cargarDatos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(err.error?.error || 'Error al actualizar la comisión.');
      }
    });
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}