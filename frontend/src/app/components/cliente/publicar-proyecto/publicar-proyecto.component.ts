import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';
import { ProyectoService } from '../../../services/proyecto.service';
import { CategoriaService, Categoria } from '../../../services/categoria.service';
import { HabilidadService, Habilidad } from '../../../services/habilidad.service';

@Component({
  selector: 'app-publicar-proyecto',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './publicar-proyecto.component.html',
  styleUrl: './publicar-proyecto.component.css'
})
export class PublicarProyectoComponent implements OnInit {

  private router = inject(Router);
  private proyectoService = inject(ProyectoService);
  private categoriaService = inject(CategoriaService);
  private habilidadService = inject(HabilidadService);

  // Datos del formulario
  titulo = signal<string>('');
  descripcion = signal<string>('');
  idCategoria = signal<number>(0);
  presupuesto = signal<number | null>(null);
  fechaLimite = signal<string>('');
  habilidadesSeleccionadas = signal<number[]>([]);

  // Catálogos
  categorias = signal<Categoria[]>([]);
  habilidades = signal<Habilidad[]>([]);

  // Estado
  guardando = signal<boolean>(false);
  mensajeError = signal<string>('');

  // Fecha mínima (mañana)
  fechaMinima: string = '';

  ngOnInit() {
    // Calcular fecha mínima (mañana)
    const manana = new Date();
    manana.setDate(manana.getDate() + 1);
    this.fechaMinima = manana.toISOString().split('T')[0];

    // Cargar catálogos
    this.categoriaService.listarActivas().subscribe({
      next: (lista) => this.categorias.set(lista),
      error: () => this.mensajeError.set('No se pudieron cargar las categorías.')
    });
  }

  // Cuando cambia la categoría, cargar habilidades de esa categoría
  onCategoriaChange(idCategoria: number): void {
    this.idCategoria.set(+idCategoria);
    this.habilidadesSeleccionadas.set([]);
    this.habilidades.set([]);

    if (idCategoria > 0) {
      this.habilidadService.listarPorCategoria(+idCategoria).subscribe({
        next: (lista) => this.habilidades.set(lista),
        error: () => {}
      });
    }
  }

  toggleHabilidad(id: number): void {
    const seleccionadas = this.habilidadesSeleccionadas();
    if (seleccionadas.includes(id)) {
      this.habilidadesSeleccionadas.set(seleccionadas.filter(h => h !== id));
    } else {
      this.habilidadesSeleccionadas.set([...seleccionadas, id]);
    }
  }

  estaSeleccionada(id: number): boolean {
    return this.habilidadesSeleccionadas().includes(id);
  }

  publicar(): void {
    // Validaciones
    if (!this.titulo().trim()) { this.mensajeError.set('El título es obligatorio.'); return; }
    if (!this.descripcion().trim()) { this.mensajeError.set('La descripción es obligatoria.'); return; }
    if (this.idCategoria() <= 0) { this.mensajeError.set('Debes seleccionar una categoría.'); return; }
    if (!this.presupuesto() || this.presupuesto()! <= 0) { this.mensajeError.set('El presupuesto debe ser mayor a Q0.'); return; }
    if (!this.fechaLimite()) { this.mensajeError.set('La fecha límite es obligatoria.'); return; }

    this.guardando.set(true);
    this.mensajeError.set('');

    this.proyectoService.publicar({
      titulo: this.titulo().trim(),
      descripcion: this.descripcion().trim(),
      idCategoria: this.idCategoria(),
      presupuesto: this.presupuesto()!,
      fechaLimite: this.fechaLimite(),
      habilidades: this.habilidadesSeleccionadas()
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/cliente/mis-proyectos']);
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(err.error?.error || 'Error al publicar el proyecto.');
      }
    });
  }

  volver(): void {
    this.router.navigate(['/cliente']);
  }
}