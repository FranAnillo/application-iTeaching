package iteaching.app.service;

import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.CsvImportResult;
import iteaching.app.dto.UsuarioDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.UsuarioRepository;
import iteaching.app.repository.GradoRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonaService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradoRepository gradoRepository;

    public PersonaService(PersonaRepository personaRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          GradoRepository gradoRepository) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.gradoRepository = gradoRepository;
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

    /**
     * Importa usuarios desde un CSV.
     * Formato: username;password;nombre;apellido;email;telefono;rol
     * El rol es opcional y por defecto ROLE_ESTUDIANTE.
     * Valores válidos: ESTUDIANTE, PROFESOR, ADMIN (con o sin prefijo ROLE_).
     */
    // importFromCsv handles each line in its own transactional context to
    // prevent a single failure from marking the entire operation rollback-only.
    public CsvImportResult importFromCsv(InputStream inputStream) {
        List<UsuarioDTO> imported = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNum = 0;
        final int MAX_LINES = 500;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                if (lineNum > MAX_LINES) {
                    errors.add("Se alcanzó el límite máximo de " + MAX_LINES + " líneas. Se ignoraron las restantes.");
                    break;
                }

                String[] parts = line.split(";", -1);
                if (parts.length < 5) {
                    errors.add("Linea " + lineNum + ": formato invalido (se requieren al menos 5 campos: username;password;nombre;apellido;email)");
                    continue;
                }

                String username = InputSanitizer.sanitize(parts[0].trim());
                String password = parts[1].trim(); // password will be hashed, not stored raw
                String nombre = InputSanitizer.sanitize(parts[2].trim());
                String apellido = InputSanitizer.sanitize(parts[3].trim());
                String email = InputSanitizer.sanitize(parts[4].trim());
                String telefono = parts.length > 5 ? InputSanitizer.sanitize(parts[5].trim()) : "";
                String rolStr = parts.length > 6 ? parts[6].trim().toUpperCase() : "";
                String gradoNombre = parts.length > 7 ? parts[7].trim() : "";

                // Validación mejorada: campos obligatorios no vacíos y email válido
                if (username.isEmpty() || password.isEmpty() || nombre.isEmpty()
                        || apellido.isEmpty() || email.isEmpty()) {
                    errors.add("Linea " + lineNum + ": campos obligatorios vacíos (" + username + ")");
                    continue;
                }

                // Validar que nombre y apellido no sean solo espacios
                if (nombre.trim().isEmpty() || apellido.trim().isEmpty()) {
                    errors.add("Linea " + lineNum + ": nombre/apellido inválido (solo espacios) (" + username + ")");
                    continue;
                }

                // Validar formato de email básico
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    errors.add("Linea " + lineNum + ": email inválido ('" + email + "')");
                    continue;
                }

                // Validar teléfono opcional: hasta 15 dígitos numéricos
                if (!telefono.isEmpty() && !telefono.matches("^\\d{1,15}$")) {
                    errors.add("Linea " + lineNum + ": telefono inválido ('" + telefono + "')");
                    continue;
                }

                if (usuarioRepository.existsByUsername(username)) {
                    errors.add("Linea " + lineNum + ": el usuario '" + username + "' ya existe");
                    continue;
                }

                if (personaRepository.existsByEmail(email)) {
                    errors.add("Linea " + lineNum + ": el email '" + email + "' ya esta en uso");
                    continue;
                }

                Usuarios.Role role = Usuarios.Role.ROLE_ESTUDIANTE;
                if (!rolStr.isEmpty()) {
                    try {
                        if (!rolStr.startsWith("ROLE_")) rolStr = "ROLE_" + rolStr;
                        role = Usuarios.Role.valueOf(rolStr);
                    } catch (IllegalArgumentException e) {
                        errors.add("Linea " + lineNum + ": rol invalido '" + parts[6].trim() + "' (usar: ESTUDIANTE, PROFESOR o ADMIN)");
                        continue;
                    }
                }

                if (!gradoNombre.isEmpty()) {
                    var opt = gradoRepository.findByNombreIgnoreCase(gradoNombre);
                    if (opt.isEmpty()) {
                        errors.add("Linea " + lineNum + ": grado desconocido '" + gradoNombre + "'");
                        continue;
                    }
                }

                Persona persona = new Persona();
                if (!gradoNombre.isEmpty()) {
                    // we already know it exists
                    persona.setGrado(gradoRepository.findByNombreIgnoreCase(gradoNombre).orElse(null));
                }
                persona.setUsername(username);
                persona.setPassword(passwordEncoder.encode(password));
                persona.setNombre(nombre);
                persona.setApellido(apellido);
                persona.setEmail(email);
                persona.setTelefono(telefono);
                persona.setEnabled(true);
                persona.setRole(role);

                try {
                    Persona saved = savePersonaTransactional(persona);
                    imported.add(toDTO(saved));
                } catch (Exception e) {
                    // clear persistence context after failure to avoid flushing invalid state
                    if (entityManager != null) {
                        entityManager.clear();
                    }
                    errors.add("Linea " + lineNum + ": error al guardar '" + username + "' - " + e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el CSV");
        }

        return new CsvImportResult(imported, errors);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Persona savePersonaTransactional(Persona persona) {
        return personaRepository.save(persona);
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
        if (p.getGrado() != null) {
            dto.setGradoId(p.getGrado().getId());
            dto.setGradoNombre(p.getGrado().getNombre());
        }
        return dto;
    }
}
