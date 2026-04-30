/**
 * Representa un usuario del sistema (Cliente, Freelancer o Administrador).
 * Esta interfaz refleja los datos que viajan entre el frontend y el backend.
 */
export interface Usuario {
  idUsuario?: number;
  nombreCompleto: string;
  username: string;
  password?: string;          // Solo se envía al registrar/loguear, no se recibe
  email: string;
  telefono: string;
  direccion: string;
  cui: string;
  fechaNacimiento: string;    // Formato YYYY-MM-DD
  rol: 'CLIENTE' | 'FREELANCER' | 'ADMINISTRADOR';
  saldo?: number;
  activo?: boolean;
  primeraVez?: boolean;
}

/**
 * Datos necesarios para iniciar sesión.
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Respuesta del backend cuando el login es exitoso.
 * Incluye el token JWT y los datos básicos del usuario.
 */
export interface LoginResponse {
  mensaje: string;
  token: string;
  idUsuario: number;
  username: string;
  nombreCompleto: string;
  rol: 'CLIENTE' | 'FREELANCER' | 'ADMINISTRADOR';
  primeraVez: boolean;
}

/**
 * Respuesta del backend cuando el registro es exitoso.
 */
export interface RegistroResponse {
  mensaje: string;
  idUsuario: number;
  username: string;
  rol: string;
}