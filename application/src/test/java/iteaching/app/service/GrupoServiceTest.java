package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Grupo;
import iteaching.app.Models.Persona;
import iteaching.app.dto.GrupoDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.GrupoRepository;
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
@DisplayName("GrupoService — unit tests")
class GrupoServiceTest {

    @Mock private GrupoRepository grupoRepository;
    @Mock private AsignaturaRepository asignaturaRepository;
    @Mock private PersonaRepository personaRepository;

    @InjectMocks private GrupoService service;

    private Asignatura asignatura;
    private Grupo grupo;
    private Persona persona;

    @BeforeEach
    void setUp() {
        asignatura = new Asignatura();
        asignatura.setId(5L);
        asignatura.setNombre("Mates");
        asignatura.setProfesores(new ArrayList<>());
        asignatura.setEstudiantes(new ArrayList<>());

        grupo = new Grupo();
        grupo.setId(10L);
        grupo.setNombre("Grupo A");
        grupo.setTipo(Grupo.TipoGrupo.TEORIA);
        grupo.setInscribible(false);
        grupo.setAsignatura(asignatura);
        grupo.setMiembros(new HashSet<>());

        persona = new Persona();
        persona.setId(1L);
        persona.setUsername("user1");
        persona.setNombre("Juan");
        persona.setApellido("García");
    }

    @Test
    void findByAsignatura_returnsList() {
        when(grupoRepository.findByAsignaturaId(5L)).thenReturn(List.of(grupo));
        assertEquals(1, service.findByAsignatura(5L).size());
    }

    @Test
    void findById_found() {
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        GrupoDTO dto = service.findById(10L);
        assertEquals("Grupo A", dto.getNombre());
        assertEquals("TEORIA", dto.getTipo());
    }

    @Test
    void findById_notFound_throws() {
        when(grupoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void create_success() {
        GrupoDTO dto = new GrupoDTO();
        dto.setNombre("Grupo B");
        dto.setTipo("PRACTICA");
        dto.setInscribible(true);
        dto.setAsignaturaId(5L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> {
            Grupo g = inv.getArgument(0);
            g.setId(20L);
            g.setMiembros(new HashSet<>());
            return g;
        });

        GrupoDTO result = service.create(dto);
        assertEquals("Grupo B", result.getNombre());
        assertEquals("PRACTICA", result.getTipo());
        assertTrue(result.isInscribible());
    }

    @Test
    void create_defaultsTipoToTeoria() {
        GrupoDTO dto = new GrupoDTO();
        dto.setNombre("Grupo C");
        dto.setAsignaturaId(5L);

        when(asignaturaRepository.findById(5L)).thenReturn(Optional.of(asignatura));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> {
            Grupo g = inv.getArgument(0);
            g.setId(21L);
            g.setMiembros(new HashSet<>());
            return g;
        });

        GrupoDTO result = service.create(dto);
        assertEquals("TEORIA", result.getTipo());
    }

    @Test
    void update_success() {
        GrupoDTO dto = new GrupoDTO();
        dto.setNombre("Updated");
        dto.setTipo("PRACTICA");
        dto.setInscribible(true);

        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        when(grupoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GrupoDTO result = service.update(10L, dto);
        assertEquals("Updated", result.getNombre());
        assertEquals("PRACTICA", result.getTipo());
    }

    @Test
    void delete_success() {
        when(grupoRepository.existsById(10L)).thenReturn(true);
        assertDoesNotThrow(() -> service.delete(10L));
        verify(grupoRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        when(grupoRepository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.delete(999L));
    }

    @Test
    void addMiembro_success() {
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(grupoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GrupoDTO result = service.addMiembro(10L, 1L);
        assertTrue(result.getMiembroIds().contains(1L));
    }

    @Test
    void removeMiembro_success() {
        grupo.getMiembros().add(persona);
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        when(grupoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GrupoDTO result = service.removeMiembro(10L, 1L);
        assertFalse(result.getMiembroIds().contains(1L));
    }

    @Test
    void toggleInscribible_togglesValue() {
        assertFalse(grupo.isInscribible());
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        when(grupoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GrupoDTO result = service.toggleInscribible(10L);
        assertTrue(result.isInscribible());
    }

    @Test
    void selfEnrol_success_whenInscribible() {
        grupo.setInscribible(true);
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(persona));
        when(grupoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GrupoDTO result = service.selfEnrol(10L, "user1");
        assertTrue(result.getMiembroIds().contains(1L));
    }

    @Test
    void selfEnrol_notInscribible_throws() {
        grupo.setInscribible(false);
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        assertThrows(RuntimeException.class, () -> service.selfEnrol(10L, "user1"));
    }

    @Test
    void selfUnenrol_success() {
        grupo.getMiembros().add(persona);
        when(grupoRepository.findById(10L)).thenReturn(Optional.of(grupo));
        when(personaRepository.findByUsername("user1")).thenReturn(Optional.of(persona));
        when(grupoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GrupoDTO result = service.selfUnenrol(10L, "user1");
        assertFalse(result.getMiembroIds().contains(1L));
    }
}
