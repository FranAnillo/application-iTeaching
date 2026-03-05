package iteaching.app.service;

import iteaching.app.Models.Logro;
import iteaching.app.Models.Persona;
import iteaching.app.dto.LogroDTO;
import iteaching.app.repository.LogroRepository;
import iteaching.app.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogroService — unit tests")
class LogroServiceTest {

    @Mock private LogroRepository logroRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private LogroService service;

    private Persona persona;
    private Logro logro;

    @BeforeEach
    void setUp() {
        logro = new Logro();
        logro.setId(1L);
        logro.setCodigo("PRIMERA_ENTREGA");
        logro.setNombre("Primera entrega");
        logro.setDescripcion("Has enviado tu primera entrega");
        logro.setIcono("📝");
        logro.setCategoria(Logro.CategoriaLogro.ACADEMICO);
        logro.setValorObjetivo(1);

        persona = new Persona();
        persona.setId(10L);
        persona.setUsername("alumno1");
        persona.setNombre("Est");
        persona.setApellido("Y");
        persona.setLogros(new HashSet<>());
        persona.setAsignaturasInscritas(new HashSet<>());
    }

    @Test
    void getAllLogros_marksObtained() {
        persona.getLogros().add(logro);
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(logroRepository.findAll()).thenReturn(List.of(logro));

        List<LogroDTO> result = service.getAllLogros(10L);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isObtenido());
    }

    @Test
    void getAllLogros_marksNotObtained() {
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(logroRepository.findAll()).thenReturn(List.of(logro));

        List<LogroDTO> result = service.getAllLogros(10L);
        assertFalse(result.get(0).isObtenido());
    }

    @Test
    void getAllLogros_userNotFound_throws() {
        when(personaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getAllLogros(999L));
    }

    @Test
    void getLogrosObtenidos_returnsOnlyObtained() {
        persona.getLogros().add(logro);
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));

        List<LogroDTO> result = service.getLogrosObtenidos(10L);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isObtenido());
    }

    @Test
    void getLogrosObtenidos_emptyWhenNone() {
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        List<LogroDTO> result = service.getLogrosObtenidos(10L);
        assertTrue(result.isEmpty());
    }

    @Test
    void otorgarLogro_grantsNew() {
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(logroRepository.findByCodigo("PRIMERA_ENTREGA")).thenReturn(Optional.of(logro));

        service.otorgarLogro(10L, "PRIMERA_ENTREGA");

        assertTrue(persona.getLogros().contains(logro));
        verify(personaRepository).save(persona);
        verify(notificacionService).crearNotificacion(eq(persona), anyString(), anyString(), eq("LOGRO"), eq("/logros"));
    }

    @Test
    void otorgarLogro_doesNotDuplicate() {
        persona.getLogros().add(logro);
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(logroRepository.findByCodigo("PRIMERA_ENTREGA")).thenReturn(Optional.of(logro));

        service.otorgarLogro(10L, "PRIMERA_ENTREGA");

        verify(personaRepository, never()).save(any());
        verify(notificacionService, never()).crearNotificacion(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void otorgarLogro_logroNotFound_throws() {
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(logroRepository.findByCodigo("INEXISTENTE")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.otorgarLogro(10L, "INEXISTENTE"));
    }

    @Test
    void crearLogro_success() {
        LogroDTO dto = new LogroDTO();
        dto.setCodigo("NUEVO_LOGRO");
        dto.setNombre("Nuevo Logro");
        dto.setDescripcion("Descripción");
        dto.setIcono("🏅");
        dto.setCategoria("SOCIAL");
        dto.setValorObjetivo(5);

        when(logroRepository.save(any(Logro.class))).thenAnswer(inv -> {
            Logro l = inv.getArgument(0);
            l.setId(50L);
            return l;
        });

        LogroDTO result = service.crearLogro(dto);
        assertNotNull(result);
        assertEquals("NUEVO_LOGRO", result.getCodigo());
        assertEquals("SOCIAL", result.getCategoria());
    }
}
