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
export interface Usuario {
  id: number;
  username: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  role: string;
  puntuacion: number;
  avatar: string | null;
}

export interface Asignatura {
  id: number;
  nombre: string;
  siglas: string;
  descripcion: string;
  url?: string;
  creadorId?: number;
  creadorNombre?: string;
  profesorIds: number[];
  estudianteIds: number[];
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
  puntosMejora: string;
  fechaCreacion: string;
  profesorId: number;
  profesorNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
  // Anónima: sin datos del alumno
}

// ===== Material =====
export interface Material {
  id: number;
  titulo: string;
  descripcion: string;
  urlRecurso: string;
  tipo: string;
  fechaCreacion: string;
  autorId: number;
  autorNombre: string;
  asignaturaId: number | null;
  asignaturaNombre: string | null;
  carpetaId: number | null;
  carpetaNombre: string | null;
}

// ===== Grupo (Group) =====
export interface Grupo {
  id: number;
  nombre: string;
  tipo: string; // TEORIA | PRACTICA
  inscribible: boolean;
  asignaturaId: number;
  asignaturaNombre: string;
  miembroIds: number[];
}

// ===== Carpeta (Folder) =====
export interface Carpeta {
  id: number;
  nombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
  padreId: number | null;
  padreNombre: string | null;
}

// ===== Anuncio (Announcement) =====
export interface Anuncio {
  id: number;
  titulo: string;
  contenido: string;
  fechaCreacion: string;
  importante: boolean;
  asignaturaId?: number;
  asignaturaNombre?: string;
  autorId?: number;
  autorNombre?: string;
  global?: boolean;
  destinatarios?: string;
}

// ===== Tarea (Assignment) =====
export interface Tarea {
  id: number;
  titulo: string;
  descripcion: string;
  fechaCreacion: string;
  fechaEntrega: string;
  puntuacionMaxima: number;
  tipoTarea: string; // TAREA | EVALUACION | SIMULACRO
  asignaturaId: number;
  asignaturaNombre: string;
  creadorId: number;
  creadorNombre: string;
  totalEntregas: number;
}

// ===== Entrega (Submission) =====
export interface Entrega {
  id: number;
  contenido: string;
  urlAdjunto: string;
  fechaEntrega: string;
  calificacion: number | null;
  comentarioProfesor: string | null;
  tareaId: number;
  tareaTitulo: string;
  estudianteId: number;
  estudianteNombre: string;
}

// ===== Foro (Forum) =====
export interface ForoTema {
  id: number;
  titulo: string;
  contenido: string;
  fechaCreacion: string;
  fijado: boolean;
  asignaturaId: number;
  asignaturaNombre: string;
  autorId: number;
  autorNombre: string;
  totalRespuestas: number;
  respuestas: ForoRespuesta[];
}

export interface ForoRespuesta {
  id: number;
  contenido: string;
  fechaCreacion: string;
  temaId: number;
  autorId: number;
  autorNombre: string;
}

// ===== API Error =====
export interface ApiError {
  timestamp: string;
  message: string;
  status: number;
  errors?: Record<string, string>;
}

// ===== Mensaje (Message) =====
export interface Mensaje {
  id: number;
  contenido: string;
  fechaEnvio: string;
  leido: boolean;
  remitenteId: number;
  remitenteNombre: string;
  destinatarioId: number;
  destinatarioNombre: string;
  asignaturaId: number | null;
  asignaturaNombre: string | null;
}

// ===== Notificacion =====
export interface Notificacion {
  id: number;
  titulo: string;
  mensaje: string;
  tipo: string;
  fechaCreacion: string;
  leida: boolean;
  enlace: string | null;
  usuarioId: number;
}

// ===== Asistencia (Attendance) =====
export interface AsistenciaRecord {
  id: number;
  fecha: string;
  estado: string;
  observacion: string | null;
  estudianteId: number;
  estudianteNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
  registradoPorId: number;
  registradoPorNombre: string;
}

// ===== Progreso (Progress) =====
export interface Progreso {
  asignaturaId: number;
  asignaturaNombre: string;
  totalTareas: number;
  tareasEntregadas: number;
  tareasCalificadas: number;
  promedioCalificaciones: number;
  totalClases: number;
  clasesAsistidas: number;
  porcentajeAsistencia: number;
  porcentajeProgreso: number;
}

// ===== Rubrica =====
export interface Rubrica {
  id: number;
  nombre: string;
  descripcion: string;
  tareaId: number;
  tareaTitulo: string;
  criterios: CriterioRubrica[];
}

export interface CriterioRubrica {
  id: number;
  nombre: string;
  descripcion: string;
  puntuacionMaxima: number;
  orden: number;
  nivelExcelente: string;
  nivelBueno: string;
  nivelSuficiente: string;
  nivelInsuficiente: string;
}

// ===== Logro (Achievement) =====
export interface Logro {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  icono: string;
  categoria: string;
  valorObjetivo: number;
  obtenido: boolean;
}
