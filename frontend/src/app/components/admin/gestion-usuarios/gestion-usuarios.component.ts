import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import { HeaderComponent } from '../../shared/header/header.component';
import { MenuLateralComponent } from '../../shared/menu-lateral/menu-lateral.component';

export interface Usuario {
  idUsuario: number;
  nombreCompleto: string;
  username: string;
  email: string;
  telefono: string;
  rol: 'CLIENTE' | 'FREELANCER' | 'ADMINISTRADOR';
  saldo: number;
  activo: boolean;
  primeraVez: boolean;
  fechaCreacion: string;
}

@Component({
  selector: 'app-gestion-usuarios',
  standalone: true,
  imports: [FormsModule, HeaderComponent, MenuLateralComponent],
  templateUrl: './gestion-usuarios.component.html',
  styleUrl: './gestion-usuarios.component.css'
})
export class GestionUsuariosComponent implements OnInit {

  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/connectwork-backend/usuarios';

  usuarios = signal<Usuario[]>([]);
  cargando = signal<boolean>(false);
  mensajeExito = signal<string>('');
  mensajeError = signal<string>('');

  // Filtro por rol
  filtroRol = signal<string>('TODOS');

  usuariosFiltrados = computed(() => {
    const filtro = this.filtroRol();
    if (filtro === 'TODOS') return this.usuarios();
    return this.usuarios().filter(u => u.rol === filtro);
  });

  // Contadores por rol
  totalClientes = computed(() => this.usuarios().filter(u => u.rol === 'CLIENTE').length);
  totalFreelancers = computed(() => this.usuarios().filter(u => u.rol === 'FREELANCER').length);
  totalAdmins = computed(() => this.usuarios().filter(u => u.rol === 'ADMINISTRADOR').length);
  totalActivos = computed(() => this.usuarios().filter(u => u.activo).length);

  ngOnInit() {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando.set(true);
    this.http.get<Usuario[]>(this.API_URL).subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar los usuarios.');
        this.cargando.set(false);
      }
    });
  }

  toggleEstado(usuario: Usuario): void {
    if (usuario.rol === 'ADMINISTRADOR') {
      this.mensajeError.set('No se puede desactivar una cuenta de administrador.');
      setTimeout(() => this.mensajeError.set(''), 3000);
      return;
    }

    const nuevoEstado = !usuario.activo;
    this.http.put<{ mensaje: string }>(
      `${this.API_URL}/${usuario.idUsuario}/estado`,
      { activo: nuevoEstado }
    ).subscribe({
      next: () => {
        const lista = this.usuarios().map(u =>
          u.idUsuario === usuario.idUsuario ? { ...u, activo: nuevoEstado } : u
        );
        this.usuarios.set(lista);
        this.mostrarExito(nuevoEstado ? 'Usuario activado.' : 'Usuario desactivado.');
      },
      error: () => {
        this.mensajeError.set('No se pudo cambiar el estado.');
        setTimeout(() => this.mensajeError.set(''), 3000);
      }
    });
  }

  etiquetaRol(rol: string): string {
    switch (rol) {
      case 'CLIENTE': return 'Cliente';
      case 'FREELANCER': return 'Freelancer';
      case 'ADMINISTRADOR': return 'Admin';
      default: return rol;
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  private mostrarExito(mensaje: string): void {
    this.mensajeExito.set(mensaje);
    setTimeout(() => this.mensajeExito.set(''), 3000);
  }
}