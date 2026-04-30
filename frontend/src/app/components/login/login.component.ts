import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/usuario.model';

interface TarjetaFeature {
  titulo: string;
  descripcion: string;
  iconoPath: string;
}

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
export class LoginComponent implements OnInit, OnDestroy {

  private authService = inject(AuthService);
  private router = inject(Router);

  // ============================================================
  // FORMULARIO
  // ============================================================
  username = signal<string>('');
  password = signal<string>('');
  cargando = signal<boolean>(false);
  mensajeError = signal<string>('');

  // ============================================================
  // CARRUSEL DE TARJETAS
  // ============================================================
  private intervaloCarrusel?: ReturnType<typeof setInterval>;
  indiceActivo = signal<number>(0);

  tarjetas: TarjetaFeature[] = [
    {
      titulo: 'Pagos seguros',
      descripcion: 'Saldo bloqueado hasta finalizar el contrato',
      iconoPath: 'M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z'
    },
    {
      titulo: 'Freelancers calificados',
      descripcion: 'Sistema de reputación con estrellas',
      iconoPath: 'M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.32.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .32-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z'
    },
    {
      titulo: 'Proyectos verificados',
      descripcion: 'Trazabilidad completa de cada entrega',
      iconoPath: 'M2.25 12.75V12A2.25 2.25 0 0 1 4.5 9.75h15A2.25 2.25 0 0 1 21.75 12v.75m-8.69-6.44-2.12-2.12a1.5 1.5 0 0 0-1.061-.44H4.5A2.25 2.25 0 0 0 2.25 6v12a2.25 2.25 0 0 0 2.25 2.25h15A2.25 2.25 0 0 0 21.75 18V9a2.25 2.25 0 0 0-2.25-2.25h-5.379a1.5 1.5 0 0 1-1.06-.44Z'
    },
    {
      titulo: 'Comunidad activa',
      descripcion: 'Cientos de profesionales conectados',
      iconoPath: 'M15 19.128a9.38 9.38 0 0 0 2.625.372 9.337 9.337 0 0 0 4.121-.952 4.125 4.125 0 0 0-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 0 1 8.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0 1 11.964-3.07M12 6.375a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0Zm8.25 2.25a2.625 2.625 0 1 1-5.25 0 2.625 2.625 0 0 1 5.25 0Z'
    },
    {
      titulo: 'Reportes detallados',
      descripcion: 'Análisis completo de tu actividad',
      iconoPath: 'M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z'
    }
  ];

  ngOnInit() {
    // Iniciar el carrusel automático: rota cada 4 segundos
    this.intervaloCarrusel = setInterval(() => {
      this.indiceActivo.update(idx => (idx + 1) % this.tarjetas.length);
    }, 4000);
  }

  ngOnDestroy() {
    // Limpiar el intervalo cuando el componente se destruye
    if (this.intervaloCarrusel) {
      clearInterval(this.intervaloCarrusel);
    }
  }

  /**
   * Devuelve los índices de las 3 tarjetas que se ven actualmente:
   * la activa (centro), la siguiente (abajo) y la que está saliendo (arriba).
   */
  obtenerIndicesVisibles(): { saliendo: number; activa: number; siguiente: number } {
    const total = this.tarjetas.length;
    const activa = this.indiceActivo();
    return {
      saliendo: (activa - 1 + total) % total,
      activa: activa,
      siguiente: (activa + 1) % total
    };
  }

  // ============================================================
  // LOGIN
  // ============================================================
  iniciarSesion(): void {
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

  private redirigirSegunRol(rol: string, primeraVez: boolean): void {
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