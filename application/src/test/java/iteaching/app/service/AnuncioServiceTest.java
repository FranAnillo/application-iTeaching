package iteaching.app.service;

import iteaching.app.Models.Anuncio;
import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.dto.AnuncioDTO;
import iteaching.app.repository.AnuncioRepository;
import iteaching.app.repository.AsignaturaRepository;
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
@DisplayName("AnuncioService — unit tests")
class AnuncioServiceTest {

    @Mock private AnuncioRepository anuncioRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private PersonaRepository personaRepository;

    @InjectMocks private AnuncioService service;

    private Persona autor;
    private Asignatura asignatura;
    private Anuncio anuncio;

    @BeforeEach
    void setUp() {
        autor = new Persona();
        autor.setId(1L);
        autor.setUsername("prof1");
        autor.setNombre("Prof");
        autor.setApellido("Uno");

        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Historia");
        asignatura.setProfesores(new HashSet<>());
        asignatura.setEstudiantes(new HashSet<>());

        anuncio = new Anuncio();
        anuncio.setId(10L);
        anuncio.setTitulo("Examen lunes");
        anuncio.setContenido("El examen del lunes es el tema 3");
        anuncio.setFechaCreacion(LocalDateTime.now());
        anuncio.setImportante(true);
        anuncio.setAsignatura(asignatura);
        anuncio.setAutor(autor);
    }

    @Test
    void findByAsignatura_returnsList() {
        when(anuncioRepository.findByAsignaturaIdOrderByFechaCreacionDesc(5L))
                .thenReturn(List.of(anuncio));
        List<AnuncioDTO> result = service.findByAsignatura(5L);
        assertEquals(1, result.size());
        assertEquals("Examen lunes", result.get(0).getTitulo());
    }

    @Test
    void findById_found() {
        when(anuncioRepository.findById(10L)).thenReturn(Optional.of(anuncio));
        AnuncioDTO dto = service.findById(10L);
        assertEquals(10L, dto.getId());
        assertTrue(dto.getImportante());
    }

    @Test
    void findById_notFound_throws() {
        when(anuncioRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void create_success() {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setTitulo("Nuevo anuncio");
        dto.setContenido("Contenido del anuncio");
        dto.setImportante(false);
        dto.setAsignaturaId(5L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(autor));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> {
            Anuncio a = inv.getArgument(0);
            a.setId(20L);
            return a;
        });

        AnuncioDTO result = service.create(dto, "prof1");
        assertNotNull(result);
        assertEquals("Nuevo anuncio", result.getTitulo());
        assertFalse(result.getImportante());
    }

    @Test
    void create_defaultsImportanteToFalse() {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setTitulo("T");
        dto.setContenido("C");
        dto.setAsignaturaId(5L);
        // importante not set

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(personaRepository.findByUsername("prof1")).thenReturn(Optional.of(autor));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> {
            Anuncio a = inv.getArgument(0);
            a.setId(21L);
            return a;
        });

        AnuncioDTO result = service.create(dto, "prof1");
        assertFalse(result.getImportante());
    }

    @Test
    void create_asignaturaNotFound_throws() {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setAsignaturaId(999L);
        when(asignaturaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(dto, "prof1"));
    }

    @Test
    void delete_success() {
        when(anuncioRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(anuncioRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(anuncioRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }
}
