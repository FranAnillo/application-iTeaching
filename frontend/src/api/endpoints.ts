import api from './client';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  Usuario,
  Asignatura,
  Clase,
  ClaseCreateRequest,
  Valoracion,
  Material,
  Anuncio,
  Tarea,
  Entrega,
  ForoTema,
  ForoRespuesta,
  Grupo,
  Carpeta,
} from '../types';

// ===== Auth =====
export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
};

// ===== Usuarios =====
export const usuariosApi = {
  getAll: () => api.get<Usuario[]>('/usuarios'),
  getById: (id: number) => api.get<Usuario>(`/usuarios/${id}`),
  me: () => api.get<Usuario>('/usuarios/me'),
  search: (q: string) => api.get<Usuario[]>(`/usuarios/search?q=${q}`),
};

// ===== Asignaturas =====
export const asignaturasApi = {
  getAll: () => api.get<Asignatura[]>('/asignaturas'),
  getById: (id: number) => api.get<Asignatura>(`/asignaturas/${id}`),
  search: (q: string) => api.get<Asignatura[]>(`/asignaturas/search?q=${q}`),
  create: (data: Partial<Asignatura>) => api.post<Asignatura>('/asignaturas', data),
  update: (id: number, data: Partial<Asignatura>) => api.put<Asignatura>(`/asignaturas/${id}`, data),
  delete: (id: number) => api.delete(`/asignaturas/${id}`),
  importCsv: (file: File) => {
    var formData = new FormData();
    formData.append('file', file);
    return api.post<Asignatura[]>('/asignaturas/import-csv', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  addProfesor: (asignaturaId: number, personaId: number) =>
    api.post<Asignatura>(`/asignaturas/${asignaturaId}/profesores/${personaId}`),
  removeProfesor: (asignaturaId: number, personaId: number) =>
    api.delete<Asignatura>(`/asignaturas/${asignaturaId}/profesores/${personaId}`),
  addEstudiante: (asignaturaId: number, personaId: number) =>
    api.post<Asignatura>(`/asignaturas/${asignaturaId}/estudiantes/${personaId}`),
  removeEstudiante: (asignaturaId: number, personaId: number) =>
    api.delete<Asignatura>(`/asignaturas/${asignaturaId}/estudiantes/${personaId}`),
  inscribirse: (asignaturaId: number) =>
    api.post<Asignatura>(`/asignaturas/${asignaturaId}/inscribirse`),
  desinscribirse: (asignaturaId: number) =>
    api.delete<Asignatura>(`/asignaturas/${asignaturaId}/desinscribirse`),
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
  descargarHorarioPdf: (asignaturaId: number) =>
    api.get(`/clases/pdf/asignatura/${asignaturaId}`, { responseType: 'blob' }),
  descargarHorarioCompletoPdf: (asignaturaId: number) =>
    api.get(`/clases/pdf/asignatura/${asignaturaId}/completo`, { responseType: 'blob' }),
};

// ===== Valoraciones (anónimas) =====
export const valoracionesApi = {
  getAll: () => api.get<Valoracion[]>('/valoraciones'),
  getByProfesor: (profesorId: number) => api.get<Valoracion[]>(`/valoraciones/profesor/${profesorId}`),
  getByAsignatura: (asignaturaId: number) => api.get<Valoracion[]>(`/valoraciones/asignatura/${asignaturaId}`),
  getByProfesorAndAsignatura: (profesorId: number, asignaturaId: number) => api.get<Valoracion[]>(`/valoraciones/profesor/${profesorId}/asignatura/${asignaturaId}`),
  getPromedio: (profesorId: number) => api.get<{ promedio: number }>(`/valoraciones/profesor/${profesorId}/promedio`),
  create: (data: Partial<Valoracion>) => api.post<Valoracion>('/valoraciones', data),
  delete: (id: number) => api.delete(`/valoraciones/${id}`),
};

// ===== Materiales =====
export const materialesApi = {
  getAll: () => api.get<Material[]>('/materiales'),
  getById: (id: number) => api.get<Material>(`/materiales/${id}`),
  getByAutor: (autorId: number) => api.get<Material[]>(`/materiales/autor/${autorId}`),
  getMisMateriales: () => api.get<Material[]>('/materiales/mis-materiales'),
  getByAsignatura: (asignaturaId: number) => api.get<Material[]>(`/materiales/asignatura/${asignaturaId}`),
  search: (q: string) => api.get<Material[]>(`/materiales/search?q=${q}`),
  create: (data: Partial<Material>) => api.post<Material>('/materiales', data),
  update: (id: number, data: Partial<Material>) => api.put<Material>(`/materiales/${id}`, data),
  delete: (id: number) => api.delete(`/materiales/${id}`),
};

// ===== Admin =====
export const adminApi = {
  cancelarClase: (id: number) => api.patch<Clase>(`/admin/clases/${id}/cancelar`),
};

// ===== Anuncios =====
export const anunciosApi = {
  getByAsignatura: (asignaturaId: number) => api.get<Anuncio[]>(`/anuncios/asignatura/${asignaturaId}`),
  getById: (id: number) => api.get<Anuncio>(`/anuncios/${id}`),
  create: (data: Partial<Anuncio>) => api.post<Anuncio>('/anuncios', data),
  delete: (id: number) => api.delete(`/anuncios/${id}`),
};

// ===== Tareas =====
export const tareasApi = {
  getByAsignatura: (asignaturaId: number) => api.get<Tarea[]>(`/tareas/asignatura/${asignaturaId}`),
  getById: (id: number) => api.get<Tarea>(`/tareas/${id}`),
  create: (data: Partial<Tarea>) => api.post<Tarea>('/tareas', data),
  delete: (id: number) => api.delete(`/tareas/${id}`),
};

// ===== Entregas =====
export const entregasApi = {
  getByTarea: (tareaId: number) => api.get<Entrega[]>(`/entregas/tarea/${tareaId}`),
  getMisEntregas: () => api.get<Entrega[]>('/entregas/mis-entregas'),
  submit: (data: Partial<Entrega>) => api.post<Entrega>('/entregas', data),
  calificar: (id: number, calificacion: number, comentario?: string) =>
    api.patch<Entrega>(`/entregas/${id}/calificar?calificacion=${calificacion}${comentario ? '&comentario=' + encodeURIComponent(comentario) : ''}`),
};

// ===== Foro =====
export const foroApi = {
  getTemasByAsignatura: (asignaturaId: number) => api.get<ForoTema[]>(`/foro/asignatura/${asignaturaId}`),
  getTemaById: (id: number) => api.get<ForoTema>(`/foro/temas/${id}`),
  createTema: (data: Partial<ForoTema>) => api.post<ForoTema>('/foro/temas', data),
  createRespuesta: (data: Partial<ForoRespuesta>) => api.post<ForoRespuesta>('/foro/respuestas', data),
  deleteTema: (id: number) => api.delete(`/foro/temas/${id}`),
  deleteRespuesta: (id: number) => api.delete(`/foro/respuestas/${id}`),
};

// ===== Grupos =====
export const gruposApi = {
  getByAsignatura: (asignaturaId: number) => api.get<Grupo[]>(`/grupos/asignatura/${asignaturaId}`),
  getById: (id: number) => api.get<Grupo>(`/grupos/${id}`),
  create: (data: Partial<Grupo>) => api.post<Grupo>('/grupos', data),
  update: (id: number, data: Partial<Grupo>) => api.put<Grupo>(`/grupos/${id}`, data),
  delete: (id: number) => api.delete(`/grupos/${id}`),
  addMiembro: (grupoId: number, personaId: number) =>
    api.post<Grupo>(`/grupos/${grupoId}/miembros/${personaId}`),
  removeMiembro: (grupoId: number, personaId: number) =>
    api.delete<Grupo>(`/grupos/${grupoId}/miembros/${personaId}`),
  toggleInscribible: (grupoId: number) =>
    api.patch<Grupo>(`/grupos/${grupoId}/inscribible`),
  selfEnrol: (grupoId: number) =>
    api.post<Grupo>(`/grupos/${grupoId}/inscribirse`),
  selfUnenrol: (grupoId: number) =>
    api.delete<Grupo>(`/grupos/${grupoId}/desinscribirse`),
};

// ===== Carpetas =====
export const carpetasApi = {
  getByAsignatura: (asignaturaId: number) => api.get<Carpeta[]>(`/carpetas/asignatura/${asignaturaId}`),
  getRootByAsignatura: (asignaturaId: number) => api.get<Carpeta[]>(`/carpetas/asignatura/${asignaturaId}/root`),
  getSubcarpetas: (id: number) => api.get<Carpeta[]>(`/carpetas/${id}/subcarpetas`),
  getById: (id: number) => api.get<Carpeta>(`/carpetas/${id}`),
  create: (data: Partial<Carpeta>) => api.post<Carpeta>('/carpetas', data),
  update: (id: number, data: Partial<Carpeta>) => api.put<Carpeta>(`/carpetas/${id}`, data),
  delete: (id: number) => api.delete(`/carpetas/${id}`),
};
