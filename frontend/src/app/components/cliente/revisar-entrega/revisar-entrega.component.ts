import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { EntregaService } from '../../../services/entrega.service';

@Component({
  selector: 'app-revisar-entrega',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './revisar-entrega.component.html',
  styleUrl: './revisar-entrega.component.css'
})
export class RevisarEntregaComponent implements OnInit {

  private entregaService = inject(EntregaService);
  private http = inject(HttpClient);

  entregas = signal<any[]>([]);
  cargando = signal<boolean>(false);
  mensajeExito = signal<string>('');
  mensajeError = signal<string>('');
  procesando = signal<number | null>(null);

  // Modal de rechazo
  modalRechazo = signal<boolean>(false);
  idEntregaRechazar = signal<number | null>(null);
  motivoRechazo = signal<string>('');

  ngOnInit() {
    this.cargarEntregas();
  }

  cargarEntregas(): void {
    this.cargando.set(true);
    // Buscar el contrato activo del cliente via proyectos EN_PROGRESO o ENTREGA_PENDIENTE
    this.http.get<any[]>('http://localhost:8080/connectwork-backend/proyectos/mis').subscribe({
      next: (proyectos) => {
        const activos = proyectos.filter(p =>
          p.estado === 'EN_PROGRESO' || p.estado === 'ENTREGA_PENDIENTE'
        );
        if (activos.length === 0) {
          this.cargando.set(false);
          return;
        }
        // Cargar entregas del primer contrato activo
        // Usamos el endpoint de entregas directamente
        this.http.get<any[]>(`http://localhost:8080/connectwork-backend/entregas/contrato`).subscribe({
          next: (lista) => { this.entregas.set(lista); this.cargando.set(false); },
          error: () => { this.cargando.set(false); }
        });
      },
      error: () => { this.cargando.set(false); }
    });
  }

  aprobar(idEntrega: number): void {
    if (!confirm('¿Aprobar esta entrega? Se liberará el pago al freelancer.')) return;
    this.procesando.set(idEntrega);

    this.entregaService.aprobar(idEntrega).subscribe({
      next: () => {
        this.procesando.set(null);
        this.mostrarExito('¡Entrega aprobada! El pago ha sido liberado al freelancer.');
        this.entregas.set(this.entregas().map(e =>
          e.idEntrega === idEntrega ? { ...e, estado: 'APROBADA' } : e
        ));
      },
      error: (err) => {
        this.procesando.set(null);
        this.mensajeError.set(err.error?.error || 'No se pudo aprobar la entrega.');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  abrirModalRechazo(idEntrega: number): void {
    this.idEntregaRechazar.set(idEntrega);
    this.motivoRechazo.set('');
    this.modalRechazo.set(true);
  }

  confirmarRechazo(): void {
    if (!this.motivoRechazo().trim()) {
      this.mensajeError.set('El motivo de rechazo es obligatorio.');
      return;
    }

    const id = this.idEntregaRechazar()!;
    this.procesando.set(id);
    this.modalRechazo.set(false);

    this.entregaService.rechazar(id, this.motivoRechazo()).subscribe({
      next: () => {
        this.procesando.set(null);
        this.mostrarExito('Entrega rechazada. El freelancer puede subir una nueva.');
        this.entregas.set(this.entregas().map(e =>
          e.idEntrega === id ? { ...e, estado: 'RECHAZADA', motivoRechazo: this.motivoRechazo() } : e
        ));
      },
      error: () => {
        this.procesando.set(null);
        this.mensajeError.set('No se pudo rechazar la entrega.');
      }
    });
  }

  private mostrarExito(msg: string): void {
    this.mensajeExito.set(msg);
    setTimeout(() => this.mensajeExito.set(''), 5000);
  }
  formatearFecha(fecha: string): string {
  if (!fecha) return '—';
  return new Date(fecha).toLocaleDateString('es-GT', {
    day: '2-digit', month: 'short', year: 'numeric'
  });
}
}