import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Guard de rol.
 * Verifica que el usuario tenga uno de los roles permitidos para acceder.
 * Si no, redirige al dashboard correspondiente a su rol.
 *
 * Uso en las rutas:
 *   data: { roles: ['CLIENTE'] }
 *   canActivate: [authGuard, roleGuard]
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const rolUsuario = authService.rolActual();
  const rolesPermitidos = route.data?.['roles'] as string[] | undefined;

  if (!rolUsuario) {
    router.navigate(['/login']);
    return false;
  }

  if (!rolesPermitidos || rolesPermitidos.includes(rolUsuario)) {
    return true;
  }

  // Redirigir al dashboard que corresponde al rol del usuario
  switch (rolUsuario) {
    case 'CLIENTE':       router.navigate(['/cliente']); break;
    case 'FREELANCER':    router.navigate(['/freelancer']); break;
    case 'ADMINISTRADOR': router.navigate(['/admin']); break;
    default:              router.navigate(['/login']);
  }
  return false;
};