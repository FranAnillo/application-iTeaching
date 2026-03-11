package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Clase;
import iteaching.app.Models.EstadoClase;
import iteaching.app.Models.Persona;
import iteaching.app.dto.ClaseCreateRequest;
import iteaching.app.dto.ClaseDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.ClaseRepository;
import iteaching.app.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaseService — unit tests")
class ClaseServiceTest {

    @Mock private ClaseRepository claseRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;

    @InjectMocks private ClaseService service;

    private Persona alumno;
    private Persona profesor;
    private Asignatura asignatura;
    private Clase clase;

    @BeforeEach
    void setUp() {
        alumno = new Persona();
        alumno.setId(1L);
        alumno.setUsername("alumno1");
        alumno.setNombre("Est");
        alumno.setApellido("Y");

        profesor = new Persona();
        profesor.setId(2L);
        profesor.setUsername("prof1");
        profesor.setNombre("Prof");
        profesor.setApellido("X");

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Mates");
        asignatura.setProfesores(new ArrayList<>());
        asignatura.setEstudiantes(new ArrayList<>());

        clase = new Clase();
        clase.setId(10L);
        clase.setHoraComienzo("10:00");
        clase.setHoraFin("11:00");
        clase.setAlumno(alumno);
        clase.setProfesor(profesor);
        clase.setAsignatura(asignatura);
        clase.setEstadoClase(EstadoClase.SOLICITADA);
        clase.setAceptacionAlumno(true);
        clase.setAceptacionProfesor(false);
    }

    @Test
    void findAll_returnsList() {
        when(claseRepository.findAll()).thenReturn(List.of(clase));
        List<ClaseDTO> result = service.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void findById_found() {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(clase));
        ClaseDTO dto = service.findById(10L);
        assertEquals("10:00", dto.getHoraComienzo());
        assertEquals("SOLICITADA", dto.getEstadoClase());
    }

    @Test
    void findById_notFound_throws() {
        when(claseRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void findByAlumno_returnsList() {
        when(claseRepository.findByAlumnoUsername("alumno1")).thenReturn(List.of(clase));
        assertEquals(1, service.findByAlumno("alumno1").size());
    }

    @Test
    void findByProfesor_returnsList() {
        when(claseRepository.findByProfesorUsername("prof1")).thenReturn(List.of(clase));
        assertEquals(1, service.findByProfesor("prof1").size());
    }

    @Test
    void findByEstado_returnsList() {
        when(claseRepository.findByEstadoClase(EstadoClase.SOLICITADA)).thenReturn(List.of(clase));
        assertEquals(1, service.findByEstado(EstadoClase.SOLICITADA).size());
    }

    @Test
    void create_success() {
        ClaseCreateRequest request = new ClaseCreateRequest("09:00", "10:00", 1L, 2L, 5L);

        when(personaRepository.findById(1L)).thenReturn(Optional.of(alumno));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(claseRepository.save(any(Clase.class))).thenAnswer(inv -> {
            Clase c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });

        ClaseDTO result = service.create(request);
        assertNotNull(result);
        assertEquals("09:00", result.getHoraComienzo());
        assertEquals("SOLICITADA", result.getEstadoClase());
        assertTrue(result.getAceptacionAlumno());
        assertFalse(result.getAceptacionProfesor());
    }

    @Test
    void create_alumnoNotFound_throws() {
        ClaseCreateRequest request = new ClaseCreateRequest("09:00", "10:00", 999L, 2L, 5L);
        when(personaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(request));
    }

    @Test
    void updateEstado_aceptada() {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(clase));
        when(claseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaseDTO result = service.updateEstado(10L, EstadoClase.ACEPTADA);
        assertEquals("ACEPTADA", result.getEstadoClase());
        assertTrue(result.getAceptacionProfesor());
    }

    @Test
    void updateEstado_cancelada() {
        clase.setAceptacionProfesor(true);
        when(claseRepository.findById(10L)).thenReturn(Optional.of(clase));
        when(claseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaseDTO result = service.updateEstado(10L, EstadoClase.CANCELADA);
        assertEquals("CANCELADA", result.getEstadoClase());
        assertFalse(result.getAceptacionProfesor());
    }

    @Test
    void updateEstado_rechazada() {
        when(claseRepository.findById(10L)).thenReturn(Optional.of(clase));
        when(claseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaseDTO result = service.updateEstado(10L, EstadoClase.RECHAZADA);
        assertEquals("RECHAZADA", result.getEstadoClase());
        assertFalse(result.getAceptacionProfesor());
    }

    @Test
    void delete_success() {
        when(claseRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(claseRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(claseRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }
}
