package iteaching.app.service;

import iteaching.app.Models.Profesor;
import iteaching.app.dto.ProfesorDTO;
import iteaching.app.repository.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesorService {

    private final ProfesorRepository profesorRepository;

    public ProfesorService(ProfesorRepository profesorRepository) {
        this.profesorRepository = profesorRepository;
    }

    public List<ProfesorDTO> findAll() {
        return profesorRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProfesorDTO findById(Long id) {
        Profesor profesor = profesorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado con id: " + id));
        return toDTO(profesor);
    }

    public ProfesorDTO findByUsername(String username) {
        Profesor profesor = profesorRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado: " + username));
        return toDTO(profesor);
    }

    @Transactional
    public ProfesorDTO update(Long id, ProfesorDTO dto) {
        Profesor profesor = profesorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado con id: " + id));
        profesor.setNombre(dto.getNombre());
        profesor.setApellido(dto.getApellido());
        profesor.setEmail(dto.getEmail());
        profesor.setTelefono(dto.getTelefono());
        return toDTO(profesorRepository.save(profesor));
    }

    @Transactional
    public void delete(Long id) {
        if (!profesorRepository.existsById(id)) {
            throw new RuntimeException("Profesor no encontrado con id: " + id);
        }
        profesorRepository.deleteById(id);
    }

    private ProfesorDTO toDTO(Profesor p) {
        return new ProfesorDTO(
                p.getId(), p.getNombre(), p.getApellido(), p.getEmail(),
                p.getTelefono(), p.getPuntuacion(), p.getDivision(), p.getAvatar()
        );
    }
}
