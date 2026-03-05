package iteaching.app.service;

import iteaching.app.Models.Logro;
import iteaching.app.Models.Persona;
import iteaching.app.dto.LogroDTO;
import iteaching.app.repository.EntregaRepository;
import iteaching.app.repository.LogroRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.security.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LogroService {

    private static final Logger log = LoggerFactory.getLogger(LogroService.class);

    private final LogroRepository logroRepository;
    private final PersonaRepository personaRepository;
    private final NotificacionService notificacionService;

    public LogroService(LogroRepository logroRepository,
                        PersonaRepository personaRepository,
                        NotificacionService notificacionService) {
        this.logroRepository = logroRepository;
        this.personaRepository = personaRepository;
        this.notificacionService = notificacionService;
    }

    public List<LogroDTO> getAllLogros(Long userId) {
        Persona persona = personaRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Set<Long> obtenidosIds = persona.getLogros().stream()
            .map(Logro::getId).collect(Collectors.toSet());

        return logroRepository.findAll().stream()
            .map(logro -> {
                LogroDTO dto = toDTO(logro);
                dto.setObtenido(obtenidosIds.contains(logro.getId()));
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<LogroDTO> getLogrosObtenidos(Long userId) {
        Persona persona = personaRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return persona.getLogros().stream()
            .map(logro -> {
                LogroDTO dto = toDTO(logro);
                dto.setObtenido(true);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public void otorgarLogro(Long userId, String codigoLogro) {
        Persona persona = personaRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Logro logro = logroRepository.findByCodigo(codigoLogro)
            .orElseThrow(() -> new RuntimeException("Logro no encontrado: " + codigoLogro));

        if (!persona.getLogros().contains(logro)) {
            persona.getLogros().add(logro);
            personaRepository.save(persona);

            notificacionService.crearNotificacion(
                persona,
                "¡Logro desbloqueado! " + logro.getIcono(),
                logro.getNombre() + ": " + logro.getDescripcion(),
                "LOGRO",
                "/logros"
            );
            log.info("Logro '{}' otorgado a {}", codigoLogro, persona.getUsername());
        }
    }

    @Transactional
    public void verificarLogros(Long userId) {
        Persona persona = personaRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check for first submission
        long totalEntregas = persona.getAsignaturasInscritas().stream()
            .flatMap(a -> a.getGrupos().stream())
            .count(); // simplified - just check if they have any activity

        // Simple achievement checks
        if (!tieneLogro(persona, "PRIMERA_ENTREGA")) {
            // This will be triggered from EntregaService when a submission is made
        }
    }

    private boolean tieneLogro(Persona persona, String codigo) {
        return persona.getLogros().stream()
            .anyMatch(l -> l.getCodigo().equals(codigo));
    }

    public LogroDTO crearLogro(LogroDTO dto) {
        Logro logro = new Logro();
        logro.setCodigo(InputSanitizer.sanitize(dto.getCodigo()));
        logro.setNombre(InputSanitizer.sanitize(dto.getNombre()));
        logro.setDescripcion(InputSanitizer.sanitize(dto.getDescripcion()));
        logro.setIcono(InputSanitizer.sanitize(dto.getIcono()));
        logro.setCategoria(Logro.CategoriaLogro.valueOf(dto.getCategoria()));
        logro.setValorObjetivo(dto.getValorObjetivo());
        return toDTO(logroRepository.save(logro));
    }

    private LogroDTO toDTO(Logro l) {
        LogroDTO dto = new LogroDTO();
        dto.setId(l.getId());
        dto.setCodigo(l.getCodigo());
        dto.setNombre(l.getNombre());
        dto.setDescripcion(l.getDescripcion());
        dto.setIcono(l.getIcono());
        dto.setCategoria(l.getCategoria().name());
        dto.setValorObjetivo(l.getValorObjetivo());
        return dto;
    }
}
