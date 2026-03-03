package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Estudiante;
import iteaching.app.Models.Profesor;
import iteaching.app.Models.Valoracion;
import iteaching.app.dto.ValoracionDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.EstudianteRepository;
import iteaching.app.repository.ProfesorRepository;
import iteaching.app.repository.ValoracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final ProfesorRepository profesorRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignaturaRepository asignaturaRepository;

    public ValoracionService(ValoracionRepository valoracionRepository, ProfesorRepository profesorRepository,
                             EstudianteRepository estudianteRepository, AsignaturaRepository asignaturaRepository) {
        this.valoracionRepository = valoracionRepository;
        this.profesorRepository = profesorRepository;
        this.estudianteRepository = estudianteRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    public List<ValoracionDTO> findAll() {
        return valoracionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ValoracionDTO> findByProfesor(Long profesorId) {
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        return valoracionRepository.findByProfesor(profesor).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ValoracionDTO> findByAsignatura(Long asignaturaId) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        return valoracionRepository.findByAsignatura(asignatura).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ValoracionDTO create(ValoracionDTO dto) {
        Profesor profesor = profesorRepository.findById(dto.getProfesorId())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
        Estudiante alumno = estudianteRepository.findById(dto.getAlumnoId())
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Valoracion valoracion = new Valoracion();
        valoracion.setPuntuacion(dto.getPuntuacion());
        valoracion.setComentario(dto.getComentario());
        valoracion.setProfesor(profesor);
        valoracion.setAlumno(alumno);
        valoracion.setAsignatura(asignatura);

        return toDTO(valoracionRepository.save(valoracion));
    }

    @Transactional
    public void delete(Long id) {
        if (!valoracionRepository.existsById(id)) {
            throw new RuntimeException("Valoración no encontrada con id: " + id);
        }
        valoracionRepository.deleteById(id);
    }

    private ValoracionDTO toDTO(Valoracion v) {
        ValoracionDTO dto = new ValoracionDTO();
        dto.setId(v.getId());
        dto.setPuntuacion(v.getPuntuacion());
        dto.setComentario(v.getComentario());
        if (v.getProfesor() != null) {
            dto.setProfesorId(v.getProfesor().getId());
            dto.setProfesorNombre(v.getProfesor().getNombreCompleto());
        }
        if (v.getAsignatura() != null) {
            dto.setAsignaturaId(v.getAsignatura().getId());
            dto.setAsignaturaNombre(v.getAsignatura().getNombre());
        }
        if (v.getAlumno() != null) {
            dto.setAlumnoId(v.getAlumno().getId());
            dto.setAlumnoNombre(v.getAlumno().getNombreCompleto());
        }
        return dto;
    }
}
