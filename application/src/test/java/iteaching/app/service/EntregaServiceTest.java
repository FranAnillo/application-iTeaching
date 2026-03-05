package iteaching.app.service;

import iteaching.app.Models.Entrega;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Tarea;
import iteaching.app.dto.EntregaDTO;
import iteaching.app.repository.EntregaRepository;
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
@DisplayName("EntregaService — unit tests")
class EntregaServiceTest {

    @Mock private EntregaRepository entregaRepository;
    @Mock private TareaRepository tareaRepository;
    @Mock private PersonaRepository personaRepository;

    @InjectMocks private EntregaService service;

    private Persona estudiante;
    private Tarea tarea;
    private Entrega entrega;
    private iteaching.app.Models.Asignatura asignatura;

    @BeforeEach
    void setUp() {
        asignatura = new iteaching.app.Models.Asignatura();
        asignatura.setId(1L);
        asignatura.setNombre("Mates");
        asignatura.setProfesores(new HashSet<>());
        asignatura.setEstudiantes(new HashSet<>());

        Persona creador = new Persona();
        creador.setId(10L);
        creador.setNombre("Prof");
        creador.setApellido("X");

        estudiante = new Persona();
        estudiante.setId(2L);
        estudiante.setUsername("alumno1");
        estudiante.setNombre("Est");
        estudiante.setApellido("Y");

        tarea = new Tarea();
        tarea.setId(5L);
        tarea.setTitulo("Ejercicio 1");
        tarea.setAsignatura(asignatura);
        tarea.setCreador(creador);
        tarea.setEntregas(new HashSet<>());

        entrega = new Entrega();
        entrega.setId(100L);
        entrega.setContenido("Mi respuesta");
        entrega.setUrlAdjunto("https://file.com/doc.pdf");
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setTarea(tarea);
        entrega.setEstudiante(estudiante);
    }

    @Test
    void findByTarea_returnsList() {
        when(entregaRepository.findByTareaId(5L)).thenReturn(List.of(entrega));
        List<EntregaDTO> result = service.findByTarea(5L);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getTareaId());
    }

    @Test
    void findByEstudiante_returnsList() {
        when(entregaRepository.findByEstudianteUsername("alumno1")).thenReturn(List.of(entrega));
        List<EntregaDTO> result = service.findByEstudiante("alumno1");
        assertEquals(1, result.size());
    }

    @Test
    void submit_success() {
        EntregaDTO dto = new EntregaDTO();
        dto.setContenido("Mi entrega");
        dto.setUrlAdjunto("https://drive.com/file");
        dto.setTareaId(5L);

        when(tareaRepository.findById(5L)).thenReturn(Optional.of(tarea));
        when(personaRepository.findByUsername("alumno1")).thenReturn(Optional.of(estudiante));
        when(entregaRepository.findByTareaIdAndEstudianteId(5L, 2L)).thenReturn(Optional.empty());
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> {
            Entrega e = inv.getArgument(0);
            e.setId(200L);
            return e;
        });

        EntregaDTO result = service.submit(dto, "alumno1");
        assertNotNull(result);
        assertEquals(5L, result.getTareaId());
        assertEquals(2L, result.getEstudianteId());
    }

    @Test
    void submit_duplicateSubmission_throws() {
        EntregaDTO dto = new EntregaDTO();
        dto.setContenido("Otra entrega");
        dto.setTareaId(5L);

        when(tareaRepository.findById(5L)).thenReturn(Optional.of(tarea));
        when(personaRepository.findByUsername("alumno1")).thenReturn(Optional.of(estudiante));
        when(entregaRepository.findByTareaIdAndEstudianteId(5L, 2L)).thenReturn(Optional.of(entrega));

        assertThrows(RuntimeException.class, () -> service.submit(dto, "alumno1"));
    }

    @Test
    void submit_tareaNotFound_throws() {
        EntregaDTO dto = new EntregaDTO();
        dto.setTareaId(999L);
        when(tareaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.submit(dto, "alumno1"));
    }

    @Test
    void submit_userNotFound_throws() {
        EntregaDTO dto = new EntregaDTO();
        dto.setTareaId(5L);
        when(tareaRepository.findById(5L)).thenReturn(Optional.of(tarea));
        when(personaRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.submit(dto, "ghost"));
    }

    @Test
    void calificar_success() {
        when(entregaRepository.findById(100L)).thenReturn(Optional.of(entrega));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> inv.getArgument(0));

        EntregaDTO result = service.calificar(100L, 8.5, "Buen trabajo");
        assertEquals(8.5, result.getCalificacion());
        assertEquals("Buen trabajo", result.getComentarioProfesor());
    }

    @Test
    void calificar_notFound_throws() {
        when(entregaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.calificar(999L, 5.0, "Ok"));
    }
}
