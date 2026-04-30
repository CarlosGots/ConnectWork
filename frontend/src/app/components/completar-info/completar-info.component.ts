import { Component, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth.service';

/**
 * Componente que el usuario ve al iniciar sesión por primera vez.
 * Muestra un formulario distinto según su rol (Cliente o Freelancer).
 * Hasta completar esta información, el usuario no puede operar en la plataforma.
 */
@Component({
  selector: 'app-completar-info',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './completar-info.component.html',
  styleUrl: './completar-info.component.css'
})
export class CompletarInfoComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  // Datos del usuario actual
  usuario = computed(() => this.authService.obtenerUsuarioActual());
  esCliente = computed(() => this.usuario()?.rol === 'CLIENTE');
  esFreelancer = computed(() => this.usuario()?.rol === 'FREELANCER');

  // ========== Campos del CLIENTE ==========
  descripcionEmpresa = signal<string>('');
  sector = signal<string>('');
  sitioWeb = signal<string>('');

  // ========== Campos del FREELANCER ==========
  biografia = signal<string>('');
  nivelExperiencia = signal<'JUNIOR' | 'SEMI_SENIOR' | 'SENIOR' | ''>('');
  tarifaHora = signal<number | null>(null);
  habilidadesSeleccionadas = signal<number[]>([]);

  // Catálogo temporal de habilidades (luego vendrá del backend)
  // TODO: cargar desde el backend cuando agreguemos el HabilidadService
  habilidadesDisponibles = signal<Array<{id: number; nombre: string; categoria: string}>>([
    { id: 1, nombre: 'Photoshop', categoria: 'Diseño Gráfico' },
    { id: 2, nombre: 'Illustrator', categoria: 'Diseño Gráfico' },
    { id: 3, nombre: 'Figma', categoria: 'Diseño Gráfico' },
    { id: 4, nombre: 'HTML/CSS', categoria: 'Desarrollo Web' },
    { id: 5, nombre: 'JavaScript', categoria: 'Desarrollo Web' },
    { id: 6, nombre: 'Angular', categoria: 'Desarrollo Web' },
    { id: 7, nombre: 'Java', categoria: 'Desarrollo Web' },
    { id: 8, nombre: 'SEO', categoria: 'Marketing Digital' },
    { id: 9, nombre: 'Google Ads', categoria: 'Marketing Digital' },
    { id: 10, nombre: 'Redacción SEO', categoria: 'Redacción' },
    { id: 11, nombre: 'Copywriting', categoria: 'Redacción' },
    { id: 12, nombre: 'Android nativo', categoria: 'Desarrollo Móvil' },
    { id: 13, nombre: 'iOS nativo', categoria: 'Desarrollo Móvil' }
  ]);

  // Estado del componente
  cargando = signal<boolean>(false);
  mensajeError = signal<string>('');

  /**
   * Activa o desactiva una habilidad de la selección.
   */
  toggleHabilidad(idHabilidad: number): void {
    const seleccionadas = this.habilidadesSeleccionadas();
    if (seleccionadas.includes(idHabilidad)) {
      this.habilidadesSeleccionadas.set(seleccionadas.filter(id => id !== idHabilidad));
    } else {
      this.habilidadesSeleccionadas.set([...seleccionadas, idHabilidad]);
    }
  }

  estaHabilidadSeleccionada(id: number): boolean {
    return this.habilidadesSeleccionadas().includes(id);
  }

  /**
   * Procesa el envío del formulario.
   */
  guardarInformacion(): void {
    const errorValidacion = this.validar();
    if (errorValidacion) {
      this.mensajeError.set(errorValidacion);
      return;
    }

    this.cargando.set(true);
    this.mensajeError.set('');

    // TODO: cuando tengamos el endpoint en el backend, llamar al servicio aquí
    // Por ahora simulamos un guardado exitoso para probar el flujo visual
    setTimeout(() => {
      this.cargando.set(false);
      this.redirigirAlDashboard();
    }, 800);
  }

  /**
   * Valida el formulario según el rol del usuario.
   */
  private validar(): string | null {
    if (this.esCliente()) {
      if (!this.descripcionEmpresa().trim()) return 'La descripción de la empresa es obligatoria.';
      if (!this.sector().trim()) return 'El sector es obligatorio.';
    }

    if (this.esFreelancer()) {
      if (!this.biografia().trim()) return 'La biografía profesional es obligatoria.';
      if (this.biografia().length < 30) return 'La biografía debe tener al menos 30 caracteres.';
      if (this.habilidadesSeleccionadas().length === 0) return 'Selecciona al menos una habilidad.';
      if (!this.nivelExperiencia()) return 'Selecciona tu nivel de experiencia.';
      if (this.tarifaHora() === null || this.tarifaHora()! <= 0) return 'Ingresa una tarifa por hora válida.';
    }

    return null;
  }

  /**
   * Redirige al dashboard correspondiente al rol.
   */
  private redirigirAlDashboard(): void {
    const rol = this.usuario()?.rol;
    switch (rol) {
      case 'CLIENTE':    this.router.navigate(['/cliente']); break;
      case 'FREELANCER': this.router.navigate(['/freelancer']); break;
      default:           this.router.navigate(['/login']);
    }
  }

  /**
   * Cierra la sesión y vuelve al login.
   */
  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}