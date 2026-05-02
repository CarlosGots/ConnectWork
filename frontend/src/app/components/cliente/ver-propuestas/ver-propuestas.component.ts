import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { PropuestaService, Propuesta } from '../../../services/propuesta.service';
import { ProyectoService, Proyecto } from '../../../services/proyecto.service';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-ver-propuestas',
  standalone: true,
  imports: [HeaderComponent, MenuLateralComponent, NgClass],
  templateUrl: './ver-propuestas.component.html',
  styleUrl: './ver-propuestas.component.css'
})
export class VerPropuestasComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private propuestaService = inject(PropuestaService);
  private proyectoService = inject(ProyectoService);

  propuestas = signal<Propuesta[]>([]);
  proyecto = signal<Proyecto | null>(null);
  cargando = signal<boolean>(false);
  mensajeExito = signal<string>('');
  mensajeError = signal<string>('');
  procesando = signal<number | null>(null);

  ngOnInit() {
    const idProyecto = Number(this.route.snapshot.paramMap.get('idProyecto'));
    this.cargarDatos(idProyecto);
  }

  cargarDatos(idProyecto: number): void {
    this.cargando.set(true);

    this.proyectoService.obtenerPorId(idProyecto).subscribe({
      next: (p) => this.proyecto.set(p),
      error: () => {}
    });

    this.propuestaService.listarPorProyecto(idProyecto).subscribe({
      next: (lista) => { this.propuestas.set(lista); this.cargando.set(false); },
      error: () => { this.mensajeError.set('No se pudieron cargar las propuestas.'); this.cargando.set(false); }
    });
  }

  aceptar(propuesta: Propuesta): void {
    if (!confirm(`¿Aceptar la propuesta de ${propuesta.nombreFreelancer} por Q${propuesta.montoOfertado.toFixed(2)}? Se bloqueará ese monto de tu saldo.`)) return;

    this.procesando.set(propuesta.idPropuesta);

    this.propuestaService.aceptar(propuesta.idPropuesta).subscribe({
      next: () => {
        this.procesando.set(null);
        this.mostrarExito('¡Propuesta aceptada! El contrato ha sido generado.');
        // Actualizar lista local
        const lista = this.propuestas().map(p => ({
          ...p,
          estado: p.idPropuesta === propuesta.idPropuesta ? 'ACEPTADA' as const :
                  p.estado === 'PENDIENTE' ? 'RECHAZADA' as const : p.estado
        }));
        this.propuestas.set(lista);
      },
      error: (err) => {
        this.procesando.set(null);
        this.mensajeError.set(err.error?.error || 'No se pudo aceptar. Verifica tu saldo.');
        setTimeout(() => this.mensajeError.set(''), 4000);
      }
    });
  }

  rechazar(propuesta: Propuesta): void {
    if (!confirm(`¿Rechazar la propuesta de ${propuesta.nombreFreelancer}?`)) return;

    this.procesando.set(propuesta.idPropuesta);

    this.propuestaService.rechazar(propuesta.idPropuesta).subscribe({
      next: () => {
        this.procesando.set(null);
        this.mostrarExito('Propuesta rechazada.');
        const lista = this.propuestas().map(p =>
          p.idPropuesta === propuesta.idPropuesta ? { ...p, estado: 'RECHAZADA' as const } : p
        );
        this.propuestas.set(lista);
      },
      error: () => {
        this.procesando.set(null);
        this.mensajeError.set('No se pudo rechazar la propuesta.');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  volver(): void {
    this.router.navigate(['/cliente/mis-proyectos']);
  }

  private mostrarExito(msg: string): void {
    this.mensajeExito.set(msg);
    setTimeout(() => this.mensajeExito.set(''), 4000);
  }
}