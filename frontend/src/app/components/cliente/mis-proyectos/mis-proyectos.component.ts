import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { HeaderComponent } from '../../shared/header/header.component';
import { NgClass } from '@angular/common';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ProyectoService, Proyecto } from '../../../services/proyecto.service';

@Component({
  selector: 'app-mis-proyectos',
  standalone: true,
  imports: [HeaderComponent, MenuLateralComponent, NgClass],
  templateUrl: './mis-proyectos.component.html',
  styleUrl: './mis-proyectos.component.css'
})
export class MisProyectosComponent implements OnInit {

  private router = inject(Router);
  private proyectoService = inject(ProyectoService);

  proyectos = signal<Proyecto[]>([]);
  cargando = signal<boolean>(false);
  mensajeExito = signal<string>('');
  mensajeError = signal<string>('');

  ngOnInit() {
    this.cargarProyectos();
  }

  cargarProyectos(): void {
    this.cargando.set(true);
    this.proyectoService.misProyectos().subscribe({
      next: (lista) => {
        this.proyectos.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar tus proyectos.');
        this.cargando.set(false);
      }
    });
  }

  verPropuestas(idProyecto: number): void {
  this.router.navigate(['/cliente/propuestas', idProyecto]);
}

  cancelar(proyecto: Proyecto): void {
    if (!confirm(`¿Cancelar el proyecto "${proyecto.titulo}"? Esta acción no se puede deshacer.`)) return;

    this.proyectoService.cancelar(proyecto.idProyecto).subscribe({
      next: () => {
        const lista = this.proyectos().map(p =>
          p.idProyecto === proyecto.idProyecto ? { ...p, estado: 'CANCELADO' as any } : p
        );
        this.proyectos.set(lista);
        this.mostrarExito('Proyecto cancelado exitosamente.');
      },
      error: (err) => {
        this.mensajeError.set(err.error?.error || 'No se pudo cancelar el proyecto.');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  publicarNuevo(): void {
    this.router.navigate(['/cliente/publicar']);
  }

  etiquetaEstado(estado: string): string {
    const etiquetas: Record<string, string> = {
      'ABIERTO': 'Abierto',
      'EN_REVISION': 'En revisión',
      'EN_PROGRESO': 'En progreso',
      'ENTREGA_PENDIENTE': 'Entrega pendiente',
      'COMPLETADO': 'Completado',
      'CANCELADO': 'Cancelado'
    };
    return etiquetas[estado] || estado;
  }

  colorEstado(estado: string): string {
    const colores: Record<string, string> = {
      'ABIERTO': 'estado-abierto',
      'EN_REVISION': 'estado-revision',
      'EN_PROGRESO': 'estado-progreso',
      'ENTREGA_PENDIENTE': 'estado-entrega',
      'COMPLETADO': 'estado-completado',
      'CANCELADO': 'estado-cancelado'
    };
    return colores[estado] || '';
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  private mostrarExito(mensaje: string): void {
    this.mensajeExito.set(mensaje);
    setTimeout(() => this.mensajeExito.set(''), 3000);
  }
}