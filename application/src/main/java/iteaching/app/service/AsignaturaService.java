package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AsignaturaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository, PersonaRepository personaRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
    }

    public List<AsignaturaDTO> findAll() {
        return asignaturaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AsignaturaDTO findById(Long id) {
        Asignatura a = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
        return toDTO(a);
    }

    public List<AsignaturaDTO> search(String query) {
        return asignaturaRepository.findByNombreContainingIgnoreCase(query).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public AsignaturaDTO create(AsignaturaDTO dto, String username) {
        Persona creador = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Asignatura a = new Asignatura();
        a.setNombre(dto.getNombre());
        a.setDescripcion(dto.getDescripcion());
        a.setUrl(dto.getUrl());
        a.setCreador(creador);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO update(Long id, AsignaturaDTO dto) {
        Asignatura a = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
        a.setNombre(dto.getNombre());
        a.setDescripcion(dto.getDescripcion());
        a.setUrl(dto.getUrl());
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        if (!asignaturaRepository.existsById(id))
            throw new RuntimeException("Asignatura no encontrada con id: " + id);
        asignaturaRepository.deleteById(id);
    }

    // ===== Profesor assignment (admin only) =====

    @Transactional
    public AsignaturaDTO addProfesor(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona p = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        if (p.getRole() != Usuarios.Role.ROLE_PROFESOR) {
            throw new RuntimeException("El usuario no tiene rol de profesor");
        }
        a.getProfesores().add(p);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO removeProfesor(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        a.getProfesores().removeIf(p -> p.getId().equals(personaId));
        return toDTO(asignaturaRepository.save(a));
    }

    // ===== Estudiante assignment (admin only) =====

    @Transactional
    public AsignaturaDTO addEstudiante(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona p = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        if (p.getRole() != Usuarios.Role.ROLE_ESTUDIANTE) {
            throw new RuntimeException("El usuario no tiene rol de estudiante");
        }
        a.getEstudiantes().add(p);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO removeEstudiante(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        a.getEstudiantes().removeIf(p -> p.getId().equals(personaId));
        return toDTO(asignaturaRepository.save(a));
    }

    // ===== Self-enrollment for students =====

    @Transactional
    public AsignaturaDTO inscribirse(Long asignaturaId, String username) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona p = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        a.getEstudiantes().add(p);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO desinscribirse(Long asignaturaId, String username) {
        Asignatura a = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        a.getEstudiantes().removeIf(p -> p.getUsername().equals(username));
        return toDTO(asignaturaRepository.save(a));
    }

    // ===== CSV Import =====

    @Transactional
    public List<AsignaturaDTO> importFromCsv(InputStream inputStream, String adminUsername) {
        Persona admin = personaRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + adminUsername));

        List<AsignaturaDTO> imported = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // skip header
                String[] parts = line.split(";", -1);
                if (parts.length < 1 || parts[0].trim().isEmpty()) continue;

                Asignatura a = new Asignatura();
                a.setNombre(parts[0].trim());
                a.setDescripcion(parts.length > 1 ? parts[1].trim() : "");
                a.setUrl(parts.length > 2 ? parts[2].trim() : "");
                a.setCreador(admin);
                imported.add(toDTO(asignaturaRepository.save(a)));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el CSV: " + e.getMessage());
        }
        return imported;
    }

    private AsignaturaDTO toDTO(Asignatura a) {
        Set<Long> profesorIds = a.getProfesores() != null
                ? a.getProfesores().stream().map(Persona::getId).collect(Collectors.toSet())
                : new HashSet<>();
        Set<Long> estudianteIds = a.getEstudiantes() != null
                ? a.getEstudiantes().stream().map(Persona::getId).collect(Collectors.toSet())
                : new HashSet<>();
        AsignaturaDTO dto = new AsignaturaDTO();
        dto.setId(a.getId());
        dto.setNombre(a.getNombre());
        dto.setDescripcion(a.getDescripcion());
        dto.setUrl(a.getUrl());
        dto.setCreadorId(a.getCreador() != null ? a.getCreador().getId() : null);
        dto.setCreadorNombre(a.getCreador() != null ? a.getCreador().getNombreCompleto() : null);
        dto.setProfesorIds(profesorIds);
        dto.setEstudianteIds(estudianteIds);
        return dto;
    }
}
