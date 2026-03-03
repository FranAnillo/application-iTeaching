// ===== Auth =====
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

// ===== Entities =====
export interface Estudiante {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
}

export interface Profesor {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  puntuacion: number;
  division: number;
  avatar: string | null;
}

export interface Asignatura {
  id: number;
  nombre: string;
  descripcion: string;
  tituloAnuncio?: string;
  url?: string;
  precio: number;
  estudianteIds: number[];
  profesorId?: number;
  profesorNombre?: string;
}

export interface Clase {
  id: number;
  horaComienzo: string;
  horaFin: string;
  aceptacionAlumno: boolean;
  aceptacionProfesor: boolean;
  estadoClase: string;
  alumnoId: number;
  alumnoNombre: string;
  profesorId: number;
  profesorNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
}

export interface ClaseCreateRequest {
  horaComienzo: string;
  horaFin: string;
  alumnoId: number;
  profesorId: number;
  asignaturaId: number;
}

export interface Valoracion {
  id: number;
  puntuacion: number;
  comentario: string;
  profesorId: number;
  profesorNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
  alumnoId: number;
  alumnoNombre: string;
}

// ===== API Error =====
export interface ApiError {
  timestamp: string;
  message: string;
  status: number;
  errors?: Record<string, string>;
}
