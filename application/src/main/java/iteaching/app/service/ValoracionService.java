package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.Models.Valoracion;
import iteaching.app.dto.ValoracionDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.ValoracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final PersonaRepository personaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final ContentModerationService moderationService;

    public ValoracionService(ValoracionRepository valoracionRepository, PersonaRepository personaRepository,
                             AsignaturaRepository asignaturaRepository, ContentModerationService moderationService) {
        this.valoracionRepository = valoracionRepository;
        this.personaRepository = personaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.moderationService = moderationService;
    }

    public List<ValoracionDTO> findAll() {
        return valoracionRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ValoracionDTO> findByProfesor(Long profesorId) {
        Persona p = personaRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        return valoracionRepository.findByProfesor(p).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ValoracionDTO> findByAsignatura(Long asignaturaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        return valoracionRepository.findByAsignatura(a).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ValoracionDTO> findByProfesorAndAsignatura(Long profesorId, Long asignaturaId) {
        Persona p = personaRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        return valoracionRepository.findByProfesorAndAsignatura(p, a).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Double getPromedioByProfesor(Long profesorId) {
        Persona p = personaRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        Double promedio = valoracionRepository.getPromedioByProfesor(p);
        return promedio != null ? Math.round(promedio * 100.0) / 100.0 : 0.0;
    }

    /**
     * Crea una valoración anónima de un profesor.
     * Validaciones:
     * - Solo estudiantes pueden valorar
     * - El estudiante debe estar matriculado en la asignatura
     * - El profesor valorado debe estar asignado a la asignatura
     * - No se permiten valoraciones duplicadas (mismo alumno + profesor + asignatura)
     * - El contenido se modera con IA para bloquear insultos
     */
    @Transactional
    public ValoracionDTO create(ValoracionDTO dto, String username) {
        // 1. Obtener el estudiante autenticado
        Persona alumno = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Verificar que es un estudiante
        if (alumno.getRole() != Usuarios.Role.ROLE_ESTUDIANTE) {
            throw new RuntimeException("Solo los estudiantes pueden valorar a los profesores");
        }

        // 3. Obtener profesor y asignatura
        Persona profesor = personaRepository.findById(dto.getProfesorId())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        // 4. Verificar que el profesor está asignado a la asignatura
        if (!asignatura.getProfesores().contains(profesor)) {
            throw new RuntimeException("El profesor seleccionado no imparte esta asignatura");
        }

        // 5. Verificar que el estudiante está matriculado en la asignatura
        if (!asignatura.getEstudiantes().contains(alumno)) {
            throw new RuntimeException("Solo los estudiantes matriculados en esta asignatura pueden valorar a sus profesores");
        }

        // 6. Verificar que no existe ya una valoración del mismo alumno al mismo profesor en la misma asignatura
        if (valoracionRepository.findByAlumnoAndProfesorAndAsignatura(alumno, profesor, asignatura).isPresent()) {
            throw new RuntimeException("Ya has valorado a este profesor en esta asignatura");
        }

        // 7. Moderar el comentario con IA
        if (dto.getComentario() != null && !dto.getComentario().trim().isEmpty()) {
            ContentModerationService.ModerationResult result = moderationService.moderate(dto.getComentario());
            if (!result.isApproved()) {
                throw new RuntimeException(result.getReason());
            }
        }

        // 8. Moderar los puntos de mejora con IA
        if (dto.getPuntosMejora() != null && !dto.getPuntosMejora().trim().isEmpty()) {
            ContentModerationService.ModerationResult result = moderationService.moderate(dto.getPuntosMejora());
            if (!result.isApproved()) {
                throw new RuntimeException("Puntos de mejora: " + result.getReason());
            }
        }

        // 9. Crear la valoración
        Valoracion v = new Valoracion();
        v.setPuntuacion(dto.getPuntuacion());
        v.setComentario(dto.getComentario());
        v.setPuntosMejora(dto.getPuntosMejora());
        v.setFechaCreacion(LocalDateTime.now());
        v.setProfesor(profesor);
        v.setAlumno(alumno); // se guarda internamente pero NO se expone en el DTO
        v.setAsignatura(asignatura);

        Valoracion saved = valoracionRepository.save(v);

        // 10. Actualizar puntuación media del profesor
        Double promedio = valoracionRepository.getPromedioByProfesor(profesor);
        if (promedio != null) {
            profesor.setPuntuacion(Math.round(promedio * 100.0) / 100.0);
            personaRepository.save(profesor);
        }

        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Valoracion v = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada con id: " + id));
        Persona profesor = v.getProfesor();
        valoracionRepository.delete(v);

        // Recalcular media del profesor
        Double promedio = valoracionRepository.getPromedioByProfesor(profesor);
        profesor.setPuntuacion(promedio != null ? Math.round(promedio * 100.0) / 100.0 : 0.0);
        personaRepository.save(profesor);
    }

    /**
     * Convierte a DTO ANÓNIMO: nunca expone el id o nombre del alumno.
     */
    private ValoracionDTO toDTO(Valoracion v) {
        ValoracionDTO dto = new ValoracionDTO();
        dto.setId(v.getId());
        dto.setPuntuacion(v.getPuntuacion());
        dto.setComentario(v.getComentario());
        dto.setPuntosMejora(v.getPuntosMejora());
        dto.setFechaCreacion(v.getFechaCreacion() != null ? v.getFechaCreacion().toString() : null);
        if (v.getProfesor() != null) {
            dto.setProfesorId(v.getProfesor().getId());
            dto.setProfesorNombre(v.getProfesor().getNombreCompleto());
        }
        if (v.getAsignatura() != null) {
            dto.setAsignaturaId(v.getAsignatura().getId());
            dto.setAsignaturaNombre(v.getAsignatura().getNombre());
        }
        // NO se incluyen datos del alumno → valoración anónima
        return dto;
    }
}
