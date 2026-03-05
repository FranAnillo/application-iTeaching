package iteaching.app.service;

import iteaching.app.Models.*;
import iteaching.app.dto.MaterialDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.CarpetaRepository;
import iteaching.app.repository.MaterialRepository;
import iteaching.app.repository.PersonaRepository;
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
@DisplayName("MaterialService — unit tests")
class MaterialServiceTest {

    @Mock private MaterialRepository materialRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private CarpetaRepository carpetaRepository;

    @InjectMocks private MaterialService service;

    private Persona autor;
    private Asignatura asignatura;
    private Material material;
    private Carpeta carpeta;

    @BeforeEach
    void setUp() {
        autor = new Persona();
        autor.setId(1L);
        autor.setUsername("prof1");
        autor.setNombre("Prof");
        autor.setApellido("Uno");

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Física");
        asignatura.setProfesores(new HashSet<>());
        asignatura.setEstudiantes(new HashSet<>());

        carpeta = new Carpeta();
        carpeta.setId(3L);
        carpeta.setNombre("Tema 1");
        carpeta.setAsignatura(asignatura);

        material = new Material();
        material.setId(10L);
        material.setTitulo("Apuntes T1");
        material.setDescripcion("Desc");
        material.setUrlRecurso("https://drive.com/file");
        material.setTipo(Material.TipoMaterial.DOCUMENTO);
        material.setFechaCreacion(LocalDateTime.now());
        material.setAutor(autor);
        material.setAsignatura(asignatura);
    }

    @Test
    void findAll_returnsList() {
        when(materialRepository.findAll()).thenReturn(List.of(material));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void findById_found() {
        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));
        MaterialDTO dto = service.findById(10L);
        assertEquals("Apuntes T1", dto.getTitulo());
    }

    @Test
    void findById_notFound_throws() {
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void findByAsignatura_returnsList() {
        when(materialRepository.findByAsignaturaId(5L)).thenReturn(List.of(material));
        assertEquals(1, service.findByAsignatura(5L).size());
    }

    @Test
    void search_returnsList() {
        when(materialRepository.findByTituloContainingIgnoreCase("apuntes")).thenReturn(List.of(material));
        assertEquals(1, service.search("apuntes").size());
    }

    @Test
    void create_success_withAsignaturaAndCarpeta() {
        MaterialDTO dto = new MaterialDTO();
        dto.setTitulo("Nuevo Material");
        dto.setDescripcion("Descripción");
        dto.setUrlRecurso("https://example.com");
        dto.setTipo("VIDEO");
        dto.setAsignaturaId(5L);
        dto.setCarpetaId(3L);

        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(autor));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(carpetaRepository.findById(3L)).thenReturn(Optional.of(carpeta));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> {
            Material m = inv.getArgument(0);
            m.setId(20L);
            return m;
        });

        MaterialDTO result = service.create(dto, "prof1");
        assertNotNull(result);
        assertEquals("Nuevo Material", result.getTitulo());
        assertEquals("VIDEO", result.getTipo());
    }

    @Test
    void create_defaultsTipoToDocumento() {
        MaterialDTO dto = new MaterialDTO();
        dto.setTitulo("Material sin tipo");
        // no setTipo

        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(autor));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> {
            Material m = inv.getArgument(0);
            m.setId(21L);
            return m;
        });

        MaterialDTO result = service.create(dto, "prof1");
        assertEquals("DOCUMENTO", result.getTipo());
    }

    @Test
    void create_userNotFound_throws() {
        MaterialDTO dto = new MaterialDTO();
        dto.setTitulo("Test");
        when(personaRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(dto, "ghost"));
    }

    @Test
    void update_success() {
        MaterialDTO dto = new MaterialDTO();
        dto.setTitulo("Updated");
        dto.setDescripcion("New Desc");
        dto.setUrlRecurso("https://new.com");
        dto.setTipo("ENLACE");
        dto.setAsignaturaId(5L);

        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));
        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(materialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MaterialDTO result = service.update(10L, dto);
        assertEquals("Updated", result.getTitulo());
        assertEquals("ENLACE", result.getTipo());
    }

    @Test
    void update_notFound_throws() {
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(999L, new MaterialDTO()));
    }

    @Test
    void delete_success() {
        when(materialRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(materialRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(materialRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }
}
