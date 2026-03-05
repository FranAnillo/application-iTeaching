package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.ForoRespuesta;
import iteaching.app.Models.ForoTema;
import iteaching.app.Models.Persona;
import iteaching.app.dto.ForoRespuestaDTO;
import iteaching.app.dto.ForoTemaDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.ForoRespuestaRepository;
import iteaching.app.repository.ForoTemaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForoService {

    private final ForoTemaRepository temaRepository;
    private final ForoRespuestaRepository respuestaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;

    public ForoService(ForoTemaRepository temaRepository,
                       ForoRespuestaRepository respuestaRepository,
                       AsignaturaRepository asignaturaRepository,
                       PersonaRepository personaRepository) {
        this.temaRepository = temaRepository;
        this.respuestaRepository = respuestaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
    }

    public List<ForoTemaDTO> findTemasByAsignatura(Long asignaturaId) {
        return temaRepository.findByAsignaturaIdOrderByFijadoDescFechaCreacionDesc(asignaturaId)
                .stream().map(t -> toTemaDTO(t, false)).collect(Collectors.toList());
    }

    public ForoTemaDTO findTemaById(Long id) {
        ForoTema t = temaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tema de foro no encontrado"));
        return toTemaDTO(t, true);
    }

    @Transactional
    public ForoTemaDTO createTema(ForoTemaDTO dto, String username) {
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona autor = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ForoTema t = new ForoTema();
        t.setTitulo(InputSanitizer.sanitize(dto.getTitulo()));
        t.setContenido(InputSanitizer.sanitize(dto.getContenido()));
        t.setFijado(dto.getFijado() != null ? dto.getFijado() : false);
        t.setFechaCreacion(LocalDateTime.now());
        t.setAsignatura(asignatura);
        t.setAutor(autor);

        return toTemaDTO(temaRepository.save(t), false);
    }

    @Transactional
    public ForoRespuestaDTO createRespuesta(ForoRespuestaDTO dto, String username) {
        ForoTema tema = temaRepository.findById(dto.getTemaId())
                .orElseThrow(() -> new RuntimeException("Tema no encontrado"));
        Persona autor = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ForoRespuesta r = new ForoRespuesta();
        r.setContenido(InputSanitizer.sanitize(dto.getContenido()));
        r.setFechaCreacion(LocalDateTime.now());
        r.setTema(tema);
        r.setAutor(autor);

        return toRespuestaDTO(respuestaRepository.save(r));
    }

    @Transactional
    public void deleteTema(Long id) {
        if (!temaRepository.existsById(id))
            throw new RuntimeException("Tema no encontrado");
        temaRepository.deleteById(id);
    }

    @Transactional
    public void deleteRespuesta(Long id) {
        if (!respuestaRepository.existsById(id))
            throw new RuntimeException("Respuesta no encontrada");
        respuestaRepository.deleteById(id);
    }

    private ForoTemaDTO toTemaDTO(ForoTema t, boolean includeRespuestas) {
        ForoTemaDTO dto = new ForoTemaDTO();
        dto.setId(t.getId());
        dto.setTitulo(t.getTitulo());
        dto.setContenido(t.getContenido());
        dto.setFechaCreacion(t.getFechaCreacion().toString());
        dto.setFijado(t.getFijado());
        dto.setAsignaturaId(t.getAsignatura().getId());
        dto.setAsignaturaNombre(t.getAsignatura().getNombre());
        dto.setAutorId(t.getAutor().getId());
        dto.setAutorNombre(t.getAutor().getNombreCompleto());
        dto.setTotalRespuestas(t.getRespuestas() != null ? t.getRespuestas().size() : 0);
        if (includeRespuestas && t.getRespuestas() != null) {
            dto.setRespuestas(t.getRespuestas().stream()
                    .map(this::toRespuestaDTO).collect(Collectors.toList()));
        } else {
            dto.setRespuestas(Collections.emptyList());
        }
        return dto;
    }

    private ForoRespuestaDTO toRespuestaDTO(ForoRespuesta r) {
        ForoRespuestaDTO dto = new ForoRespuestaDTO();
        dto.setId(r.getId());
        dto.setContenido(r.getContenido());
        dto.setFechaCreacion(r.getFechaCreacion().toString());
        dto.setTemaId(r.getTema().getId());
        dto.setAutorId(r.getAutor().getId());
        dto.setAutorNombre(r.getAutor().getNombreCompleto());
        return dto;
    }
}
