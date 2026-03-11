package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Grado;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AsignaturaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.GradoRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.security.InputSanitizer;
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
    private final GradoRepository gradoRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository, PersonaRepository personaRepository, GradoRepository gradoRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
        this.gradoRepository = gradoRepository;
    }

    public List<AsignaturaDTO> findAll() {
        return asignaturaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AsignaturaDTO findById(Long id) {
        Asignatura a = asignaturaRepository.findById(id != null ? id : 0L)
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

        Grado grado = gradoRepository.findById(dto.getGradoId() != null ? dto.getGradoId() : 0L)
                .orElseThrow(() -> new RuntimeException("Grado no encontrado con id: " + dto.getGradoId()));

        Asignatura a = new Asignatura();
        a.setNombre(InputSanitizer.sanitize(dto.getNombre()));
        a.setSiglas(InputSanitizer.sanitize(dto.getSiglas()));
        a.setDescripcion(InputSanitizer.sanitize(dto.getDescripcion()));
        a.setUrl(InputSanitizer.sanitizeUrl(dto.getUrl()));
        a.setCreador(creador);
        a.setGrado(grado);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO update(Long id, AsignaturaDTO dto) {
        Asignatura a = asignaturaRepository.findById(id != null ? id : 0L)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
        a.setNombre(InputSanitizer.sanitize(dto.getNombre()));
        a.setSiglas(InputSanitizer.sanitize(dto.getSiglas()));
        a.setDescripcion(InputSanitizer.sanitize(dto.getDescripcion()));
        a.setUrl(InputSanitizer.sanitizeUrl(dto.getUrl()));
        
        if (dto.getGradoId() != null) {
            Grado grado = gradoRepository.findById(dto.getGradoId())
                    .orElseThrow(() -> new RuntimeException("Grado no encontrado con id: " + dto.getGradoId()));
            a.setGrado(grado);
        }
        
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        if (!asignaturaRepository.existsById(id != null ? id : 0L))
            throw new RuntimeException("Asignatura no encontrada con id: " + id);
        asignaturaRepository.deleteById(id != null ? id : 0L);
    }

    // ===== Profesor assignment (admin only) =====

    @Transactional
    public AsignaturaDTO addProfesor(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId != null ? asignaturaId : 0L)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona p = personaRepository.findById(personaId != null ? personaId : 0L)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        if (p.getRole() != Usuarios.Role.ROLE_PROFESOR) {
            throw new RuntimeException("El usuario no tiene rol de profesor");
        }
        a.getProfesores().add(p);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO removeProfesor(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId != null ? asignaturaId : 0L)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        a.getProfesores().removeIf(p -> p.getId().equals(personaId));
        return toDTO(asignaturaRepository.save(a));
    }

    // ===== Estudiante assignment (admin only) =====

    @Transactional
    public AsignaturaDTO addEstudiante(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId != null ? asignaturaId : 0L)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona p = personaRepository.findById(personaId != null ? personaId : 0L)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        if (p.getRole() != Usuarios.Role.ROLE_ESTUDIANTE) {
            throw new RuntimeException("El usuario no tiene rol de estudiante");
        }
        a.getEstudiantes().add(p);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO removeEstudiante(Long asignaturaId, Long personaId) {
        Asignatura a = asignaturaRepository.findById(asignaturaId != null ? asignaturaId : 0L)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        a.getEstudiantes().removeIf(p -> p.getId().equals(personaId));
        return toDTO(asignaturaRepository.save(a));
    }

    // ===== Self-enrollment for students =====

    @Transactional
    public AsignaturaDTO inscribirse(Long asignaturaId, String username) {
        Asignatura a = asignaturaRepository.findById(asignaturaId != null ? asignaturaId : 0L)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona p = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        a.getEstudiantes().add(p);
        return toDTO(asignaturaRepository.save(a));
    }

    @Transactional
    public AsignaturaDTO desinscribirse(Long asignaturaId, String username) {
        Asignatura a = asignaturaRepository.findById(asignaturaId != null ? asignaturaId : 0L)
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
        final int MAX_LINES = 500;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            boolean header = true;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // skip header
                lineCount++;
                if (lineCount > MAX_LINES) {
                    throw new RuntimeException("El CSV excede el límite máximo de " + MAX_LINES + " líneas");
                }
                String[] parts = line.split(";", -1);
                if (parts.length < 1 || parts[0].trim().isEmpty()) continue;

                Asignatura a = new Asignatura();
                a.setNombre(InputSanitizer.sanitize(parts[0].trim()));
                a.setSiglas(parts.length > 3 ? InputSanitizer.sanitize(parts[3].trim()) : "");
                a.setDescripcion(parts.length > 1 ? InputSanitizer.sanitize(parts[1].trim()) : "");
                a.setUrl(parts.length > 2 ? InputSanitizer.sanitizeUrl(parts[2].trim()) : "");
                a.setCreador(admin);
                
                // For CSV import, we use the first available grado as default if not specified
                // Ideally, CSV should have a grado_id column
                Grado defaultGrado = gradoRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No hay grados disponibles para asignar a la asignatura"));
                a.setGrado(defaultGrado);
                
                imported.add(toDTO(asignaturaRepository.save(a)));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el CSV");
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
        dto.setSiglas(a.getSiglas());
        dto.setDescripcion(a.getDescripcion());
        dto.setUrl(a.getUrl());
        dto.setCreadorId(a.getCreador() != null ? a.getCreador().getId() : null);
        dto.setCreadorNombre(a.getCreador() != null ? a.getCreador().getNombreCompleto() : null);
        dto.setProfesorIds(profesorIds);
        dto.setEstudianteIds(estudianteIds);
        dto.setGradoId(a.getGrado() != null ? a.getGrado().getId() : null);
        dto.setGradoNombre(a.getGrado() != null ? a.getGrado().getNombre() : null);
        return dto;
    }
}
