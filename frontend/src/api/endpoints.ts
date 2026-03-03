import api from './client';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  Estudiante,
  Profesor,
  Asignatura,
  Clase,
  ClaseCreateRequest,
  Valoracion,
} from '../types';

// ===== Auth =====
export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
};

// ===== Estudiantes =====
export const estudiantesApi = {
  getAll: () => api.get<Estudiante[]>('/estudiantes'),
  getById: (id: number) => api.get<Estudiante>(`/estudiantes/${id}`),
  search: (q: string) => api.get<Estudiante[]>(`/estudiantes/search?q=${q}`),
  create: (data: Partial<Estudiante>) => api.post<Estudiante>('/estudiantes', data),
  update: (id: number, data: Partial<Estudiante>) => api.put<Estudiante>(`/estudiantes/${id}`, data),
  delete: (id: number) => api.delete(`/estudiantes/${id}`),
};

// ===== Profesores =====
export const profesoresApi = {
  getAll: () => api.get<Profesor[]>('/profesores'),
  getById: (id: number) => api.get<Profesor>(`/profesores/${id}`),
  update: (id: number, data: Partial<Profesor>) => api.put<Profesor>(`/profesores/${id}`, data),
  delete: (id: number) => api.delete(`/profesores/${id}`),
};

// ===== Asignaturas =====
export const asignaturasApi = {
  getAll: () => api.get<Asignatura[]>('/asignaturas'),
  getById: (id: number) => api.get<Asignatura>(`/asignaturas/${id}`),
  search: (q: string) => api.get<Asignatura[]>(`/asignaturas/search?q=${q}`),
  create: (data: Partial<Asignatura>) => api.post<Asignatura>('/asignaturas', data),
  update: (id: number, data: Partial<Asignatura>) => api.put<Asignatura>(`/asignaturas/${id}`, data),
  delete: (id: number) => api.delete(`/asignaturas/${id}`),
  addEstudiante: (asignaturaId: number, estudianteId: number) =>
    api.post<Asignatura>(`/asignaturas/${asignaturaId}/estudiantes/${estudianteId}`),
  removeEstudiante: (asignaturaId: number, estudianteId: number) =>
    api.delete<Asignatura>(`/asignaturas/${asignaturaId}/estudiantes/${estudianteId}`),
};

// ===== Clases =====
export const clasesApi = {
  getAll: () => api.get<Clase[]>('/clases'),
  getById: (id: number) => api.get<Clase>(`/clases/${id}`),
  getByAlumno: (username: string) => api.get<Clase[]>(`/clases/alumno/${username}`),
  getByProfesor: (username: string) => api.get<Clase[]>(`/clases/profesor/${username}`),
  getByEstado: (estado: string) => api.get<Clase[]>(`/clases/estado/${estado}`),
  create: (data: ClaseCreateRequest) => api.post<Clase>('/clases', data),
  updateEstado: (id: number, estado: string) => api.patch<Clase>(`/clases/${id}/estado?estado=${estado}`),
  delete: (id: number) => api.delete(`/clases/${id}`),
};

// ===== Valoraciones =====
export const valoracionesApi = {
  getAll: () => api.get<Valoracion[]>('/valoraciones'),
  getByProfesor: (profesorId: number) => api.get<Valoracion[]>(`/valoraciones/profesor/${profesorId}`),
  getByAsignatura: (asignaturaId: number) => api.get<Valoracion[]>(`/valoraciones/asignatura/${asignaturaId}`),
  create: (data: Partial<Valoracion>) => api.post<Valoracion>('/valoraciones', data),
  delete: (id: number) => api.delete(`/valoraciones/${id}`),
};
