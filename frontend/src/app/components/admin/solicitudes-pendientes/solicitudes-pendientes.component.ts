import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { SolicitudService } from '../../../services/solicitud.service';

@Component({
  selector: 'app-solicitudes-pendientes',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './solicitudes-pendientes.component.html',
  styleUrl: './solicitudes-pendientes.component.css'
})
export class SolicitudesPendientesComponent implements OnInit {

  private solicitudService = inject(SolicitudService);

  solicitudes = signal<any[]>([]);
  cargando = signal(true);
  filtro = signal('PENDIENTE');
  mensaje = signal('');
  mensajeError = signal('');
  procesando = signal<number | null>(null);

  ngOnInit() { this.cargar(); }

  cargar() {
    this.cargando.set(true);
    const estado = this.filtro() === 'TODAS' ? undefined : this.filtro();
    this.solicitudService.listarTodas(estado).subscribe({
      next: (data: any[]) => { this.solicitudes.set(data); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.mensajeError.set('Error al cargar solicitudes'); }
    });
  }

  cambiarFiltro(f: string) { this.filtro.set(f); this.cargar(); }

  aceptar(s: any) {
    this.procesando.set(s.idSolicitud);
    const obs = s.tipo === 'CATEGORIA'
      ? this.solicitudService.aceptarCategoria(s.idSolicitud)
      : this.solicitudService.aceptarHabilidad(s.idSolicitud);

    obs.subscribe({
      next: () => {
        this.procesando.set(null);
        this.mensaje.set('Solicitud aceptada exitosamente');
        this.cargar();
        setTimeout(() => this.mensaje.set(''), 3000);
      },
      error: () => {
        this.procesando.set(null);
        this.mensajeError.set('Error al aceptar');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  rechazar(s: any) {
    if (!confirm('¿Rechazar esta solicitud?')) return;
    this.procesando.set(s.idSolicitud);
    const obs = s.tipo === 'CATEGORIA'
      ? this.solicitudService.rechazarCategoria(s.idSolicitud)
      : this.solicitudService.rechazarHabilidad(s.idSolicitud);

    obs.subscribe({
      next: () => {
        this.procesando.set(null);
        this.mensaje.set('Solicitud rechazada');
        this.cargar();
        setTimeout(() => this.mensaje.set(''), 3000);
      },
      error: () => {
        this.procesando.set(null);
        this.mensajeError.set('Error al rechazar');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}