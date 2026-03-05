package iteaching.app.controller;

import iteaching.app.dto.NotificacionDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final PersonaRepository personaRepository;

    public NotificacionController(NotificacionService notificacionService, PersonaRepository personaRepository) {
        this.notificacionService = notificacionService;
        this.personaRepository = personaRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> getAll(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(notificacionService.getNotificaciones(persona.getId()));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionDTO>> getNoLeidas(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(notificacionService.getNoLeidas(persona.getId()));
    }

    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> countNoLeidas(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(Map.of("count", notificacionService.countNoLeidas(persona.getId())));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        notificacionService.marcarLeida(id, persona.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        notificacionService.marcarTodasLeidas(persona.getId());
        return ResponseEntity.ok().build();
    }
}
