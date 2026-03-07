package iteaching.app.service;

import iteaching.app.Models.Entrega;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Tarea;
import iteaching.app.dto.EntregaDTO;
import iteaching.app.repository.EntregaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.TareaRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import iteaching.app.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final TareaRepository tareaRepository;
    private final PersonaRepository personaRepository;

    @Autowired
    private NotificationService notificationService;

    public EntregaService(EntregaRepository entregaRepository,
                          TareaRepository tareaRepository,
                          PersonaRepository personaRepository) {
        this.entregaRepository = entregaRepository;
        this.tareaRepository = tareaRepository;
        this.personaRepository = personaRepository;
    }

    public List<EntregaDTO> findByTarea(Long tareaId) {
        return entregaRepository.findByTareaId(tareaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EntregaDTO> findByEstudiante(String username) {
        return entregaRepository.findByEstudianteUsername(username)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public EntregaDTO submit(EntregaDTO dto, String username) {
        Tarea tarea = tareaRepository.findById(dto.getTareaId() != null ? dto.getTareaId() : 0L)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        Persona estudiante = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check if already submitted
        entregaRepository.findByTareaIdAndEstudianteId(tarea.getId(), estudiante.getId())
                .ifPresent(e -> { throw new RuntimeException("Ya has enviado una entrega para esta tarea"); });

        Entrega e = new Entrega();
        e.setContenido(InputSanitizer.sanitize(dto.getContenido()));
        e.setUrlAdjunto(InputSanitizer.sanitizeUrl(dto.getUrlAdjunto()));
        e.setFechaEntrega(LocalDateTime.now());
        e.setTarea(tarea);
        e.setEstudiante(estudiante);

        EntregaDTO result = toDTO(entregaRepository.save(e));
        // Notificar al estudiante
        notificationService.notifyMaterialUploaded(estudiante.getEmail(), tarea.getAsignatura().getNombre(), tarea.getTitulo());
        return result;
    }

    /** Instructor grades a submission */
    @Transactional
    public EntregaDTO calificar(Long entregaId, Double calificacion, String comentario) {
        Entrega e = entregaRepository.findById(entregaId != null ? entregaId : 0L)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        e.setCalificacion(calificacion);
        e.setComentarioProfesor(InputSanitizer.sanitize(comentario));
        EntregaDTO result = toDTO(entregaRepository.save(e));
        // Notificar calificación
        notificationService.notifyGrade(e.getEstudiante().getEmail(), e.getTarea().getAsignatura().getNombre(), e.getTarea().getTitulo(), String.valueOf(calificacion));
        return result;
    }

    private EntregaDTO toDTO(Entrega e) {
        EntregaDTO dto = new EntregaDTO();
        dto.setId(e.getId());
        dto.setContenido(e.getContenido());
        dto.setUrlAdjunto(e.getUrlAdjunto());
        dto.setFechaEntrega(e.getFechaEntrega().toString());
        dto.setCalificacion(e.getCalificacion());
        dto.setComentarioProfesor(e.getComentarioProfesor());
        dto.setTareaId(e.getTarea().getId());
        dto.setTareaTitulo(e.getTarea().getTitulo());
        dto.setEstudianteId(e.getEstudiante().getId());
        dto.setEstudianteNombre(e.getEstudiante().getNombreCompleto());
        return dto;
    }
}
