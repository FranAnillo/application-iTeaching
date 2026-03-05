package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.Models.Valoracion;
import iteaching.app.dto.ValoracionDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.ValoracionRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValoracionService — unit tests")
class ValoracionServiceTest {

    @Mock private ValoracionRepository valoracionRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private ContentModerationService moderationService;

    @InjectMocks private ValoracionService service;

    private Persona alumno;
    private Persona profesor;
    private Asignatura asignatura;
    private Valoracion valoracion;

    @BeforeEach
    void setUp() {
        alumno = new Persona();
        alumno.setId(1L);
        alumno.setUsername("alumno1");
        alumno.setNombre("Est");
        alumno.setApellido("Y");
        alumno.setRole(Usuarios.Role.ROLE_ESTUDIANTE);

        profesor = new Persona();
        profesor.setId(2L);
        profesor.setUsername("prof1");
        profesor.setNombre("Prof");
        profesor.setApellido("X");
        profesor.setRole(Usuarios.Role.ROLE_PROFESOR);

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Mates");
        asignatura.setProfesores(new HashSet<>(Set.of(profesor)));
        asignatura.setEstudiantes(new HashSet<>(Set.of(alumno)));

        valoracion = new Valoracion();
        valoracion.setId(100L);
        valoracion.setPuntuacion(4.5);
        valoracion.setComentario("Buen profesor");
        valoracion.setPuntosMejora("Más ejemplos");
        valoracion.setFechaCreacion(LocalDateTime.now());
        valoracion.setProfesor(profesor);
        valoracion.setAlumno(alumno);
        valoracion.setAsignatura(asignatura);
    }

    @Test
    void findAll_returnsList() {
        when(valoracionRepository.findAll()).thenReturn(List.of(valoracion));
        List<ValoracionDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals(4.5, result.get(0).getPuntuacion());
    }

    @Test
    void findByProfesor_returnsList() {
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(valoracionRepository.findByProfesor(profesor)).thenReturn(List.of(valoracion));
        List<ValoracionDTO> result = service.findByProfesor(2L);
        assertEquals(1, result.size());
    }

    @Test
    void findByProfesor_notFound_throws() {
        when(personaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findByProfesor(999L));
    }

    @Test
    void findByAsignatura_returnsList() {
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(valoracionRepository.findByAsignatura(asignatura)).thenReturn(List.of(valoracion));
        assertEquals(1, service.findByAsignatura(5L).size());
    }

    @Test
    void getPromedioByProfesor_returnsAverage() {
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(valoracionRepository.getPromedioByProfesor(profesor)).thenReturn(4.333);
        assertEquals(4.33, service.getPromedioByProfesor(2L));
    }

    @Test
    void getPromedioByProfesor_nullReturnsZero() {
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(valoracionRepository.getPromedioByProfesor(profesor)).thenReturn(null);
        assertEquals(0.0, service.getPromedioByProfesor(2L));
    }

    @Test
    void create_success() {
        ValoracionDTO dto = new ValoracionDTO();
        dto.setPuntuacion(5.0);
        dto.setComentario("Excelente");
        dto.setPuntosMejora("Nada");
        dto.setProfesorId(2L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("alumno1")).thenReturn(Optional.of(alumno));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(valoracionRepository.findByAlumnoAndProfesorAndAsignatura(alumno, profesor, asignatura))
                .thenReturn(Optional.empty());
        when(moderationService.moderate("Excelente"))
                .thenReturn(new ContentModerationService.ModerationResult(true, null));
        when(moderationService.moderate("Nada"))
                .thenReturn(new ContentModerationService.ModerationResult(true, null));
        when(valoracionRepository.save(any(Valoracion.class))).thenAnswer(inv -> {
            Valoracion v = inv.getArgument(0);
            v.setId(200L);
            return v;
        });
        when(valoracionRepository.getPromedioByProfesor(profesor)).thenReturn(4.75);

        ValoracionDTO result = service.create(dto, "alumno1");
        assertNotNull(result);
        assertEquals(5.0, result.getPuntuacion());
        // DTO anónimo: no expone datos del alumno
        verify(personaRepository).save(profesor); // actualizar media
    }

    @Test
    void create_notEstudiante_throws() {
        Persona profUser = new Persona();
        profUser.setId(3L);
        profUser.setUsername("prof2");
        profUser.setRole(Usuarios.Role.ROLE_PROFESOR);

        ValoracionDTO dto = new ValoracionDTO();
        dto.setProfesorId(2L);
        dto.setAsignaturaId(5L);
        dto.setPuntuacion(3.0);

        when(personaRepository.findByUsername("prof2")).thenReturn(Optional.of(profUser));
        assertThrows(RuntimeException.class, () -> service.create(dto, "prof2"));
    }

    @Test
    void create_profesorNotInAsignatura_throws() {
        Persona otroProf = new Persona();
        otroProf.setId(99L);
        otroProf.setRole(Usuarios.Role.ROLE_PROFESOR);

        // asignatura does not contain otroProf
        ValoracionDTO dto = new ValoracionDTO();
        dto.setPuntuacion(3.0);
        dto.setProfesorId(99L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("alumno1")).thenReturn(Optional.of(alumno));
        when(personaRepository.findById(99L)).thenReturn(Optional.of(otroProf));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));

        assertThrows(RuntimeException.class, () -> service.create(dto, "alumno1"));
    }

    @Test
    void create_alumnoNotEnrolled_throws() {
        Persona otroAlumno = new Persona();
        otroAlumno.setId(88L);
        otroAlumno.setUsername("otro");
        otroAlumno.setRole(Usuarios.Role.ROLE_ESTUDIANTE);

        // asignatura.estudiantes does NOT contain otroAlumno
        ValoracionDTO dto = new ValoracionDTO();
        dto.setPuntuacion(4.0);
        dto.setProfesorId(2L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("otro")).thenReturn(Optional.of(otroAlumno));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));

        assertThrows(RuntimeException.class, () -> service.create(dto, "otro"));
    }

    @Test
    void create_duplicate_throws() {
        ValoracionDTO dto = new ValoracionDTO();
        dto.setPuntuacion(4.0);
        dto.setProfesorId(2L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("alumno1")).thenReturn(Optional.of(alumno));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(valoracionRepository.findByAlumnoAndProfesorAndAsignatura(alumno, profesor, asignatura))
                .thenReturn(Optional.of(valoracion));

        assertThrows(RuntimeException.class, () -> service.create(dto, "alumno1"));
    }

    @Test
    void create_moderationRejectsComment_throws() {
        ValoracionDTO dto = new ValoracionDTO();
        dto.setPuntuacion(1.0);
        dto.setComentario("Insulto grosero");
        dto.setProfesorId(2L);
        dto.setAsignaturaId(5L);

        when(personaRepository.findByUsername("alumno1")).thenReturn(Optional.of(alumno));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(valoracionRepository.findByAlumnoAndProfesorAndAsignatura(alumno, profesor, asignatura))
                .thenReturn(Optional.empty());
        when(moderationService.moderate("Insulto grosero"))
                .thenReturn(new ContentModerationService.ModerationResult(false, "Lenguaje ofensivo"));

        assertThrows(RuntimeException.class, () -> service.create(dto, "alumno1"));
    }

    @Test
    void delete_success_recalculatesAverage() {
        when(valoracionRepository.findById(100L)).thenReturn(Optional.of(valoracion));
        when(valoracionRepository.getPromedioByProfesor(profesor)).thenReturn(4.0);

        service.delete(100L);
        verify(valoracionRepository).delete(valoracion);
        verify(personaRepository).save(profesor);
        assertEquals(4.0, profesor.getPuntuacion());
    }

    @Test
    void delete_notFound_throws() {
        when(valoracionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }

    @Test
    void delete_noMoreValoraciones_setsZero() {
        when(valoracionRepository.findById(100L)).thenReturn(Optional.of(valoracion));
        when(valoracionRepository.getPromedioByProfesor(profesor)).thenReturn(null);

        service.delete(100L);
        assertEquals(0.0, profesor.getPuntuacion());
    }
}
