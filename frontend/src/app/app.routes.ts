import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // Rutas públicas
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'registro',
    loadComponent: () => import('./components/registro/registro.component').then(m => m.RegistroComponent)
  },

  // Completar información (cualquier rol logueado)
  {
    path: 'completar-info',
    loadComponent: () => import('./components/completar-info/completar-info.component').then(m => m.CompletarInfoComponent),
    canActivate: [authGuard]
  },

  // Cliente
  {
    path: 'cliente',
    loadComponent: () => import('./components/cliente/dashboard-cliente/dashboard-cliente.component').then(m => m.DashboardClienteComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLIENTE'] }
  },
  {
    path: 'cliente/publicar',
    loadComponent: () => import('./components/cliente/publicar-proyecto/publicar-proyecto.component').then(m => m.PublicarProyectoComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLIENTE'] }
  },
  {
    path: 'cliente/mis-proyectos',
    loadComponent: () => import('./components/cliente/mis-proyectos/mis-proyectos.component').then(m => m.MisProyectosComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLIENTE'] }
  },

  {
    path: 'cliente/propuestas/:idProyecto',
    loadComponent: () => import('./components/cliente/ver-propuestas/ver-propuestas.component').then(m => m.VerPropuestasComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLIENTE'] }
  },
  {
    path: 'cliente/recargar',
    loadComponent: () => import('./components/cliente/recargar-saldo/recargar-saldo.component').then(m => m.RecargarSaldoComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLIENTE'] }
  },

  // Freelancer

{
    path: 'freelancer/explorar',
    loadComponent: () => import('./components/freelancer/explorar-proyectos/explorar-proyectos.component').then(m => m.ExplorarProyectosComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FREELANCER'] }
  },
  {
    path: 'freelancer/propuestas',
    loadComponent: () => import('./components/freelancer/mis-propuestas/mis-propuestas.component').then(m => m.MisPropuestasComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FREELANCER'] }
  },


  {
    path: 'freelancer',
    loadComponent: () => import('./components/freelancer/dashboard-freelancer/dashboard-freelancer.component').then(m => m.DashboardFreelancerComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FREELANCER'] }
  },

  
 // Administrador
  {
    path: 'admin',
    loadComponent: () => import('./components/admin/dashboard-admin/dashboard-admin.component').then(m => m.DashboardAdminComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'admin/categorias',
    loadComponent: () => import('./components/admin/gestion-categorias/gestion-categorias.component').then(m => m.GestionCategoriasComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'admin/habilidades',
    loadComponent: () => import('./components/admin/gestion-habilidades/gestion-habilidades.component').then(m => m.GestionHabilidadesComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'admin/comision',
    loadComponent: () => import('./components/admin/cambiar-comision/cambiar-comision.component').then(m => m.CambiarComisionComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'admin/usuarios',
    loadComponent: () => import('./components/admin/gestion-usuarios/gestion-usuarios.component').then(m => m.GestionUsuariosComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },

  { path: '**', redirectTo: 'login' }

  
];