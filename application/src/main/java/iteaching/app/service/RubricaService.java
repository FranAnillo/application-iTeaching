package iteaching.app.service;

import iteaching.app.Models.CriterioRubrica;
import iteaching.app.Models.Rubrica;
import iteaching.app.dto.CriterioRubricaDTO;
import iteaching.app.dto.RubricaDTO;
import iteaching.app.repository.RubricaRepository;
import iteaching.app.repository.TareaRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RubricaService {

    private final RubricaRepository rubricaRepository;
    private final TareaRepository tareaRepository;

    public RubricaService(RubricaRepository rubricaRepository, TareaRepository tareaRepository) {
        this.rubricaRepository = rubricaRepository;
        this.tareaRepository = tareaRepository;
    }

    public RubricaDTO getByTarea(Long tareaId) {
        return rubricaRepository.findByTareaId(tareaId)
            .map(this::toDTO)
            .orElse(null);
    }

    public RubricaDTO getById(Long id) {
        return rubricaRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("Rúbrica no encontrada"));
    }

    @Transactional
    public RubricaDTO crear(RubricaDTO dto) {
        Rubrica rubrica = new Rubrica();
        rubrica.setNombre(InputSanitizer.sanitize(dto.getNombre()));
        rubrica.setDescripcion(InputSanitizer.sanitize(dto.getDescripcion()));

        if (dto.getTareaId() != null) {
            rubrica.setTarea(tareaRepository.findById(dto.getTareaId())
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada")));
        }

        // Add criterios
        if (dto.getCriterios() != null) {
            for (CriterioRubricaDTO cDto : dto.getCriterios()) {
                CriterioRubrica criterio = new CriterioRubrica();
                criterio.setNombre(InputSanitizer.sanitize(cDto.getNombre()));
                criterio.setDescripcion(InputSanitizer.sanitize(cDto.getDescripcion()));
                criterio.setPuntuacionMaxima(cDto.getPuntuacionMaxima());
                criterio.setOrden(cDto.getOrden());
                criterio.setNivelExcelente(InputSanitizer.sanitize(cDto.getNivelExcelente()));
                criterio.setNivelBueno(InputSanitizer.sanitize(cDto.getNivelBueno()));
                criterio.setNivelSuficiente(InputSanitizer.sanitize(cDto.getNivelSuficiente()));
                criterio.setNivelInsuficiente(InputSanitizer.sanitize(cDto.getNivelInsuficiente()));
                criterio.setRubrica(rubrica);
                rubrica.getCriterios().add(criterio);
            }
        }

        return toDTO(rubricaRepository.save(rubrica));
    }

    @Transactional
    public void eliminar(Long id) {
        rubricaRepository.deleteById(id);
    }

    private RubricaDTO toDTO(Rubrica r) {
        RubricaDTO dto = new RubricaDTO();
        dto.setId(r.getId());
        dto.setNombre(r.getNombre());
        dto.setDescripcion(r.getDescripcion());
        if (r.getTarea() != null) {
            dto.setTareaId(r.getTarea().getId());
            dto.setTareaTitulo(r.getTarea().getTitulo());
        }
        dto.setCriterios(r.getCriterios().stream().map(c -> {
            CriterioRubricaDTO cDto = new CriterioRubricaDTO();
            cDto.setId(c.getId());
            cDto.setNombre(c.getNombre());
            cDto.setDescripcion(c.getDescripcion());
            cDto.setPuntuacionMaxima(c.getPuntuacionMaxima());
            cDto.setOrden(c.getOrden());
            cDto.setNivelExcelente(c.getNivelExcelente());
            cDto.setNivelBueno(c.getNivelBueno());
            cDto.setNivelSuficiente(c.getNivelSuficiente());
            cDto.setNivelInsuficiente(c.getNivelInsuficiente());
            return cDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
