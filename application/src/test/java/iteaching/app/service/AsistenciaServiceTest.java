package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Asistencia;
import iteaching.app.Models.Persona;
import iteaching.app.dto.AsistenciaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.AsistenciaRepository;
import iteaching.app.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsistenciaService — unit tests")
class AsistenciaServiceTest {

    @Mock private AsistenciaRepository asistenciaRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;

    @InjectMocks private AsistenciaService service;

    private Persona estudiante;
    private Persona registradoPor;
    private Asignatura asignatura;
    private Asistencia asistencia;

    @BeforeEach
    void setUp() {
        estudiante = new Persona();
        estudiante.setId(1L);
        estudiante.setNombre("Est");
        estudiante.setApellido("Y");

        registradoPor = new Persona();
        registradoPor.setId(2L);
        registradoPor.setUsername("prof1");
        registradoPor.setNombre("Prof");
        registradoPor.setApellido("X");

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Mates");
        asignatura.setProfesores(new HashSet<>());
        asignatura.setEstudiantes(new HashSet<>());

        asistencia = new Asistencia();
        asistencia.setId(10L);
        asistencia.setFecha(LocalDate.of(2026, 3, 5));
        asistencia.setEstado(Asistencia.EstadoAsistencia.PRESENTE);
        asistencia.setObservacion("OK");
        asistencia.setEstudiante(estudiante);
        asistencia.setAsignatura(asignatura);
        asistencia.setRegistradoPor(registradoPor);
    }

    @Test
    void getByAsignatura_returnsList() {
        when(asistenciaRepository.findByAsignaturaIdOrderByFechaDesc(5L))
                .thenReturn(List.of(asistencia));
        assertEquals(1, service.getByAsignatura(5L).size());
    }

    @Test
    void getByAsignaturaAndFecha_returnsList() {
        LocalDate fecha = LocalDate.of(2026, 3, 5);
        when(asistenciaRepository.findByAsignaturaIdAndFecha(5L, fecha))
                .thenReturn(List.of(asistencia));
        assertEquals(1, service.getByAsignaturaAndFecha(5L, fecha).size());
    }

    @Test
    void getByEstudiante_returnsList() {
        when(asistenciaRepository.findByEstudianteIdAndAsignaturaIdOrderByFechaDesc(1L, 5L))
                .thenReturn(List.of(asistencia));
        assertEquals(1, service.getByEstudiante(1L, 5L).size());
    }

    @Test
    void registrar_newRecord() {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setFecha("2026-03-06");
        dto.setEstado("AUSENTE");
        dto.setObservacion("No vino");
        dto.setEstudianteId(1L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(registradoPor));
        when(asistenciaRepository.findByEstudianteIdAndAsignaturaIdAndFecha(1L, 5L, LocalDate.parse("2026-03-06")))
                .thenReturn(Optional.empty());
        when(personaRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> {
            Asistencia a = inv.getArgument(0);
            a.setId(20L);
            return a;
        });

        AsistenciaDTO result = service.registrar(dto, "prof1");
        assertNotNull(result);
        assertEquals("AUSENTE", result.getEstado());
    }

    @Test
    void registrar_updatesExisting() {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setFecha("2026-03-05");
        dto.setEstado("TARDANZA");
        dto.setObservacion("Llegó tarde");
        dto.setEstudianteId(1L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(registradoPor));
        when(asistenciaRepository.findByEstudianteIdAndAsignaturaIdAndFecha(1L, 5L, LocalDate.parse("2026-03-05")))
                .thenReturn(Optional.of(asistencia));
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> inv.getArgument(0));

        AsistenciaDTO result = service.registrar(dto, "prof1");
        assertEquals("TARDANZA", result.getEstado());
    }

    @Test
    void registrar_userNotFound_throws() {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setFecha("2026-03-05");
        dto.setEstado("PRESENTE");
        dto.setEstudianteId(1L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.registrar(dto, "ghost"));
    }

    @Test
    void registrarLote_callsRegistrarForEach() {
        AsistenciaDTO dto1 = new AsistenciaDTO();
        dto1.setFecha("2026-03-05");
        dto1.setEstado("PRESENTE");
        dto1.setEstudianteId(1L);
        dto1.setAsignaturaId(5L);

        AsistenciaDTO dto2 = new AsistenciaDTO();
        dto2.setFecha("2026-03-05");
        dto2.setEstado("AUSENTE");
        dto2.setEstudianteId(1L);
        dto2.setAsignaturaId(5L);

        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(registradoPor));
        when(asistenciaRepository.findByEstudianteIdAndAsignaturaIdAndFecha(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(personaRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> {
            Asistencia a = inv.getArgument(0);
            a.setId(30L);
            return a;
        });

        assertDoesNotThrow(() -> service.registrarLote(List.of(dto1, dto2), "prof1"));
        verify(asistenciaRepository, times(2)).save(any());
    }
}
