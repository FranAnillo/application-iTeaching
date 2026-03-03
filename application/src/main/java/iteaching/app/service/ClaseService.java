package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Clase;
import iteaching.app.Models.EstadoClase;
import iteaching.app.Models.Estudiante;
import iteaching.app.Models.Profesor;
import iteaching.app.dto.ClaseCreateRequest;
import iteaching.app.dto.ClaseDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.ClaseRepository;
import iteaching.app.repository.EstudianteRepository;
import iteaching.app.repository.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final EstudianteRepository estudianteRepository;
    private final ProfesorRepository profesorRepository;
    private final AsignaturaRepository asignaturaRepository;

    public ClaseService(ClaseRepository claseRepository, EstudianteRepository estudianteRepository,
                        ProfesorRepository profesorRepository, AsignaturaRepository asignaturaRepository) {
        this.claseRepository = claseRepository;
        this.estudianteRepository = estudianteRepository;
        this.profesorRepository = profesorRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    public List<ClaseDTO> findAll() {
        return claseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClaseDTO findById(Long id) {
        Clase clase = claseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada con id: " + id));
        return toDTO(clase);
    }

    public List<ClaseDTO> findByAlumno(String username) {
        return claseRepository.findByAlumnoUsername(username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ClaseDTO> findByProfesor(String username) {
        return claseRepository.findByProfesorUsername(username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ClaseDTO> findByEstado(EstadoClase estado) {
        return claseRepository.findByEstadoClase(estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaseDTO create(ClaseCreateRequest request) {
        Estudiante alumno = estudianteRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        Profesor profesor = profesorRepository.findById(request.getProfesorId())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        Asignatura asignatura = asignaturaRepository.findById(request.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Clase clase = new Clase();
        clase.setHoraComienzo(request.getHoraComienzo());
        clase.setHoraFin(request.getHoraFin());
        clase.setAlumno(alumno);
        clase.setProfesor(profesor);
        clase.setAsignatura(asignatura);
        clase.setAceptacionAlumno(true);
        clase.setAceptacionProfesor(false);
        clase.setEstadoClase(EstadoClase.SOLICITADA);

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
        if (!claseRepository.existsById(id)) {
            throw new RuntimeException("Clase no encontrada con id: " + id);
        }
        claseRepository.deleteById(id);
    }

    private ClaseDTO toDTO(Clase c) {
        ClaseDTO dto = new ClaseDTO();
        dto.setId(c.getId());
        dto.setHoraComienzo(c.getHoraComienzo());
        dto.setHoraFin(c.getHoraFin());
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
        return dto;
    }
}
