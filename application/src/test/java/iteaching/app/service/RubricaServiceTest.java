package iteaching.app.service;

import iteaching.app.Models.CriterioRubrica;
import iteaching.app.Models.Rubrica;
import iteaching.app.Models.Tarea;
import iteaching.app.dto.CriterioRubricaDTO;
import iteaching.app.dto.RubricaDTO;
import iteaching.app.repository.RubricaRepository;
import iteaching.app.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RubricaService — unit tests")
class RubricaServiceTest {

    @Mock private RubricaRepository rubricaRepository;
    @Mock private TareaRepository tareaRepository;

    @InjectMocks private RubricaService service;

    private Tarea tarea;
    private Rubrica rubrica;

    @BeforeEach
    void setUp() {
        tarea = new Tarea();
        tarea.setId(5L);
        tarea.setTitulo("Examen T1");

        CriterioRubrica criterio = new CriterioRubrica();
        criterio.setId(1L);
        criterio.setNombre("Presentación");
        criterio.setDescripcion("Calidad de presentación");
        criterio.setPuntuacionMaxima(5.0);
        criterio.setOrden(1);
        criterio.setNivelExcelente("Perfecto");
        criterio.setNivelBueno("Bien");
        criterio.setNivelSuficiente("Aceptable");
        criterio.setNivelInsuficiente("Mal");

        rubrica = new Rubrica();
        rubrica.setId(10L);
        rubrica.setNombre("Rúbrica Examen");
        rubrica.setDescripcion("Para evaluar el examen");
        rubrica.setTarea(tarea);
        rubrica.setCriterios(new ArrayList<>(List.of(criterio)));
        criterio.setRubrica(rubrica);
    }

    @Test
    void getByTarea_found() {
        when(rubricaRepository.findByTareaId(5L)).thenReturn(Optional.of(rubrica));
        RubricaDTO result = service.getByTarea(5L);
        assertNotNull(result);
        assertEquals("Rúbrica Examen", result.getNombre());
        assertEquals(1, result.getCriterios().size());
    }

    @Test
    void getByTarea_notFound_returnsNull() {
        when(rubricaRepository.findByTareaId(999L)).thenReturn(Optional.empty());
        assertNull(service.getByTarea(999L));
    }

    @Test
    void getById_found() {
        when(rubricaRepository.findById(10L)).thenReturn(Optional.of(rubrica));
        RubricaDTO result = service.getById(10L);
        assertEquals(10L, result.getId());
    }

    @Test
    void getById_notFound_throws() {
        when(rubricaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(999L));
    }

    @Test
    void crear_success_withCriterios() {
        CriterioRubricaDTO cDto = new CriterioRubricaDTO();
        cDto.setNombre("Contenido");
        cDto.setDescripcion("Calidad del contenido");
        cDto.setPuntuacionMaxima(10.0);
        cDto.setOrden(1);
        cDto.setNivelExcelente("Excelente");
        cDto.setNivelBueno("Bueno");
        cDto.setNivelSuficiente("Suficiente");
        cDto.setNivelInsuficiente("Insuficiente");

        RubricaDTO dto = new RubricaDTO();
        dto.setNombre("Nueva Rúbrica");
        dto.setDescripcion("Desc");
        dto.setTareaId(5L);
        dto.setCriterios(List.of(cDto));

        when(tareaRepository.findById(5L)).thenReturn(Optional.of(tarea));
        when(rubricaRepository.save(any(Rubrica.class))).thenAnswer(inv -> {
            Rubrica r = inv.getArgument(0);
            r.setId(20L);
            // Simulate ID assignment to criterios
            int idx = 0;
            for (CriterioRubrica c : r.getCriterios()) {
                c.setId((long)(idx + 1));
                idx++;
            }
            return r;
        });

        RubricaDTO result = service.crear(dto);
        assertNotNull(result);
        assertEquals("Nueva Rúbrica", result.getNombre());
        assertEquals(1, result.getCriterios().size());
        assertEquals("Contenido", result.getCriterios().get(0).getNombre());
    }

    @Test
    void crear_withoutTarea_success() {
        RubricaDTO dto = new RubricaDTO();
        dto.setNombre("Rúbrica sin tarea");
        dto.setDescripcion("General");

        when(rubricaRepository.save(any(Rubrica.class))).thenAnswer(inv -> {
            Rubrica r = inv.getArgument(0);
            r.setId(30L);
            return r;
        });

        RubricaDTO result = service.crear(dto);
        assertNotNull(result);
        assertNull(result.getTareaId());
    }

    @Test
    void crear_tareaNotFound_throws() {
        RubricaDTO dto = new RubricaDTO();
        dto.setNombre("Test");
        dto.setTareaId(999L);

        when(tareaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.crear(dto));
    }

    @Test
    void eliminar_delegates() {
        service.eliminar(10L);
        verify(rubricaRepository).deleteById(10L);
    }
}
