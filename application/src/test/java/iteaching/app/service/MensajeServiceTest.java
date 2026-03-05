package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Mensaje;
import iteaching.app.Models.Persona;
import iteaching.app.dto.MensajeDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.MensajeRepository;
import iteaching.app.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MensajeService — unit tests")
class MensajeServiceTest {

    @Mock private MensajeRepository mensajeRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private MensajeService service;

    private Persona remitente;
    private Persona destinatario;
    private Mensaje mensaje;

    @BeforeEach
    void setUp() {
        remitente = new Persona();
        remitente.setId(1L);
        remitente.setUsername("user1");
        remitente.setNombre("Juan");
        remitente.setApellido("García");

        destinatario = new Persona();
        destinatario.setId(2L);
        destinatario.setUsername("user2");
        destinatario.setNombre("María");
        destinatario.setApellido("López");

        mensaje = new Mensaje();
        mensaje.setId(100L);
        mensaje.setContenido("Hola");
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
    }

    @Test
    void getMensajesUsuario_returnsList() {
        when(mensajeRepository.findByUsuario(1L)).thenReturn(List.of(mensaje));
        List<MensajeDTO> result = service.getMensajesUsuario(1L);
        assertEquals(1, result.size());
        assertEquals("Hola", result.get(0).getContenido());
    }

    @Test
    void getConversacion_returnsList() {
        when(mensajeRepository.findConversacion(1L, 2L)).thenReturn(List.of(mensaje));
        List<MensajeDTO> result = service.getConversacion(1L, 2L);
        assertEquals(1, result.size());
    }

    @Test
    void countNoLeidos_returnsCount() {
        when(mensajeRepository.countNoLeidos(2L)).thenReturn(5L);
        assertEquals(5L, service.countNoLeidos(2L));
    }

    @Test
    void enviarMensaje_success_withoutAsignatura() {
        MensajeDTO dto = new MensajeDTO();
        dto.setContenido("Hola mundo");
        dto.setDestinatarioId(2L);

        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(remitente));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
            Mensaje m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });

        MensajeDTO result = service.enviarMensaje(dto, "user1");
        assertNotNull(result);
        assertEquals(1L, result.getRemitenteId());
        assertEquals(2L, result.getDestinatarioId());
        verify(notificacionService).crearNotificacion(eq(destinatario), anyString(), anyString(), eq("MENSAJE"), eq("/mensajes"));
    }

    @Test
    void enviarMensaje_success_withAsignatura() {
        Asignatura asig = new Asignatura();
        asig.setId(5L);
        asig.setNombre("Mates");

        MensajeDTO dto = new MensajeDTO();
        dto.setContenido("Pregunta sobre el tema 3");
        dto.setDestinatarioId(2L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(remitente));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asig));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
            Mensaje m = inv.getArgument(0);
            m.setId(101L);
            return m;
        });

        MensajeDTO result = service.enviarMensaje(dto, "user1");
        assertEquals(5L, result.getAsignaturaId());
    }

    @Test
    void enviarMensaje_remitenteNotFound_throws() {
        MensajeDTO dto = new MensajeDTO();
        dto.setContenido("Hola");
        dto.setDestinatarioId(2L);
        when(personaRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.enviarMensaje(dto, "ghost"));
    }

    @Test
    void enviarMensaje_destinatarioNotFound_throws() {
        MensajeDTO dto = new MensajeDTO();
        dto.setContenido("Hola");
        dto.setDestinatarioId(999L);
        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(remitente));
        when(personaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.enviarMensaje(dto, "user1"));
    }

    @Test
    void marcarLeidos_marksOnlyDestinarioMessages() {
        Mensaje m1 = new Mensaje();
        m1.setId(1L);
        m1.setContenido("Msg1");
        m1.setFechaEnvio(LocalDateTime.now());
        m1.setLeido(false);
        m1.setRemitente(remitente);
        m1.setDestinatario(destinatario);

        Mensaje m2 = new Mensaje();
        m2.setId(2L);
        m2.setContenido("Msg2");
        m2.setFechaEnvio(LocalDateTime.now());
        m2.setLeido(false);
        m2.setRemitente(destinatario);
        m2.setDestinatario(remitente);

        when(mensajeRepository.findConversacion(2L, 1L)).thenReturn(List.of(m1, m2));

        service.marcarLeidos(2L, 1L);

        // m1: destinatario=2L → should be marked if userId1=2L
        verify(mensajeRepository, atLeastOnce()).save(argThat(m -> m.getLeido()));
    }
}
