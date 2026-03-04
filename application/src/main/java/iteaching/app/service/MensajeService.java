package iteaching.app.service;

import iteaching.app.Models.Mensaje;
import iteaching.app.Models.Persona;
import iteaching.app.dto.MensajeDTO;
import iteaching.app.repository.MensajeRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.AsignaturaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final PersonaRepository personaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final NotificacionService notificacionService;

    public MensajeService(MensajeRepository mensajeRepository,
                          PersonaRepository personaRepository,
                          AsignaturaRepository asignaturaRepository,
                          NotificacionService notificacionService) {
        this.mensajeRepository = mensajeRepository;
        this.personaRepository = personaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.notificacionService = notificacionService;
    }

    public List<MensajeDTO> getMensajesUsuario(Long userId) {
        return mensajeRepository.findByUsuario(userId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<MensajeDTO> getConversacion(Long userId1, Long userId2) {
        return mensajeRepository.findConversacion(userId1, userId2).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public long countNoLeidos(Long userId) {
        return mensajeRepository.countNoLeidos(userId);
    }

    public MensajeDTO enviarMensaje(MensajeDTO dto, String username) {
        Persona remitente = personaRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Remitente no encontrado"));
        Persona destinatario = personaRepository.findById(dto.getDestinatarioId())
            .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setContenido(dto.getContenido());
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);

        if (dto.getAsignaturaId() != null) {
            mensaje.setAsignatura(asignaturaRepository.findById(dto.getAsignaturaId()).orElse(null));
        }

        Mensaje saved = mensajeRepository.save(mensaje);

        // Create notification for recipient
        notificacionService.crearNotificacion(
            destinatario,
            "Nuevo mensaje de " + remitente.getNombreCompleto(),
            dto.getContenido().length() > 100 ? dto.getContenido().substring(0, 100) + "..." : dto.getContenido(),
            "MENSAJE",
            "/mensajes"
        );

        return toDTO(saved);
    }

    public void marcarLeidos(Long userId1, Long userId2) {
        List<Mensaje> mensajes = mensajeRepository.findConversacion(userId1, userId2);
        for (Mensaje m : mensajes) {
            if (m.getDestinatario().getId().equals(userId1) && !m.getLeido()) {
                m.setLeido(true);
                mensajeRepository.save(m);
            }
        }
    }

    private MensajeDTO toDTO(Mensaje m) {
        MensajeDTO dto = new MensajeDTO();
        dto.setId(m.getId());
        dto.setContenido(m.getContenido());
        dto.setFechaEnvio(m.getFechaEnvio().toString());
        dto.setLeido(m.getLeido());
        dto.setRemitenteId(m.getRemitente().getId());
        dto.setRemitenteNombre(m.getRemitente().getNombreCompleto());
        dto.setDestinatarioId(m.getDestinatario().getId());
        dto.setDestinatarioNombre(m.getDestinatario().getNombreCompleto());
        if (m.getAsignatura() != null) {
            dto.setAsignaturaId(m.getAsignatura().getId());
            dto.setAsignaturaNombre(m.getAsignatura().getNombre());
        }
        return dto;
    }
}
