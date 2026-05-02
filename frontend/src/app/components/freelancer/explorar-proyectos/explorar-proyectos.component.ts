import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ProyectoService, Proyecto } from '../../../services/proyecto.service';
import { CategoriaService, Categoria } from '../../../services/categoria.service';
import { PropuestaService } from '../../../services/propuesta.service';

@Component({
  selector: 'app-explorar-proyectos',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './explorar-proyectos.component.html',
  styleUrl: './explorar-proyectos.component.css'
})
export class ExplorarProyectosComponent implements OnInit {

  private proyectoService = inject(ProyectoService);
  private categoriaService = inject(CategoriaService);
  private propuestaService = inject(PropuestaService);

  proyectos = signal<Proyecto[]>([]);
  categorias = signal<Categoria[]>([]);
  cargando = signal<boolean>(false);
  mensajeError = signal<string>('');
  mensajeExito = signal<string>('');

  // Filtros
  filtroCategoriaId = signal<number>(0);
  filtroBusqueda = signal<string>('');

  proyectosFiltrados = computed(() => {
    let lista = this.proyectos();
    if (this.filtroCategoriaId() > 0) {
      lista = lista.filter(p => p.idCategoria === this.filtroCategoriaId());
    }
    const busqueda = this.filtroBusqueda().toLowerCase().trim();
    if (busqueda) {
      lista = lista.filter(p =>
        p.titulo.toLowerCase().includes(busqueda) ||
        p.descripcion.toLowerCase().includes(busqueda)
      );
    }
    return lista;
  });

  // Modal de propuesta
  modalAbierto = signal<boolean>(false);
  proyectoSeleccionado = signal<Proyecto | null>(null);
  montoOfertado = signal<number | null>(null);
  plazoDias = signal<number | null>(null);
  cartaPresentacion = signal<string>('');
  enviando = signal<boolean>(false);
  errorModal = signal<string>('');

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.categoriaService.listarActivas().subscribe({
      next: (lista) => this.categorias.set(lista)
    });
    this.proyectoService.listarAbiertos().subscribe({
      next: (lista) => { this.proyectos.set(lista); this.cargando.set(false); },
      error: () => { this.mensajeError.set('No se pudieron cargar los proyectos.'); this.cargando.set(false); }
    });
  }

  abrirModal(proyecto: Proyecto): void {
    this.proyectoSeleccionado.set(proyecto);
    this.montoOfertado.set(proyecto.presupuesto);
    this.plazoDias.set(null);
    this.cartaPresentacion.set('');
    this.errorModal.set('');
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.errorModal.set('');
  }

  enviarPropuesta(): void {
    if (!this.montoOfertado() || this.montoOfertado()! <= 0) {
      this.errorModal.set('El monto ofertado debe ser mayor a Q0.'); return;
    }
    if (!this.plazoDias() || this.plazoDias()! <= 0) {
      this.errorModal.set('El plazo debe ser mayor a 0 días.'); return;
    }
    if (!this.cartaPresentacion().trim() || this.cartaPresentacion().length < 50) {
      this.errorModal.set('La carta de presentación debe tener al menos 50 caracteres.'); return;
    }

    this.enviando.set(true);
    this.errorModal.set('');

    this.propuestaService.enviar({
      idProyecto: this.proyectoSeleccionado()!.idProyecto,
      montoOfertado: this.montoOfertado()!,
      plazoDias: this.plazoDias()!,
      cartaPresentacion: this.cartaPresentacion().trim()
    }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.cerrarModal();
        this.mensajeExito.set('¡Propuesta enviada exitosamente!');
        setTimeout(() => this.mensajeExito.set(''), 4000);
      },
      error: (err) => {
        this.enviando.set(false);
        if (err.status === 409) {
          this.errorModal.set('Ya enviaste una propuesta a este proyecto.');
        } else {
          this.errorModal.set(err.error?.error || 'Error al enviar la propuesta.');
        }
      }
    });
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }
}