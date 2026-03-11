package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Grado;
import iteaching.app.dto.GradoDTO;
import iteaching.app.enums.CursoAcademico;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.GradoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradoService — unit tests")
class GradoServiceTest {

    @Mock private GradoRepository gradoRepository;
    @Mock private AsignaturaRepository asignaturaRepository;

    @InjectMocks private GradoService service;

    private Grado grado;
    private Asignatura asignatura;

    @BeforeEach
    void setUp() {
        grado = new Grado();
        grado.setId(1L);
        grado.setNombre("Ingenieria");
        grado.setCursoAcademico(CursoAcademico.valueOf("Y2025_2026"));
        asignatura = new Asignatura();
        asignatura.setId(10L);
        asignatura.setNombre("Matematicas");
    }

    @Test
    void findAll_invokesRepo() {
        when(gradoRepository.findAll()).thenReturn(List.of(grado));
        List<GradoDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("Ingenieria", result.get(0).getNombre());
    }

    @Test
    void save_returnsSaved() {
        GradoDTO dto = new GradoDTO();
        dto.setNombre("Ingenieria");
        grado.setCursoAcademico(CursoAcademico.valueOf("Y2025_2026"));
        when(gradoRepository.save(any(Grado.class))).thenReturn(grado);
        GradoDTO result = service.save(dto);
        assertEquals("Ingenieria", result.getNombre());
    }

    @Test
    void findById_found() {
        when(gradoRepository.findById(1L)).thenReturn(Optional.of(grado));
        GradoDTO result = service.findById(1L);
        assertEquals(1L, result.getId());
        assertEquals("Ingenieria", result.getNombre());
    }

    @Test
    void findById_notFound_throws() {
        when(gradoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void getAsignaturas_returnsList() {
        when(asignaturaRepository.findByGradoId(1L)).thenReturn(List.of(asignatura));
        List<Asignatura> result = service.getAsignaturas(1L);
        assertEquals(1, result.size());
        assertEquals("Matematicas", result.get(0).getNombre());
    }

    @Test
    void addAsignatura_success() {
        when(gradoRepository.findById(1L)).thenReturn(Optional.of(grado));
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> inv.getArgument(0));
        GradoDTO result = service.addAsignatura(1L, 10L);
        assertEquals(1L, result.getId());
    }

    @Test
    void removeAsignatura_success() {
        asignatura.setGrado(grado);
        when(gradoRepository.findById(1L)).thenReturn(Optional.of(grado));
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(inv -> inv.getArgument(0));
        GradoDTO result = service.removeAsignatura(1L, 10L);
        assertEquals(1L, result.getId());
    }
}
