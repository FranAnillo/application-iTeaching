package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Estudiante;
import iteaching.app.dto.AsignaturaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final EstudianteRepository estudianteRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository, EstudianteRepository estudianteRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public List<AsignaturaDTO> findAll() {
        return asignaturaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AsignaturaDTO findById(Long id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
        return toDTO(asignatura);
    }

    public List<AsignaturaDTO> search(String query) {
        return asignaturaRepository.findByNombreContainingIgnoreCase(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AsignaturaDTO create(AsignaturaDTO dto) {
        Asignatura asignatura = new Asignatura();
        updateEntityFromDTO(asignatura, dto);
        return toDTO(asignaturaRepository.save(asignatura));
    }

    @Transactional
    public AsignaturaDTO update(Long id, AsignaturaDTO dto) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
        updateEntityFromDTO(asignatura, dto);
        return toDTO(asignaturaRepository.save(asignatura));
    }

    @Transactional
    public void delete(Long id) {
        if (!asignaturaRepository.existsById(id)) {
            throw new RuntimeException("Asignatura no encontrada con id: " + id);
        }
        asignaturaRepository.deleteById(id);
    }

    @Transactional
    public AsignaturaDTO addEstudiante(Long asignaturaId, Long estudianteId) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        asignatura.getEstudiantes().add(estudiante);
        return toDTO(asignaturaRepository.save(asignatura));
    }

    @Transactional
    public AsignaturaDTO removeEstudiante(Long asignaturaId, Long estudianteId) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        asignatura.getEstudiantes().removeIf(e -> e.getId().equals(estudianteId));
        return toDTO(asignaturaRepository.save(asignatura));
    }

    private AsignaturaDTO toDTO(Asignatura a) {
        Set<Long> estudianteIds = a.getEstudiantes() != null
                ? a.getEstudiantes().stream().map(Estudiante::getId).collect(Collectors.toSet())
                : new HashSet<>();
        return new AsignaturaDTO(a.getId(), a.getNombre(), a.getDescripcion(), estudianteIds);
    }

    private void updateEntityFromDTO(Asignatura entity, AsignaturaDTO dto) {
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        if (dto.getEstudianteIds() != null && !dto.getEstudianteIds().isEmpty()) {
            Set<Estudiante> estudiantes = new HashSet<>(estudianteRepository.findAllById(dto.getEstudianteIds()));
            entity.setEstudiantes(estudiantes);
        }
    }
}
