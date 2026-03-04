package iteaching.app.config;

import iteaching.app.Models.Persona;
import iteaching.app.repository.PersonaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rastrea conexiones WebSocket para indicar estado "en línea" de los usuarios.
 * Mantiene un mapa sessionId -> userId y un set de userIds conectados.
 */
@Component
public class WebSocketEventListener {

    // sessionId -> userId
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    // userId set de usuarios online
    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

    private final SimpMessagingTemplate messagingTemplate;
    private final PersonaRepository personaRepository;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate,
                                  PersonaRepository personaRepository) {
        this.messagingTemplate = messagingTemplate;
        this.personaRepository = personaRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        var user = accessor.getUser();
        if (user instanceof UsernamePasswordAuthenticationToken authToken) {
            String username = authToken.getName();
            Optional<Persona> persona = personaRepository.findByUsername(username);
            persona.ifPresent(p -> {
                String sessionId = accessor.getSessionId();
                if (sessionId != null) {
                    sessionUserMap.put(sessionId, p.getId());
                }
                onlineUsers.add(p.getId());
                // Broadcast online status
                messagingTemplate.convertAndSend("/topic/online", Map.of(
                    "userId", p.getId(),
                    "online", true
                ));
            });
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            Long userId = sessionUserMap.remove(sessionId);
            if (userId != null) {
                // Check if user has other active sessions
                boolean stillOnline = sessionUserMap.containsValue(userId);
                if (!stillOnline) {
                    onlineUsers.remove(userId);
                    messagingTemplate.convertAndSend("/topic/online", Map.of(
                        "userId", userId,
                        "online", false
                    ));
                }
            }
        }
    }

    public Set<Long> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }
}
