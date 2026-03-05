package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Tarea;
import iteaching.app.dto.TareaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TareaService — unit tests")
class TareaServiceTest {

    @Mock private TareaRepository tareaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private PersonaRepository personaRepository;

    @InjectMocks private TareaService service;

    private Persona creador;
    private Asignatura asignatura;
    private Tarea tarea;

    @BeforeEach
    void setUp() {
        creador = new Persona();
        creador.setId(1L);
        creador.setUsername("prof1");
        creador.setNombre("Prof");
        creador.setApellido("Uno");

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Matemáticas");
        asignatura.setProfesores(new HashSet<>());
        asignatura.setEstudiantes(new HashSet<>());

        tarea = new Tarea();
        tarea.setId(10L);
        tarea.setTitulo("Ejercicio 1");
        tarea.setDescripcion("Resolver problemas");
        tarea.setFechaCreacion(LocalDateTime.now());
        tarea.setFechaEntrega(LocalDateTime.now().plusDays(7));
        tarea.setPuntuacionMaxima(10.0);
        tarea.setTipoTarea(Tarea.TipoTarea.TAREA);
        tarea.setAsignatura(asignatura);
        tarea.setCreador(creador);
        tarea.setEntregas(new HashSet<>());
    }

    @Test
    void findByAsignatura_returnsList() {
        when(tareaRepository.findByAsignaturaIdOrderByFechaEntregaAsc(5L)).thenReturn(List.of(tarea));
        List<TareaDTO> result = service.findByAsignatura(5L);
        assertEquals(1, result.size());
        assertEquals("Ejercicio 1", result.get(0).getTitulo());
    }

    @Test
    void findById_found() {
        when(tareaRepository.findById(10L)).thenReturn(Optional.of(tarea));
        TareaDTO result = service.findById(10L);
        assertEquals(10L, result.getId());
        assertEquals("Ejercicio 1", result.getTitulo());
    }

    @Test
    void findById_notFound_throws() {
        when(tareaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void create_success() {
        TareaDTO dto = new TareaDTO();
        dto.setTitulo("Nueva Tarea");
        dto.setDescripcion("Descripción");
        dto.setFechaEntrega(LocalDateTime.now().plusDays(5).toString());
        dto.setPuntuacionMaxima(8.0);
        dto.setTipoTarea("EVALUACION");
        dto.setAsignaturaId(5L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(creador));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> {
            Tarea t = inv.getArgument(0);
            t.setId(20L);
            t.setEntregas(new HashSet<>());
            return t;
        });

        TareaDTO result = service.create(dto, "prof1");
        assertNotNull(result);
        assertEquals("Nueva Tarea", result.getTitulo());
        assertEquals("EVALUACION", result.getTipoTarea());
        assertEquals(8.0, result.getPuntuacionMaxima());
    }

    @Test
    void create_defaultsTipoToTarea() {
        TareaDTO dto = new TareaDTO();
        dto.setTitulo("Tarea default");
        dto.setFechaEntrega(LocalDateTime.now().plusDays(5).toString());
        dto.setAsignaturaId(5L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(creador));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> {
            Tarea t = inv.getArgument(0);
            t.setId(21L);
            t.setEntregas(new HashSet<>());
            return t;
        });

        TareaDTO result = service.create(dto, "prof1");
        assertEquals("TAREA", result.getTipoTarea());
        assertEquals(10.0, result.getPuntuacionMaxima());
    }

    @Test
    void create_asignaturaNotFound_throws() {
        TareaDTO dto = new TareaDTO();
        dto.setAsignaturaId(999L);
        when(asignaturaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(dto, "prof1"));
    }

    @Test
    void delete_success() {
        when(tareaRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(tareaRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(tareaRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }
}
