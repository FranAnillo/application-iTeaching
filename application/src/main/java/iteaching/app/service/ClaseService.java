package iteaching.app.service;

import iteaching.app.repository.*;
import iteaching.app.Models.*;
import iteaching.app.dto.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final PersonaRepository personaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final GrupoRepository grupoRepository;

    public ClaseService(ClaseRepository claseRepository, PersonaRepository personaRepository,
                        AsignaturaRepository asignaturaRepository, GrupoRepository grupoRepository) {
        this.claseRepository = claseRepository;
        this.personaRepository = personaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.grupoRepository = grupoRepository;
    }

    public List<ClaseDTO> findAll() {
        return claseRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ClaseDTO findById(Long id) {
        Clase c = claseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada con id: " + id));
        return toDTO(c);
    }

    public List<ClaseDTO> findByAlumno(String username) {
        return claseRepository.findByAlumnoUsername(username).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ClaseDTO> findByProfesor(String username) {
        return claseRepository.findByProfesorUsername(username).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ClaseDTO> findByEstado(EstadoClase estado) {
        return claseRepository.findByEstadoClase(estado).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ClaseDTO> findByAsignatura(Long asignaturaId) {
        return claseRepository.findByAsignaturaId(asignaturaId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ClaseDTO create(ClaseCreateRequest request) {
        Persona alumno = request.getAlumnoId() != null 
                ? personaRepository.findById(request.getAlumnoId()).orElse(null)
                : null;
        Persona profesor = personaRepository.findById(request.getProfesorId())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        Asignatura asignatura = asignaturaRepository.findById(request.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Grupo grupo = (request.getGrupoId() != null)
                ? grupoRepository.findById(request.getGrupoId()).orElse(null)
                : null;

        Clase clase = new Clase();
        clase.setTitulo(request.getTitulo() != null ? request.getTitulo() : "Sesion de " + asignatura.getNombre());
        clase.setAula(request.getAula());
        clase.setHoraComienzo(LocalDateTime.parse(request.getHoraComienzo()));
        clase.setHoraFin(LocalDateTime.parse(request.getHoraFin()));
        clase.setAlumno(alumno);
        clase.setProfesor(profesor);
        clase.setAsignatura(asignatura);
        clase.setGrupo(grupo);
        clase.setAceptacionAlumno(true);
        clase.setAceptacionProfesor(true); // Default to true if created by prof/admin
        clase.setEstadoClase(EstadoClase.ACEPTADA);

        return toDTO(claseRepository.save(clase));
    }

    @Transactional
    public ClaseDTO updateEstado(Long id, EstadoClase nuevoEstado) {
        Clase clase = claseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));
        clase.setEstadoClase(nuevoEstado);

        if (nuevoEstado == EstadoClase.ACEPTADA) {
            clase.setAceptacionProfesor(true);
        } else if (nuevoEstado == EstadoClase.CANCELADA || nuevoEstado == EstadoClase.RECHAZADA) {
            clase.setAceptacionProfesor(false);
        }

        return toDTO(claseRepository.save(clase));
    }

    @Transactional
    public void delete(Long id) {
        if (!claseRepository.existsById(id))
            throw new RuntimeException("Clase no encontrada con id: " + id);
        claseRepository.deleteById(id);
    }

    private ClaseDTO toDTO(Clase c) {
        ClaseDTO dto = new ClaseDTO();
        dto.setId(c.getId());
        dto.setTitulo(c.getTitulo());
        dto.setAula(c.getAula());
        dto.setHoraComienzo(c.getHoraComienzo() != null ? c.getHoraComienzo().toString() : null);
        dto.setHoraFin(c.getHoraFin() != null ? c.getHoraFin().toString() : null);
        dto.setAceptacionAlumno(c.getAceptacionAlumno());
        dto.setAceptacionProfesor(c.getAceptacionProfesor());
        dto.setEstadoClase(c.getEstadoClase() != null ? c.getEstadoClase().name() : null);
        if (c.getAlumno() != null) {
            dto.setAlumnoId(c.getAlumno().getId());
            dto.setAlumnoNombre(c.getAlumno().getNombreCompleto());
        }
        if (c.getProfesor() != null) {
            dto.setProfesorId(c.getProfesor().getId());
            dto.setProfesorNombre(c.getProfesor().getNombreCompleto());
        }
        if (c.getAsignatura() != null) {
            dto.setAsignaturaId(c.getAsignatura().getId());
            dto.setAsignaturaNombre(c.getAsignatura().getNombre());
        }
        if (c.getGrupo() != null) {
            dto.setGrupoId(c.getGrupo().getId());
            dto.setGrupoNombre(c.getGrupo().getNombre());
        }
        return dto;
    }
}
