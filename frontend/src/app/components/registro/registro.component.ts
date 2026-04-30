import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { Usuario } from '../../models/usuario.model';

/**
 * Componente de registro de usuarios.
 * Permite crear cuentas de Cliente o Freelancer.
 * Tras un registro exitoso, redirige al login.
 */
@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  // Paso del wizard: 1 = elegir tipo, 2 = formulario
  paso = signal<1 | 2>(1);

  // Tipo de cuenta seleccionado
  tipoSeleccionado = signal<'CLIENTE' | 'FREELANCER' | null>(null);

  // Datos del formulario
  nombreCompleto = signal<string>('');
  username = signal<string>('');
  password = signal<string>('');
  confirmarPassword = signal<string>('');
  email = signal<string>('');
  telefono = signal<string>('');
  direccion = signal<string>('');
  cui = signal<string>('');
  fechaNacimiento = signal<string>('');

  // Estado del componente
  cargando = signal<boolean>(false);
  mensajeError = signal<string>('');
  mensajeExito = signal<string>('');

  /**
   * Selecciona el tipo de cuenta y avanza al paso 2.
   */
  seleccionarTipo(tipo: 'CLIENTE' | 'FREELANCER'): void {
    this.tipoSeleccionado.set(tipo);
    this.paso.set(2);
    this.mensajeError.set('');
  }

  /**
   * Vuelve al paso de selección de tipo.
   */
  volverASeleccion(): void {
    this.paso.set(1);
    this.mensajeError.set('');
  }

  /**
   * Procesa el envío del formulario de registro.
   */
  registrarse(): void {
    // Validaciones del lado del cliente
    const errorValidacion = this.validarFormulario();
    if (errorValidacion) {
      this.mensajeError.set(errorValidacion);
      return;
    }

    this.cargando.set(true);
    this.mensajeError.set('');

    const nuevoUsuario: Usuario = {
      nombreCompleto: this.nombreCompleto().trim(),
      username: this.username().trim(),
      password: this.password(),
      email: this.email().trim(),
      telefono: this.telefono().trim(),
      direccion: this.direccion().trim(),
      cui: this.cui().trim(),
      fechaNacimiento: this.fechaNacimiento(),
      rol: this.tipoSeleccionado()!
    };

    this.authService.registrar(nuevoUsuario).subscribe({
      next: () => {
        this.cargando.set(false);
        this.mensajeExito.set('¡Cuenta creada exitosamente! Redirigiendo al login...');
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.cargando.set(false);
        if (err.status === 409) {
          this.mensajeError.set('Ya existe un usuario con ese nombre, email o CUI.');
        } else if (err.status === 400) {
          this.mensajeError.set(err.error?.error || 'Datos inválidos. Revisa los campos.');
        } else if (err.status === 0) {
          this.mensajeError.set('No se pudo conectar al servidor.');
        } else {
          this.mensajeError.set('Error al crear la cuenta. Intenta de nuevo.');
        }
      }
    });
  }

  /**
   * Valida el formulario antes de enviarlo al backend.
   * Retorna un mensaje de error o null si todo está bien.
   */
  private validarFormulario(): string | null {
    if (!this.nombreCompleto().trim()) return 'El nombre completo es obligatorio.';
    if (!this.username().trim()) return 'El nombre de usuario es obligatorio.';
    if (this.username().includes(' ')) return 'El usuario no puede contener espacios.';
    if (this.password().length < 6) return 'La contraseña debe tener al menos 6 caracteres.';
    if (this.password() !== this.confirmarPassword()) return 'Las contraseñas no coinciden.';
    if (!this.email().includes('@')) return 'El email no es válido.';
    if (!this.telefono().trim()) return 'El teléfono es obligatorio.';
    if (!this.direccion().trim()) return 'La dirección es obligatoria.';
    if (!this.cui().trim()) return 'El CUI es obligatorio.';
    if (this.cui().length < 13) return 'El CUI debe tener 13 dígitos.';
    if (!this.fechaNacimiento()) return 'La fecha de nacimiento es obligatoria.';

    // Validar que la fecha no sea futura
    const hoy = new Date().toISOString().split('T')[0];
    if (this.fechaNacimiento() > hoy) return 'La fecha de nacimiento no puede ser futura.';

    return null;
  }
}