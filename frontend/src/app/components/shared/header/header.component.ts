import { Component, inject, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../services/auth.service';

/**
 * Componente Header.
 * Aparece en la parte superior de todas las pantallas autenticadas.
 * Muestra el logo, los datos del usuario actual y el botón de cierre de sesión.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  // Datos del usuario reactivo
  nombreUsuario = computed(() => this.authService.nombreActual());
  rolUsuario = computed(() => this.authService.rolActual());

  // Inicial del nombre para el avatar
  iniciales = computed(() => {
    const nombre = this.nombreUsuario();
    if (!nombre) return '?';
    const partes = nombre.trim().split(' ').filter(p => p.length > 0);
    if (partes.length === 0) return '?';
    if (partes.length === 1) return partes[0].charAt(0).toUpperCase();
    return (partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
  });

  // Etiqueta amigable del rol
  etiquetaRol = computed(() => {
    switch (this.rolUsuario()) {
      case 'CLIENTE':       return 'Cliente';
      case 'FREELANCER':    return 'Freelancer';
      case 'ADMINISTRADOR': return 'Administrador';
      default:              return '';
    }
  });

  // Ruta del logo según el rol (para que el logo lleve al dashboard correcto)
  rutaInicio = computed(() => {
    switch (this.rolUsuario()) {
      case 'CLIENTE':       return '/cliente';
      case 'FREELANCER':    return '/freelancer';
      case 'ADMINISTRADOR': return '/admin';
      default:              return '/login';
    }
  });

  /**
   * Cierra la sesión y redirige al login.
   */
  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}