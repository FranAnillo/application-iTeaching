package iteaching.app.controller;

import iteaching.app.dto.MensajeDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.service.MensajeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final MensajeService mensajeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PersonaRepository personaRepository;

    public ChatWebSocketController(MensajeService mensajeService,
                                   SimpMessagingTemplate messagingTemplate,
                                   PersonaRepository personaRepository) {
        this.mensajeService = mensajeService;
        this.messagingTemplate = messagingTemplate;
        this.personaRepository = personaRepository;
    }

    /**
     * Recibe un mensaje del cliente via STOMP y lo persiste,
     * luego lo reenvía al destinatario en tiempo real.
     * Cliente envía a: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MensajeDTO dto, Principal principal) {
        if (principal == null) return;

        // Persistir el mensaje usando el servicio existente
        MensajeDTO saved = mensajeService.enviarMensaje(dto, principal.getName());

        // Buscar username del destinatario para el routing
        String destinatarioUsername = personaRepository.findById(saved.getDestinatarioId())
            .map(p -> p.getUsername())
            .orElse(null);

        if (destinatarioUsername != null) {
            // Enviar al destinatario en tiempo real
            messagingTemplate.convertAndSendToUser(
                destinatarioUsername,
                "/queue/chat",
                saved
            );
        }

        // Enviar confirmación al remitente
        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/queue/chat",
            saved
        );
    }

    /**
     * Indicador de "escribiendo..."
     * Cliente envía a: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingIndicator indicator, Principal principal) {
        if (principal == null) return;

        String destinatarioUsername = personaRepository.findById(indicator.getDestinatarioId())
            .map(p -> p.getUsername())
            .orElse(null);

        if (destinatarioUsername != null) {
            messagingTemplate.convertAndSendToUser(
                destinatarioUsername,
                "/queue/typing",
                indicator
            );
        }
    }

    /**
     * Marcar mensajes como leídos (doble check azul)
     * Cliente envía a: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markRead(@Payload ReadReceipt receipt, Principal principal) {
        if (principal == null) return;

        mensajeService.marcarLeidos(receipt.getLectorId(), receipt.getConversacionUserId());

        String otherUsername = personaRepository.findById(receipt.getConversacionUserId())
            .map(p -> p.getUsername())
            .orElse(null);

        if (otherUsername != null) {
            messagingTemplate.convertAndSendToUser(
                otherUsername,
                "/queue/read",
                receipt
            );
        }
    }

    // ---------- Inner DTOs ----------

    public static class TypingIndicator {
        private Long remitenteId;
        private String remitenteNombre;
        private Long destinatarioId;

        public Long getRemitenteId() { return remitenteId; }
        public void setRemitenteId(Long remitenteId) { this.remitenteId = remitenteId; }
        public String getRemitenteNombre() { return remitenteNombre; }
        public void setRemitenteNombre(String remitenteNombre) { this.remitenteNombre = remitenteNombre; }
        public Long getDestinatarioId() { return destinatarioId; }
        public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
    }

    public static class ReadReceipt {
        private Long lectorId;
        private Long conversacionUserId;

        public Long getLectorId() { return lectorId; }
        public void setLectorId(Long lectorId) { this.lectorId = lectorId; }
        public Long getConversacionUserId() { return conversacionUserId; }
        public void setConversacionUserId(Long conversacionUserId) { this.conversacionUserId = conversacionUserId; }
    }
}
