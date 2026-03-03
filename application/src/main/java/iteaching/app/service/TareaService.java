package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Tarea;
import iteaching.app.dto.TareaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;

    public TareaService(TareaRepository tareaRepository,
                        AsignaturaRepository asignaturaRepository,
                        PersonaRepository personaRepository) {
        this.tareaRepository = tareaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
    }

    public List<TareaDTO> findByAsignatura(Long asignaturaId) {
        return tareaRepository.findByAsignaturaIdOrderByFechaEntregaAsc(asignaturaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TareaDTO findById(Long id) {
        return toDTO(tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada")));
    }

    @Transactional
    public TareaDTO create(TareaDTO dto, String username) {
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona creador = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Tarea t = new Tarea();
        t.setTitulo(dto.getTitulo());
        t.setDescripcion(dto.getDescripcion());
        t.setFechaCreacion(LocalDateTime.now());
        t.setFechaEntrega(LocalDateTime.parse(dto.getFechaEntrega()));
        t.setPuntuacionMaxima(dto.getPuntuacionMaxima() != null ? dto.getPuntuacionMaxima() : 10.0);
        t.setAsignatura(asignatura);
        t.setCreador(creador);

        return toDTO(tareaRepository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        if (!tareaRepository.existsById(id))
            throw new RuntimeException("Tarea no encontrada");
        tareaRepository.deleteById(id);
    }

    private TareaDTO toDTO(Tarea t) {
        TareaDTO dto = new TareaDTO();
        dto.setId(t.getId());
        dto.setTitulo(t.getTitulo());
        dto.setDescripcion(t.getDescripcion());
        dto.setFechaCreacion(t.getFechaCreacion().toString());
        dto.setFechaEntrega(t.getFechaEntrega().toString());
        dto.setPuntuacionMaxima(t.getPuntuacionMaxima());
        dto.setAsignaturaId(t.getAsignatura().getId());
        dto.setAsignaturaNombre(t.getAsignatura().getNombre());
        dto.setCreadorId(t.getCreador().getId());
        dto.setCreadorNombre(t.getCreador().getNombreCompleto());
        dto.setTotalEntregas(t.getEntregas() != null ? t.getEntregas().size() : 0);
        return dto;
    }
}
