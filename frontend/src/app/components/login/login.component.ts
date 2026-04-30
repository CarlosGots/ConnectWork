import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/usuario.model';

/**
 * Componente de inicio de sesión.
 * Permite al usuario autenticarse con username y password.
 * Tras un login exitoso, redirige al dashboard correspondiente a su rol.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  // Datos del formulario
  username = signal<string>('');
  password = signal<string>('');

  // Estado del componente
  cargando = signal<boolean>(false);
  mensajeError = signal<string>('');

  /**
   * Procesa el envío del formulario de login.
   */
  iniciarSesion(): void {
    // Validación básica
    if (!this.username().trim() || !this.password().trim()) {
      this.mensajeError.set('Por favor completa todos los campos.');
      return;
    }

    this.cargando.set(true);
    this.mensajeError.set('');

    const credenciales: LoginRequest = {
      username: this.username(),
      password: this.password()
    };

    this.authService.login(credenciales).subscribe({
      next: (respuesta) => {
        this.cargando.set(false);
        this.redirigirSegunRol(respuesta.rol, respuesta.primeraVez);
      },
      error: (err) => {
        this.cargando.set(false);
        if (err.status === 401) {
          this.mensajeError.set('Usuario o contraseña incorrectos.');
        } else if (err.status === 0) {
          this.mensajeError.set('No se pudo conectar al servidor. Verifica que el backend esté corriendo.');
        } else {
          this.mensajeError.set(err.error?.error || 'Error al iniciar sesión.');
        }
      }
    });
  }

  /**
   * Redirige al usuario según su rol.
   * Si es la primera vez que ingresa, lo manda a completar información.
   */
  private redirigirSegunRol(rol: string, primeraVez: boolean): void {
    // Los administradores no necesitan completar información inicial
    if (primeraVez && rol !== 'ADMINISTRADOR') {
      this.router.navigate(['/completar-info']);
      return;
    }

    switch (rol) {
      case 'CLIENTE':       this.router.navigate(['/cliente']); break;
      case 'FREELANCER':    this.router.navigate(['/freelancer']); break;
      case 'ADMINISTRADOR': this.router.navigate(['/admin']); break;
      default:              this.router.navigate(['/login']);
    }
  }
}