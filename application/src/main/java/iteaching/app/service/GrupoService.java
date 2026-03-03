package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Grupo;
import iteaching.app.Models.Persona;
import iteaching.app.dto.GrupoDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.GrupoRepository;
import iteaching.app.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;

    public GrupoService(GrupoRepository grupoRepository,
                        AsignaturaRepository asignaturaRepository,
                        PersonaRepository personaRepository) {
        this.grupoRepository = grupoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
    }

    public List<GrupoDTO> findByAsignatura(Long asignaturaId) {
        return grupoRepository.findByAsignaturaId(asignaturaId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public GrupoDTO findById(Long id) {
        return toDTO(grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado")));
    }

    @Transactional
    public GrupoDTO create(GrupoDTO dto) {
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Grupo g = new Grupo();
        g.setNombre(dto.getNombre());
        g.setTipo(dto.getTipo() != null
                ? Grupo.TipoGrupo.valueOf(dto.getTipo())
                : Grupo.TipoGrupo.TEORIA);
        g.setAsignatura(asignatura);

        return toDTO(grupoRepository.save(g));
    }

    @Transactional
    public GrupoDTO update(Long id, GrupoDTO dto) {
        Grupo g = grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        g.setNombre(dto.getNombre());
        if (dto.getTipo() != null) {
            g.setTipo(Grupo.TipoGrupo.valueOf(dto.getTipo()));
        }
        return toDTO(grupoRepository.save(g));
    }

    @Transactional
    public void delete(Long id) {
        if (!grupoRepository.existsById(id))
            throw new RuntimeException("Grupo no encontrado");
        grupoRepository.deleteById(id);
    }

    @Transactional
    public GrupoDTO addMiembro(Long grupoId, Long personaId) {
        Grupo g = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        Persona p = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        g.getMiembros().add(p);
        return toDTO(grupoRepository.save(g));
    }

    @Transactional
    public GrupoDTO removeMiembro(Long grupoId, Long personaId) {
        Grupo g = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        g.getMiembros().removeIf(p -> p.getId().equals(personaId));
        return toDTO(grupoRepository.save(g));
    }

    private GrupoDTO toDTO(Grupo g) {
        Set<Long> miembroIds = g.getMiembros() != null
                ? g.getMiembros().stream().map(Persona::getId).collect(Collectors.toSet())
                : new HashSet<>();
        GrupoDTO dto = new GrupoDTO();
        dto.setId(g.getId());
        dto.setNombre(g.getNombre());
        dto.setTipo(g.getTipo().name());
        dto.setAsignaturaId(g.getAsignatura().getId());
        dto.setAsignaturaNombre(g.getAsignatura().getNombre());
        dto.setMiembroIds(miembroIds);
        return dto;
    }
}
