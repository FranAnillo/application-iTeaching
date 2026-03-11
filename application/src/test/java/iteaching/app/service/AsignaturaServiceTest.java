package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AsignaturaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsignaturaService — unit tests")
class AsignaturaServiceTest {

    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private PersonaRepository personaRepository;

    @InjectMocks private AsignaturaService service;

    private Persona admin;
    private Asignatura asignatura;

    @BeforeEach
    void setUp() {
        admin = new Persona();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setRole(Usuarios.Role.ROLE_ADMIN);

        asignatura = new Asignatura();
        asignatura.setId(10L);
        asignatura.setNombre("Matemáticas");
        asignatura.setDescripcion("Curso de mates");
        asignatura.setUrl("https://example.com");
        asignatura.setCreador(admin);
        asignatura.setProfesores(new ArrayList<>());
        asignatura.setEstudiantes(new ArrayList<>());
    }

    @Test
    void findAll_returnsList() {
        when(asignaturaRepository.findAll()).thenReturn(List.of(asignatura));
        List<AsignaturaDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("Matemáticas", result.get(0).getNombre());
    }

    @Test
    void findById_found() {
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        AsignaturaDTO dto = service.findById(10L);
        assertEquals(10L, dto.getId());
        assertEquals("Matemáticas", dto.getNombre());
    }

    @Test
    void findById_notFound_throws() {
        when(asignaturaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void search_delegatesToRepo() {
        when(asignaturaRepository.findByNombreContainingIgnoreCase("mat"))
                .thenReturn(List.of(asignatura));
        List<AsignaturaDTO> result = service.search("mat");
        assertEquals(1, result.size());
    }

    @Test
    void create_success() {
        when(personaRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> {
            Asignatura a = inv.getArgument(0);
            a.setId(10L);
            a.setProfesores(new ArrayList<>());
            a.setEstudiantes(new ArrayList<>());
            return a;
        });

        AsignaturaDTO dto = new AsignaturaDTO();
        dto.setNombre("Física");
        dto.setDescripcion("Curso de física");
        dto.setUrl("https://fisica.com");

        AsignaturaDTO result = service.create(dto, "admin");
        assertNotNull(result);
        assertEquals("Física", result.getNombre());
        verify(asignaturaRepository).save(any(Asignatura.class));
    }

    @Test
    void create_userNotFound_throws() {
        when(personaRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        AsignaturaDTO dto = new AsignaturaDTO();
        dto.setNombre("Test");
        assertThrows(RuntimeException.class, () -> service.create(dto, "ghost"));
    }

    @Test
    void update_success() {
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> inv.getArgument(0));

        AsignaturaDTO dto = new AsignaturaDTO();
        dto.setNombre("Matemáticas II");
        dto.setDescripcion("Avanzado");
        dto.setUrl("https://mates2.com");

        AsignaturaDTO result = service.update(10L, dto);
        assertEquals("Matemáticas II", result.getNombre());
    }

    @Test
    void update_notFound_throws() {
        when(asignaturaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(999L, new AsignaturaDTO()));
    }

    @Test
    void delete_success() {
        when(asignaturaRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(asignaturaRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(asignaturaRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }

    @Test
    void addProfesor_success() {
        Persona prof = new Persona();
        prof.setId(2L);
        prof.setNombre("Prof");
        prof.setApellido("X");
        prof.setRole(Usuarios.Role.ROLE_PROFESOR);

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(prof));
        when(asignaturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AsignaturaDTO result = service.addProfesor(10L, 2L);
        assertTrue(result.getProfesorIds().contains(2L));
    }

    @Test
    void addProfesor_notProfesor_throws() {
        Persona estudiante = new Persona();
        estudiante.setId(3L);
        estudiante.setRole(Usuarios.Role.ROLE_ESTUDIANTE);

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findById(3L)).thenReturn(Optional.of(estudiante));

        assertThrows(RuntimeException.class, () -> service.addProfesor(10L, 3L));
    }

    @Test
    void removeProfesor_success() {
        Persona prof = new Persona();
        prof.setId(2L);
        prof.setNombre("Prof");
        prof.setApellido("X");
        prof.setRole(Usuarios.Role.ROLE_PROFESOR);
        asignatura.getProfesores().add(prof);

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(asignaturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AsignaturaDTO result = service.removeProfesor(10L, 2L);
        assertFalse(result.getProfesorIds().contains(2L));
    }

    @Test
    void addEstudiante_success() {
        Persona est = new Persona();
        est.setId(4L);
        est.setNombre("Est");
        est.setApellido("Y");
        est.setRole(Usuarios.Role.ROLE_ESTUDIANTE);

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findById(4L)).thenReturn(Optional.of(est));
        when(asignaturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AsignaturaDTO result = service.addEstudiante(10L, 4L);
        assertTrue(result.getEstudianteIds().contains(4L));
    }

    @Test
    void addEstudiante_notEstudiante_throws() {
        Persona prof = new Persona();
        prof.setId(2L);
        prof.setRole(Usuarios.Role.ROLE_PROFESOR);

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findById(2L)).thenReturn(Optional.of(prof));

        assertThrows(RuntimeException.class, () -> service.addEstudiante(10L, 2L));
    }

    @Test
    void inscribirse_success() {
        Persona est = new Persona();
        est.setId(4L);
        est.setUsername("alumno");
        est.setNombre("Est");
        est.setApellido("Y");

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("alumno")).thenReturn(Optional.of(est));
        when(asignaturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AsignaturaDTO result = service.inscribirse(10L, "alumno");
        assertTrue(result.getEstudianteIds().contains(4L));
    }

    @Test
    void desinscribirse_success() {
        Persona est = new Persona();
        est.setId(4L);
        est.setUsername("alumno");
        est.setNombre("Est");
        est.setApellido("Y");
        asignatura.getEstudiantes().add(est);

        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(asignaturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AsignaturaDTO result = service.desinscribirse(10L, "alumno");
        assertFalse(result.getEstudianteIds().contains(4L));
    }

    @Test
    void importFromCsv_success() {
        String csv = "nombre;descripcion;url\nFísica;Física básica;https://example.com\nQuímica;;";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(personaRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> {
            Asignatura a = inv.getArgument(0);
            a.setId(100L);
            a.setProfesores(new ArrayList<>());
            a.setEstudiantes(new ArrayList<>());
            return a;
        });

        List<AsignaturaDTO> result = service.importFromCsv(is, "admin");
        assertEquals(2, result.size());
        verify(asignaturaRepository, times(2)).save(any());
    }

    @Test
    void importFromCsv_exceedsMaxLines_throws() {
        StringBuilder sb = new StringBuilder("nombre;descripcion;url\n");
        for (int i = 0; i < 501; i++) {
            sb.append("Asignatura").append(i).append(";desc;url\n");
        }
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

        when(personaRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> {
            Asignatura a = inv.getArgument(0);
            a.setId(1L);
            a.setProfesores(new ArrayList<>());
            a.setEstudiantes(new ArrayList<>());
            return a;
        });

        assertThrows(RuntimeException.class, () -> service.importFromCsv(is, "admin"));
    }

    @Test
    void importFromCsv_skipsEmptyLines() {
        String csv = "nombre;descripcion;url\n;;\nFísica;desc;url";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(personaRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> {
            Asignatura a = inv.getArgument(0);
            a.setId(100L);
            a.setProfesores(new ArrayList<>());
            a.setEstudiantes(new ArrayList<>());
            return a;
        });

        List<AsignaturaDTO> result = service.importFromCsv(is, "admin");
        assertEquals(1, result.size());
    }
}
