import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { CategoriaService, Categoria } from '../../../services/categoria.service';

/**
 * Pantalla de gestión de categorías para el administrador.
 * Permite listar, crear, editar y activar/desactivar categorías.
 */
@Component({
  selector: 'app-gestion-categorias',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './gestion-categorias.component.html',
  styleUrl: './gestion-categorias.component.css'
})
export class GestionCategoriasComponent implements OnInit {

  private categoriaService = inject(CategoriaService);

  // ============================================================
  // ESTADO
  // ============================================================
  categorias = signal<Categoria[]>([]);
  cargando = signal<boolean>(false);

  // Modal de creación/edición
  modalAbierto = signal<boolean>(false);
  modoEdicion = signal<boolean>(false);
  guardando = signal<boolean>(false);

  // Datos del formulario
  idEditando = signal<number | null>(null);
  nombreInput = signal<string>('');
  descripcionInput = signal<string>('');
  activaInput = signal<boolean>(true);

  // Mensajes
  mensajeError = signal<string>('');
  mensajeExito = signal<string>('');

  ngOnInit() {
    this.cargarCategorias();
  }

  // ============================================================
  // CARGA DE DATOS
  // ============================================================
  cargarCategorias(): void {
    this.cargando.set(true);
    this.categoriaService.listarTodas().subscribe({
      next: (lista) => {
        this.categorias.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las categorías.');
        this.cargando.set(false);
      }
    });
  }

  // ============================================================
  // ABRIR MODAL
  // ============================================================
  abrirModalCrear(): void {
    this.modoEdicion.set(false);
    this.idEditando.set(null);
    this.nombreInput.set('');
    this.descripcionInput.set('');
    this.activaInput.set(true);
    this.mensajeError.set('');
    this.modalAbierto.set(true);
  }

  abrirModalEditar(categoria: Categoria): void {
    this.modoEdicion.set(true);
    this.idEditando.set(categoria.idCategoria);
    this.nombreInput.set(categoria.nombre);
    this.descripcionInput.set(categoria.descripcion);
    this.activaInput.set(categoria.activa);
    this.mensajeError.set('');
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.mensajeError.set('');
  }

  // ============================================================
  // GUARDAR (crear o actualizar)
  // ============================================================
  guardar(): void {
    if (!this.nombreInput().trim()) {
      this.mensajeError.set('El nombre es obligatorio.');
      return;
    }

    this.guardando.set(true);
    this.mensajeError.set('');

    if (this.modoEdicion()) {
      this.actualizarCategoria();
    } else {
      this.crearCategoria();
    }
  }

  private crearCategoria(): void {
    const datos = {
      nombre: this.nombreInput().trim(),
      descripcion: this.descripcionInput().trim()
    };

    this.categoriaService.crear(datos).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mostrarExito('Categoría creada exitosamente.');
        this.cargarCategorias();
      },
      error: (err) => {
        this.guardando.set(false);
        this.manejarError(err);
      }
    });
  }

  private actualizarCategoria(): void {
    const id = this.idEditando();
    if (!id) return;

    const datos = {
      nombre: this.nombreInput().trim(),
      descripcion: this.descripcionInput().trim(),
      activa: this.activaInput()
    };

    this.categoriaService.actualizar(id, datos).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mostrarExito('Categoría actualizada exitosamente.');
        this.cargarCategorias();
      },
      error: (err) => {
        this.guardando.set(false);
        this.manejarError(err);
      }
    });
  }

 toggleEstado(categoria: Categoria): void {
    const nuevoEstado = !categoria.activa;
    this.categoriaService.cambiarEstado(categoria.idCategoria, nuevoEstado).subscribe({
      next: () => {
        const lista = this.categorias().map(c =>
          c.idCategoria === categoria.idCategoria ? { ...c, activa: nuevoEstado } : c
        );
        this.categorias.set(lista);
        this.mostrarExito(nuevoEstado ? 'Categoría activada.' : 'Categoría desactivada.');
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
      this.mensajeError.set(err.error?.error || 'Ya existe una categoría con ese nombre.');
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