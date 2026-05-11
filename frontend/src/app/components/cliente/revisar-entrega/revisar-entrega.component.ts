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
  modalCancelar = signal(false);
  idContratoCancelar = signal<number | null>(null);
  motivoCancelacion = signal('');
  modalArchivos = signal(false);
  entregaArchivos = signal<any>(null);

verArchivos(entrega: any) {
  this.entregaArchivos.set(entrega);
  this.modalArchivos.set(true);
}


  // Modal de rechazo
  modalRechazo = signal<boolean>(false);
  idEntregaRechazar = signal<number | null>(null);
  motivoRechazo = signal<string>('');

  ngOnInit() {
    this.cargarEntregas();
  }

  cargarEntregas(): void {
  this.cargando.set(true);
  this.entregaService.obtenerMisEntregas().subscribe({
    next: (lista) => {
      this.entregas.set(lista);
      this.cargando.set(false);
    },
    error: () => {
      this.cargando.set(false);
    }
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
// Agregar estos métodos
abrirModalCancelar(idContrato: number) {
  this.idContratoCancelar.set(idContrato);
  this.motivoCancelacion.set('');
  this.modalCancelar.set(true);
}

confirmarCancelacion() {
  if (!this.motivoCancelacion().trim()) {
    this.mensajeError.set('El motivo de cancelación es obligatorio.');
    setTimeout(() => this.mensajeError.set(''), 3000);
    return;
  }

  const id = this.idContratoCancelar()!;
  this.modalCancelar.set(false);

  this.entregaService.cancelarContrato(id, this.motivoCancelacion()).subscribe({
    next: () => {
      this.mostrarExito('Contrato cancelado. El monto ha sido devuelto a tu saldo.');
      this.cargarEntregas();
    },
    error: (err: any) => {
      this.mensajeError.set(err.error?.error || 'No se pudo cancelar el contrato.');
      setTimeout(() => this.mensajeError.set(''), 3000);
    }
  });
}
}