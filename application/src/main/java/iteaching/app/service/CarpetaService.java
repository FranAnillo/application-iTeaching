package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Carpeta;
import iteaching.app.dto.CarpetaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.CarpetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarpetaService {

    private final CarpetaRepository carpetaRepository;
    private final AsignaturaRepository asignaturaRepository;

    public CarpetaService(CarpetaRepository carpetaRepository,
                          AsignaturaRepository asignaturaRepository) {
        this.carpetaRepository = carpetaRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    public List<CarpetaDTO> findByAsignatura(Long asignaturaId) {
        return carpetaRepository.findByAsignaturaId(asignaturaId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<CarpetaDTO> findRootByAsignatura(Long asignaturaId) {
        return carpetaRepository.findByAsignaturaIdAndPadreIsNull(asignaturaId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<CarpetaDTO> findSubcarpetas(Long padreId) {
        return carpetaRepository.findByPadreId(padreId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public CarpetaDTO findById(Long id) {
        return toDTO(carpetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada")));
    }

    @Transactional
    public CarpetaDTO create(CarpetaDTO dto) {
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Carpeta c = new Carpeta();
        c.setNombre(dto.getNombre());
        c.setAsignatura(asignatura);

        if (dto.getPadreId() != null) {
            Carpeta padre = carpetaRepository.findById(dto.getPadreId())
                    .orElseThrow(() -> new RuntimeException("Carpeta padre no encontrada"));
            c.setPadre(padre);
        }

        return toDTO(carpetaRepository.save(c));
    }

    @Transactional
    public CarpetaDTO update(Long id, CarpetaDTO dto) {
        Carpeta c = carpetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada"));
        c.setNombre(dto.getNombre());
        return toDTO(carpetaRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        if (!carpetaRepository.existsById(id))
            throw new RuntimeException("Carpeta no encontrada");
        carpetaRepository.deleteById(id);
    }

    private CarpetaDTO toDTO(Carpeta c) {
        CarpetaDTO dto = new CarpetaDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setAsignaturaId(c.getAsignatura().getId());
        dto.setAsignaturaNombre(c.getAsignatura().getNombre());
        dto.setPadreId(c.getPadre() != null ? c.getPadre().getId() : null);
        dto.setPadreNombre(c.getPadre() != null ? c.getPadre().getNombre() : null);
        return dto;
    }
}
