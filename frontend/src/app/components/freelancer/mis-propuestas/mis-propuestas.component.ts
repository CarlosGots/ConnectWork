import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { PropuestaService, Propuesta } from '../../../services/propuesta.service';

@Component({
  selector: 'app-mis-propuestas',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent, MenuLateralComponent],
  templateUrl: './mis-propuestas.component.html',
  styleUrl: './mis-propuestas.component.css'
})
export class MisPropuestasComponent implements OnInit {

  private propuestaService = inject(PropuestaService);

  propuestas = signal<Propuesta[]>([]);
  cargando = signal(true);
  mensajeError = signal('');
  filtroEstado = signal('TODOS');

  ngOnInit() {
    this.cargarPropuestas();
  }

  cargarPropuestas() {
    this.cargando.set(true);
    this.propuestaService.misPropuestas().subscribe({
      next: (data) => {
        this.propuestas.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('Error al cargar propuestas');
        this.cargando.set(false);
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  propuestasFiltradas() {
    const filtro = this.filtroEstado();
    if (filtro === 'TODOS') return this.propuestas();
    return this.propuestas().filter(p => p.estado === filtro);
  }

  setFiltro(estado: string) {
    this.filtroEstado.set(estado);
  }

  contarPorEstado(estado: string): number {
    if (estado === 'TODOS') return this.propuestas().length;
    return this.propuestas().filter(p => p.estado === estado).length;
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }
  retirarPropuesta(idPropuesta: number) {
  if (!confirm('¿Retirar esta propuesta? Esta acción no se puede deshacer.')) return;

  this.propuestaService.retirar(idPropuesta).subscribe({
    next: () => {
      this.cargarPropuestas();
    },
    error: (err: any) => {
      this.mensajeError.set(err.error?.error || 'Error al retirar propuesta');
      setTimeout(() => this.mensajeError.set(''), 3000);
    }
  });
}
}