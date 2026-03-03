package iteaching.app.service;

import iteaching.app.Models.Estudiante;
import iteaching.app.dto.EstudianteDTO;
import iteaching.app.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<EstudianteDTO> findAll() {
        return estudianteRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EstudianteDTO findById(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
        return toDTO(estudiante);
    }

    public List<EstudianteDTO> search(String query) {
        List<Estudiante> byNombre = estudianteRepository.findByNombreContainingIgnoreCase(query);
        List<Estudiante> byApellido = estudianteRepository.findByApellidoContainingIgnoreCase(query);
        byNombre.addAll(byApellido);
        return byNombre.stream()
                .distinct()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EstudianteDTO create(EstudianteDTO dto) {
        Estudiante estudiante = new Estudiante();
        updateEntityFromDTO(estudiante, dto);
        return toDTO(estudianteRepository.save(estudiante));
    }

    @Transactional
    public EstudianteDTO update(Long id, EstudianteDTO dto) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
        updateEntityFromDTO(estudiante, dto);
        return toDTO(estudianteRepository.save(estudiante));
    }

    @Transactional
    public void delete(Long id) {
        if (!estudianteRepository.existsById(id)) {
            throw new RuntimeException("Estudiante no encontrado con id: " + id);
        }
        estudianteRepository.deleteById(id);
    }

    private EstudianteDTO toDTO(Estudiante e) {
        return new EstudianteDTO(e.getId(), e.getNombre(), e.getApellido(), e.getEmail(), e.getTelefono());
    }

    private void updateEntityFromDTO(Estudiante entity, EstudianteDTO dto) {
        entity.setNombre(dto.getNombre());
        entity.setApellido(dto.getApellido());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());
    }
}
