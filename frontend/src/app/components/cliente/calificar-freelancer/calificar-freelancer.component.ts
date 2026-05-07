import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CalificacionService } from '../../../services/calificacion.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-calificar-freelancer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './calificar-freelancer.component.html',
  styleUrls: ['./calificar-freelancer.component.css']
})
export class CalificarFreelancerComponent implements OnInit {
  private calificacionService = inject(CalificacionService);
  private router = inject(Router);

  pendientes = signal<any[]>([]);
  cargando = signal(true);
  mensaje = signal('');
  mensajeError = signal('');
  modalAbierto = signal(false);
  contratoSeleccionado = signal<any>(null);
  estrellas = signal(0);
  comentario = signal('');
  hoverEstrellas = signal(0);

  ngOnInit() {
    this.cargarPendientes();
  }

  cargarPendientes() {
    this.cargando.set(true);
    this.calificacionService.obtenerPendientes().subscribe({
      next: (data) => {
        this.pendientes.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('Error al cargar contratos pendientes');
        this.cargando.set(false);
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  abrirModal(contrato: any) {
    this.contratoSeleccionado.set(contrato);
    this.estrellas.set(0);
    this.comentario.set('');
    this.hoverEstrellas.set(0);
    this.modalAbierto.set(true);
  }

  cerrarModal() {
    this.modalAbierto.set(false);
    this.contratoSeleccionado.set(null);
  }

  setEstrellas(n: number) {
    this.estrellas.set(n);
  }

  enviarCalificacion() {
    if (this.estrellas() < 1) {
      this.mensajeError.set('Selecciona al menos 1 estrella');
      setTimeout(() => this.mensajeError.set(''), 3000);
      return;
    }

    const contrato = this.contratoSeleccionado();
    this.calificacionService.calificar(contrato.idContrato, this.estrellas(), this.comentario()).subscribe({
      next: () => {
        this.mensaje.set('Calificación enviada exitosamente');
        this.cerrarModal();
        this.cargarPendientes();
        setTimeout(() => this.mensaje.set(''), 3000);
      },
      error: (err: any) => {
        this.mensajeError.set(err.error?.error || 'Error al enviar calificación');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }
}