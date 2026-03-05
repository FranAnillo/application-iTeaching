package iteaching.app.service;

import iteaching.app.Models.Asistencia;
import iteaching.app.Models.Persona;
import iteaching.app.dto.AsistenciaDTO;
import iteaching.app.repository.AsistenciaRepository;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final PersonaRepository personaRepository;
    private final AsignaturaRepository asignaturaRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                             PersonaRepository personaRepository,
                             AsignaturaRepository asignaturaRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.personaRepository = personaRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    public List<AsistenciaDTO> getByAsignatura(Long asignaturaId) {
        return asistenciaRepository.findByAsignaturaIdOrderByFechaDesc(asignaturaId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<AsistenciaDTO> getByAsignaturaAndFecha(Long asignaturaId, LocalDate fecha) {
        return asistenciaRepository.findByAsignaturaIdAndFecha(asignaturaId, fecha).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<AsistenciaDTO> getByEstudiante(Long estudianteId, Long asignaturaId) {
        return asistenciaRepository.findByEstudianteIdAndAsignaturaIdOrderByFechaDesc(estudianteId, asignaturaId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public AsistenciaDTO registrar(AsistenciaDTO dto, String username) {
        Persona registradoPor = personaRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check if already exists for this date
        var existing = asistenciaRepository.findByEstudianteIdAndAsignaturaIdAndFecha(
            dto.getEstudianteId(), dto.getAsignaturaId(), LocalDate.parse(dto.getFecha()));
        
        Asistencia asistencia;
        if (existing.isPresent()) {
            asistencia = existing.get();
        } else {
            asistencia = new Asistencia();
            asistencia.setEstudiante(personaRepository.findById(dto.getEstudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado")));
            asistencia.setAsignatura(asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada")));
            asistencia.setFecha(LocalDate.parse(dto.getFecha()));
        }

        asistencia.setEstado(Asistencia.EstadoAsistencia.valueOf(dto.getEstado()));
        asistencia.setObservacion(InputSanitizer.sanitize(dto.getObservacion()));
        asistencia.setRegistradoPor(registradoPor);

        return toDTO(asistenciaRepository.save(asistencia));
    }

    public void registrarLote(List<AsistenciaDTO> dtos, String username) {
        for (AsistenciaDTO dto : dtos) {
            registrar(dto, username);
        }
    }

    private AsistenciaDTO toDTO(Asistencia a) {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setId(a.getId());
        dto.setFecha(a.getFecha().toString());
        dto.setEstado(a.getEstado().name());
        dto.setObservacion(a.getObservacion());
        dto.setEstudianteId(a.getEstudiante().getId());
        dto.setEstudianteNombre(a.getEstudiante().getNombreCompleto());
        dto.setAsignaturaId(a.getAsignatura().getId());
        dto.setAsignaturaNombre(a.getAsignatura().getNombre());
        dto.setRegistradoPorId(a.getRegistradoPor().getId());
        dto.setRegistradoPorNombre(a.getRegistradoPor().getNombreCompleto());
        return dto;
    }
}
