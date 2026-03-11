package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.ForoRespuesta;
import iteaching.app.Models.ForoTema;
import iteaching.app.Models.Persona;
import iteaching.app.dto.ForoRespuestaDTO;
import iteaching.app.dto.ForoTemaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.ForoRespuestaRepository;
import iteaching.app.repository.ForoTemaRepository;
import iteaching.app.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForoService — unit tests")
class ForoServiceTest {

    @Mock private ForoTemaRepository temaRepository;
    @Mock private ForoRespuestaRepository respuestaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private PersonaRepository personaRepository;

    @InjectMocks private ForoService service;

    private Persona autor;
    private Asignatura asignatura;
    private ForoTema tema;

    @BeforeEach
    void setUp() {
        autor = new Persona();
        autor.setId(1L);
        autor.setUsername("user1");
        autor.setNombre("Juan");
        autor.setApellido("García");

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Física");
        asignatura.setProfesores(new ArrayList<>());
        asignatura.setEstudiantes(new ArrayList<>());

        tema = new ForoTema();
        tema.setId(10L);
        tema.setTitulo("Dudas tema 1");
        tema.setContenido("¿Alguien entiende la fórmula?");
        tema.setFechaCreacion(LocalDateTime.now());
        tema.setFijado(false);
        tema.setAsignatura(asignatura);
        tema.setAutor(autor);
        tema.setRespuestas(new ArrayList<>());
    }

    @Test
    void findTemasByAsignatura_returnsList() {
        when(temaRepository.findByAsignaturaIdOrderByFijadoDescFechaCreacionDesc(5L))
                .thenReturn(List.of(tema));
        List<ForoTemaDTO> result = service.findTemasByAsignatura(5L);
        assertEquals(1, result.size());
        assertEquals("Dudas tema 1", result.get(0).getTitulo());
    }

    @Test
    void findTemaById_found_includesRespuestas() {
        ForoRespuesta respuesta = new ForoRespuesta();
        respuesta.setId(20L);
        respuesta.setContenido("Sí, mira la página 5");
        respuesta.setFechaCreacion(LocalDateTime.now());
        respuesta.setTema(tema);
        respuesta.setAutor(autor);
        tema.getRespuestas().add(respuesta);

        when(temaRepository.findById(10L)).thenReturn(Optional.of(tema));
        ForoTemaDTO result = service.findTemaById(10L);
        assertEquals(10L, result.getId());
        assertEquals(1, result.getRespuestas().size());
    }

    @Test
    void findTemaById_notFound_throws() {
        when(temaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findTemaById(999L));
    }

    @Test
    void createTema_success() {
        ForoTemaDTO dto = new ForoTemaDTO();
        dto.setTitulo("Nuevo tema");
        dto.setContenido("Contenido");
        dto.setAsignaturaId(5L);
        dto.setFijado(true);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(autor));
        when(temaRepository.save(any(ForoTema.class))).thenAnswer(inv -> {
            ForoTema t = inv.getArgument(0);
            t.setId(30L);
            t.setRespuestas(new ArrayList<>());
            return t;
        });

        ForoTemaDTO result = service.createTema(dto, "user1");
        assertNotNull(result);
        assertEquals("Nuevo tema", result.getTitulo());
        assertTrue(result.getFijado());
    }

    @Test
    void createTema_defaultsFijadoToFalse() {
        ForoTemaDTO dto = new ForoTemaDTO();
        dto.setTitulo("Tema");
        dto.setContenido("C");
        dto.setAsignaturaId(5L);
        // fijado not set

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(autor));
        when(temaRepository.save(any(ForoTema.class))).thenAnswer(inv -> {
            ForoTema t = inv.getArgument(0);
            t.setId(31L);
            t.setRespuestas(new ArrayList<>());
            return t;
        });

        ForoTemaDTO result = service.createTema(dto, "user1");
        assertFalse(result.getFijado());
    }

    @Test
    void createRespuesta_success() {
        ForoRespuestaDTO dto = new ForoRespuestaDTO();
        dto.setContenido("Mi respuesta");
        dto.setTemaId(10L);

        when(temaRepository.findById(10L)).thenReturn(Optional.of(tema));
        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(autor));
        when(respuestaRepository.save(any(ForoRespuesta.class))).thenAnswer(inv -> {
            ForoRespuesta r = inv.getArgument(0);
            r.setId(40L);
            return r;
        });

        ForoRespuestaDTO result = service.createRespuesta(dto, "user1");
        assertNotNull(result);
        assertEquals(10L, result.getTemaId());
    }

    @Test
    void createRespuesta_temaNotFound_throws() {
        ForoRespuestaDTO dto = new ForoRespuestaDTO();
        dto.setTemaId(999L);
        when(temaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createRespuesta(dto, "user1"));
    }

    @Test
    void deleteTema_success() {
        when(temaRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.deleteTema(10L));
        verify(temaRepository).deleteById(10L);
    }

    @Test
    void deleteTema_notFound_throws() {
        when(temaRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.deleteTema(999L));
    }

    @Test
    void deleteRespuesta_success() {
        when(respuestaRepository.existsById(20L)).thenReturn(true);
        assertDoesNotThrow(() -> service.deleteRespuesta(20L));
        verify(respuestaRepository).deleteById(20L);
    }

    @Test
    void deleteRespuesta_notFound_throws() {
        when(respuestaRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.deleteRespuesta(999L));
    }
}
