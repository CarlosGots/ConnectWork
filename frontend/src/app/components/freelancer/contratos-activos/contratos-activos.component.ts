import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { EntregaService } from '../../../services/entrega.service';

@Component({
  selector: 'app-contratos-activos',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './contratos-activos.component.html',
  styleUrl: './contratos-activos.component.css'
})
export class ContratosActivosComponent implements OnInit {

  private entregaService = inject(EntregaService);

  contratos = signal<any[]>([]);
  cargando = signal(true);
  mensajeExito = signal('');
  mensajeError = signal('');

  // Formulario de entrega
  contratoSeleccionado = signal<any>(null);
  descripcion = signal('');
  archivosUrl = signal('');
  enviando = signal(false);

  // Historial de entregas
  entregasContrato = signal<any[]>([]);
  mostrandoHistorial = signal(false);

  ngOnInit() {
    this.cargarContratos();
  }

  cargarContratos() {
    this.cargando.set(true);
    this.entregaService.obtenerMisContratos().subscribe({
      next: (data) => {
        this.contratos.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('Error al cargar contratos');
        this.cargando.set(false);
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  seleccionarContrato(contrato: any) {
    this.contratoSeleccionado.set(contrato);
    this.descripcion.set('');
    this.archivosUrl.set('');
    this.mostrandoHistorial.set(false);
  }

  cancelarEntrega() {
    this.contratoSeleccionado.set(null);
  }

  subirEntrega() {
    if (!this.descripcion().trim()) {
      this.mensajeError.set('La descripción es obligatoria.');
      setTimeout(() => this.mensajeError.set(''), 3000);
      return;
    }

    this.enviando.set(true);
    this.mensajeError.set('');

    this.entregaService.subir(this.descripcion().trim(), this.archivosUrl().trim()).subscribe({
      next: () => {
        this.enviando.set(false);
        this.contratoSeleccionado.set(null);
        this.descripcion.set('');
        this.archivosUrl.set('');
        this.mensajeExito.set('¡Entrega subida exitosamente! El cliente la revisará pronto.');
        this.cargarContratos();
        setTimeout(() => this.mensajeExito.set(''), 5000);
      },
      error: (err) => {
        this.enviando.set(false);
        this.mensajeError.set(err.error?.error || 'Error al subir la entrega.');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  verHistorial(contrato: any) {
    this.entregaService.obtenerEntregasContrato(contrato.idContrato).subscribe({
      next: (data) => {
        this.entregasContrato.set(data);
        this.mostrandoHistorial.set(true);
        this.contratoSeleccionado.set(contrato);
      },
      error: () => {
        this.mensajeError.set('Error al cargar historial de entregas');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  cerrarHistorial() {
    this.mostrandoHistorial.set(false);
    this.entregasContrato.set([]);
  }
}