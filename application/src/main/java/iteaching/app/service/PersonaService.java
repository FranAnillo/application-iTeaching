package iteaching.app.service;

import iteaching.app.Models.Persona;
import iteaching.app.dto.UsuarioDTO;
import iteaching.app.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public List<UsuarioDTO> findAll() {
        return personaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UsuarioDTO findById(Long id) {
        return personaRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    public UsuarioDTO findByUsername(String username) {
        return personaRepository.findByUsername(username).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public List<UsuarioDTO> search(String query) {
        List<Persona> byNombre = personaRepository.findByNombreContainingIgnoreCase(query);
        List<Persona> byApellido = personaRepository.findByApellidoContainingIgnoreCase(query);
        return byNombre.stream()
                .collect(Collectors.toMap(Persona::getId, p -> p, (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values().stream()
                .collect(Collectors.toCollection(java.util.ArrayList::new))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private UsuarioDTO toDTO(Persona p) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(p.getId());
        dto.setUsername(p.getUsername());
        dto.setNombre(p.getNombre());
        dto.setApellido(p.getApellido());
        dto.setEmail(p.getEmail());
        dto.setTelefono(p.getTelefono());
        dto.setRole(p.getRole() != null ? p.getRole().name() : null);
        dto.setPuntuacion(p.getPuntuacion());
        dto.setAvatar(p.getAvatar());
        return dto;
    }
}
