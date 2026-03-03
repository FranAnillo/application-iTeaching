package iteaching.app.service;

import iteaching.app.Models.Entrega;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Tarea;
import iteaching.app.dto.EntregaDTO;
import iteaching.app.repository.EntregaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final TareaRepository tareaRepository;
    private final PersonaRepository personaRepository;

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
        Tarea tarea = tareaRepository.findById(dto.getTareaId())
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        Persona estudiante = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check if already submitted
        entregaRepository.findByTareaIdAndEstudianteId(tarea.getId(), estudiante.getId())
                .ifPresent(e -> { throw new RuntimeException("Ya has enviado una entrega para esta tarea"); });

        Entrega e = new Entrega();
        e.setContenido(dto.getContenido());
        e.setUrlAdjunto(dto.getUrlAdjunto());
        e.setFechaEntrega(LocalDateTime.now());
        e.setTarea(tarea);
        e.setEstudiante(estudiante);

        return toDTO(entregaRepository.save(e));
    }

    /** Instructor grades a submission */
    @Transactional
    public EntregaDTO calificar(Long entregaId, Double calificacion, String comentario) {
        Entrega e = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        e.setCalificacion(calificacion);
        e.setComentarioProfesor(comentario);
        return toDTO(entregaRepository.save(e));
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
