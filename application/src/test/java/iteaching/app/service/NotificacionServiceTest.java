package iteaching.app.service;

import iteaching.app.Models.Notificacion;
import iteaching.app.Models.Persona;
import iteaching.app.dto.NotificacionDTO;
import iteaching.app.repository.NotificacionRepository;
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
@DisplayName("NotificacionService — unit tests")
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;

    @InjectMocks private NotificacionService service;

    private Persona usuario;
    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        usuario = new Persona();
        usuario.setId(1L);
        usuario.setUsername("user1");
        usuario.setNombre("Juan");
        usuario.setApellido("García");

        notificacion = new Notificacion();
        notificacion.setId(10L);
        notificacion.setTitulo("Nueva tarea");
        notificacion.setMensaje("Se ha publicado la tarea 1");
        notificacion.setTipo(Notificacion.TipoNotificacion.TAREA);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        notificacion.setEnlace("/tareas");
        notificacion.setUsuario(usuario);
    }

    @Test
    void getNotificaciones_returnsList() {
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notificacion));
        List<NotificacionDTO> result = service.getNotificaciones(1L);
        assertEquals(1, result.size());
        assertEquals("Nueva tarea", result.get(0).getTitulo());
    }

    @Test
    void getNoLeidas_returnsList() {
        when(notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notificacion));
        List<NotificacionDTO> result = service.getNoLeidas(1L);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getLeida());
    }

    @Test
    void countNoLeidas_returnsCount() {
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(3L);
        assertEquals(3L, service.countNoLeidas(1L));
    }

    @Test
    void marcarLeida_success_ownerMatch() {
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(notificacion));
        assertDoesNotThrow(() -> service.marcarLeida(10L, 1L));
        assertTrue(notificacion.getLeida());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    void marcarLeida_notFound_throws() {
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.marcarLeida(999L, 1L));
    }

    @Test
    void marcarLeida_differentUser_throws() {
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(notificacion));
        // notificacion belongs to userId 1, attempting with userId 99
        assertThrows(RuntimeException.class, () -> service.marcarLeida(10L, 99L));
    }

    @Test
    void marcarTodasLeidas_marksAll() {
        Notificacion n2 = new Notificacion();
        n2.setId(11L);
        n2.setTitulo("Otra");
        n2.setMensaje("Msg");
        n2.setTipo(Notificacion.TipoNotificacion.INFO);
        n2.setFechaCreacion(LocalDateTime.now());
        n2.setLeida(false);
        n2.setUsuario(usuario);

        when(notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notificacion, n2));

        service.marcarTodasLeidas(1L);

        assertTrue(notificacion.getLeida());
        assertTrue(n2.getLeida());
        verify(notificacionRepository).saveAll(List.of(notificacion, n2));
    }

    @Test
    void crearNotificacion_savesCorrectly() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(100L);
            return n;
        });

        service.crearNotificacion(usuario, "Título", "Mensaje", "LOGRO", "/logros");

        verify(notificacionRepository).save(argThat(n ->
            n.getTitulo().equals("Título") &&
            n.getMensaje().equals("Mensaje") &&
            n.getTipo() == Notificacion.TipoNotificacion.LOGRO &&
            n.getEnlace().equals("/logros") &&
            !n.getLeida() &&
            n.getUsuario().equals(usuario)
        ));
    }
}
