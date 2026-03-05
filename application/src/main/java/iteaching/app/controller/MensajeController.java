package iteaching.app.controller;

import iteaching.app.config.WebSocketEventListener;
import iteaching.app.dto.MensajeDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.service.MensajeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;
    private final PersonaRepository personaRepository;
    private final WebSocketEventListener webSocketEventListener;

    public MensajeController(MensajeService mensajeService,
                             PersonaRepository personaRepository,
                             WebSocketEventListener webSocketEventListener) {
        this.mensajeService = mensajeService;
        this.personaRepository = personaRepository;
        this.webSocketEventListener = webSocketEventListener;
    }

    @GetMapping
    public ResponseEntity<List<MensajeDTO>> getMisMensajes(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(mensajeService.getMensajesUsuario(persona.getId()));
    }

    @GetMapping("/conversacion/{userId}")
    public ResponseEntity<List<MensajeDTO>> getConversacion(
            @PathVariable Long userId, Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(mensajeService.getConversacion(persona.getId(), userId));
    }

    @GetMapping("/no-leidos/count")
    public ResponseEntity<Map<String, Long>> countNoLeidos(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(Map.of("count", mensajeService.countNoLeidos(persona.getId())));
    }

    @PostMapping
    public ResponseEntity<MensajeDTO> enviar(@Valid @RequestBody MensajeDTO dto, Authentication auth) {
        return ResponseEntity.ok(mensajeService.enviarMensaje(dto, auth.getName()));
    }

    @PatchMapping("/conversacion/{userId}/leer")
    public ResponseEntity<Void> marcarLeidos(@PathVariable Long userId, Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        mensajeService.marcarLeidos(persona.getId(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/online")
    public ResponseEntity<Set<Long>> getOnlineUsers() {
        return ResponseEntity.ok(webSocketEventListener.getOnlineUsers());
    }
}
