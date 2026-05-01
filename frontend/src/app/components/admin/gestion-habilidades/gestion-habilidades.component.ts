import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { HabilidadService, Habilidad } from '../../../services/habilidad.service';
import { CategoriaService, Categoria } from '../../../services/categoria.service';

/**
 * Pantalla de gestión de habilidades para el administrador.
 * Permite listar (filtradas por categoría), crear, editar y activar/desactivar habilidades.
 */
@Component({
  selector: 'app-gestion-habilidades',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './gestion-habilidades.component.html',
  styleUrl: './gestion-habilidades.component.css'
})
export class GestionHabilidadesComponent implements OnInit {

  private habilidadService = inject(HabilidadService);
  private categoriaService = inject(CategoriaService);

  // ============================================================
  // ESTADO
  // ============================================================
  habilidades = signal<Habilidad[]>([]);
  categorias = signal<Categoria[]>([]);
  cargando = signal<boolean>(false);

  // Filtro por categoría (0 = todas)
  filtroCategoria = signal<number>(0);

  // Modal de creación/edición
  modalAbierto = signal<boolean>(false);
  modoEdicion = signal<boolean>(false);
  guardando = signal<boolean>(false);

  // Datos del formulario
  idEditando = signal<number | null>(null);
  nombreInput = signal<string>('');
  idCategoriaInput = signal<number>(0);
  activaInput = signal<boolean>(true);

  // Mensajes
  mensajeError = signal<string>('');
  mensajeExito = signal<string>('');

  // Habilidades filtradas según la categoría seleccionada
  habilidadesFiltradas = computed(() => {
    const filtro = this.filtroCategoria();
    if (filtro === 0) return this.habilidades();
    return this.habilidades().filter(h => h.idCategoria === filtro);
  });

  ngOnInit() {
    this.cargarDatos();
  }

  // ============================================================
  // CARGA DE DATOS
  // ============================================================
  cargarDatos(): void {
    this.cargando.set(true);

    // Cargar categorías y habilidades en paralelo
    this.categoriaService.listarTodas().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las categorías.');
      }
    });

    this.habilidadService.listarTodas().subscribe({
      next: (habilidades) => {
        this.habilidades.set(habilidades);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las habilidades.');
        this.cargando.set(false);
      }
    });
  }

  // ============================================================
  // FILTRO
  // ============================================================
  cambiarFiltro(idCategoria: number): void {
    this.filtroCategoria.set(idCategoria);
  }

  // ============================================================
  // ABRIR MODAL
  // ============================================================
  abrirModalCrear(): void {
    this.modoEdicion.set(false);
    this.idEditando.set(null);
    this.nombreInput.set('');
    // Pre-seleccionar la categoría del filtro actual (si hay)
    this.idCategoriaInput.set(this.filtroCategoria() || 0);
    this.activaInput.set(true);
    this.mensajeError.set('');
    this.modalAbierto.set(true);
  }

  abrirModalEditar(habilidad: Habilidad): void {
    this.modoEdicion.set(true);
    this.idEditando.set(habilidad.idHabilidad);
    this.nombreInput.set(habilidad.nombre);
    this.idCategoriaInput.set(habilidad.idCategoria);
    this.activaInput.set(habilidad.activa);
    this.mensajeError.set('');
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.mensajeError.set('');
  }

  // ============================================================
  // GUARDAR
  // ============================================================
  guardar(): void {
    if (!this.nombreInput().trim()) {
      this.mensajeError.set('El nombre es obligatorio.');
      return;
    }
    if (this.idCategoriaInput() <= 0) {
      this.mensajeError.set('Debes seleccionar una categoría.');
      return;
    }

    this.guardando.set(true);
    this.mensajeError.set('');

    if (this.modoEdicion()) {
      this.actualizarHabilidad();
    } else {
      this.crearHabilidad();
    }
  }

  private crearHabilidad(): void {
    const datos = {
      nombre: this.nombreInput().trim(),
      idCategoria: this.idCategoriaInput()
    };

    this.habilidadService.crear(datos).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mostrarExito('Habilidad creada exitosamente.');
        this.cargarDatos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.manejarError(err);
      }
    });
  }

  private actualizarHabilidad(): void {
    const id = this.idEditando();
    if (!id) return;

    const datos = {
      nombre: this.nombreInput().trim(),
      idCategoria: this.idCategoriaInput(),
      activa: this.activaInput()
    };

    this.habilidadService.actualizar(id, datos).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mostrarExito('Habilidad actualizada exitosamente.');
        this.cargarDatos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.manejarError(err);
      }
    });
  }

  // ============================================================
  // ACTIVAR / DESACTIVAR
  // ============================================================
  toggleEstado(habilidad: Habilidad): void {
    const nuevoEstado = !habilidad.activa;
    this.habilidadService.cambiarEstado(habilidad.idHabilidad, nuevoEstado).subscribe({
      next: () => {
        const lista = this.habilidades().map(h =>
          h.idHabilidad === habilidad.idHabilidad ? { ...h, activa: nuevoEstado } : h
        );
        this.habilidades.set(lista);
        this.mostrarExito(nuevoEstado ? 'Habilidad activada.' : 'Habilidad desactivada.');
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        this.mensajeError.set('No se pudo cambiar el estado.');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  // ============================================================
  // UTILIDADES
  // ============================================================
  private manejarError(err: any): void {
    if (err.status === 409) {
      this.mensajeError.set(err.error?.error || 'Ya existe una habilidad con ese nombre.');
    } else if (err.status === 403) {
      this.mensajeError.set('No tienes permisos para esta acción.');
    } else {
      this.mensajeError.set('Ocurrió un error. Intenta de nuevo.');
    }
  }

  private mostrarExito(mensaje: string): void {
    this.mensajeExito.set(mensaje);
    setTimeout(() => this.mensajeExito.set(''), 3000);
  }
}