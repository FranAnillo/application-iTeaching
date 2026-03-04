package iteaching.app.service;

import iteaching.app.Models.Notificacion;
import iteaching.app.Models.Persona;
import iteaching.app.dto.NotificacionDTO;
import iteaching.app.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public List<NotificacionDTO> getNotificaciones(Long userId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(userId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<NotificacionDTO> getNoLeidas(Long userId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(userId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public long countNoLeidas(Long userId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(userId);
    }

    public void marcarLeida(Long notificacionId) {
        Notificacion n = notificacionRepository.findById(notificacionId)
            .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        n.setLeida(true);
        notificacionRepository.save(n);
    }

    public void marcarTodasLeidas(Long userId) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(userId);
        for (Notificacion n : noLeidas) {
            n.setLeida(true);
        }
        notificacionRepository.saveAll(noLeidas);
    }

    public void crearNotificacion(Persona usuario, String titulo, String mensaje, String tipo, String enlace) {
        Notificacion n = new Notificacion();
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setTipo(Notificacion.TipoNotificacion.valueOf(tipo));
        n.setFechaCreacion(LocalDateTime.now());
        n.setLeida(false);
        n.setEnlace(enlace);
        n.setUsuario(usuario);
        notificacionRepository.save(n);
    }

    private NotificacionDTO toDTO(Notificacion n) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(n.getId());
        dto.setTitulo(n.getTitulo());
        dto.setMensaje(n.getMensaje());
        dto.setTipo(n.getTipo().name());
        dto.setFechaCreacion(n.getFechaCreacion().toString());
        dto.setLeida(n.getLeida());
        dto.setEnlace(n.getEnlace());
        dto.setUsuarioId(n.getUsuario().getId());
        return dto;
    }
}
