import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor JWT.
 * Se ejecuta automáticamente antes de cada petición HTTP saliente.
 * Si hay un token guardado, lo agrega al header Authorization.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.obtenerToken();

  // No interceptar las peticiones de login y registro (todavía no hay token)
  const esRutaAuth = req.url.includes('/auth/login') || req.url.includes('/auth/registro');

  if (token && !esRutaAuth) {
    const requestConToken = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(requestConToken);
  }

  return next(req);
};