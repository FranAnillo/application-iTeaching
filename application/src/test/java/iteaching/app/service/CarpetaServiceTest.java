package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Carpeta;
import iteaching.app.dto.CarpetaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.CarpetaRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarpetaService — unit tests")
class CarpetaServiceTest {

    @Mock private CarpetaRepository carpetaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;

    @InjectMocks private CarpetaService service;

    private Asignatura asignatura;
    private Carpeta carpeta;

    @BeforeEach
    void setUp() {
        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Física");
        asignatura.setProfesores(new HashSet<>());
        asignatura.setEstudiantes(new HashSet<>());

        carpeta = new Carpeta();
        carpeta.setId(10L);
        carpeta.setNombre("Tema 1");
        carpeta.setAsignatura(asignatura);
    }

    @Test
    void findByAsignatura_returnsList() {
        when(carpetaRepository.findByAsignaturaId(5L)).thenReturn(List.of(carpeta));
        assertEquals(1, service.findByAsignatura(5L).size());
    }

    @Test
    void findRootByAsignatura_returnsList() {
        when(carpetaRepository.findByAsignaturaIdAndPadreIsNull(5L)).thenReturn(List.of(carpeta));
        assertEquals(1, service.findRootByAsignatura(5L).size());
    }

    @Test
    void findSubcarpetas_returnsList() {
        Carpeta sub = new Carpeta();
        sub.setId(11L);
        sub.setNombre("Subtema");
        sub.setAsignatura(asignatura);
        sub.setPadre(carpeta);
        when(carpetaRepository.findByPadreId(10L)).thenReturn(List.of(sub));
        List<CarpetaDTO> result = service.findSubcarpetas(10L);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getPadreId());
    }

    @Test
    void findById_found() {
        when(carpetaRepository.findById(10L)).thenReturn(Optional.of(carpeta));
        CarpetaDTO dto = service.findById(10L);
        assertEquals("Tema 1", dto.getNombre());
    }

    @Test
    void findById_notFound_throws() {
        when(carpetaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void create_success_root() {
        CarpetaDTO dto = new CarpetaDTO();
        dto.setNombre("Nueva carpeta");
        dto.setAsignaturaId(5L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(carpetaRepository.save(any(Carpeta.class))).thenAnswer(inv -> {
            Carpeta c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });

        CarpetaDTO result = service.create(dto);
        assertEquals("Nueva carpeta", result.getNombre());
        assertNull(result.getPadreId());
    }

    @Test
    void create_success_withParent() {
        CarpetaDTO dto = new CarpetaDTO();
        dto.setNombre("Subcarpeta");
        dto.setAsignaturaId(5L);
        dto.setPadreId(10L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(carpetaRepository.findById(10L)).thenReturn(Optional.of(carpeta));
        when(carpetaRepository.save(any(Carpeta.class))).thenAnswer(inv -> {
            Carpeta c = inv.getArgument(0);
            c.setId(21L);
            return c;
        });

        CarpetaDTO result = service.create(dto);
        assertEquals(10L, result.getPadreId());
    }

    @Test
    void update_success() {
        when(carpetaRepository.findById(10L)).thenReturn(Optional.of(carpeta));
        when(carpetaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarpetaDTO dto = new CarpetaDTO();
        dto.setNombre("Updated");
        CarpetaDTO result = service.update(10L, dto);
        assertEquals("Updated", result.getNombre());
    }

    @Test
    void update_notFound_throws() {
        when(carpetaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(999L, new CarpetaDTO()));
    }

    @Test
    void delete_success() {
        when(carpetaRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(carpetaRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(carpetaRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }
}
