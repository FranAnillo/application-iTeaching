package iteaching.app.service;

import iteaching.app.Models.Anuncio;
import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.dto.AnuncioDTO;
import iteaching.app.repository.AnuncioRepository;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;

    public AnuncioService(AnuncioRepository anuncioRepository,
                          AsignaturaRepository asignaturaRepository,
                          PersonaRepository personaRepository) {
        this.anuncioRepository = anuncioRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
    }

    public List<AnuncioDTO> findByAsignatura(Long asignaturaId) {
        return anuncioRepository.findByAsignaturaIdOrderByFechaCreacionDesc(asignaturaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AnuncioDTO findById(Long id) {
        return toDTO(anuncioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anuncio no encontrado")));
    }

    @Transactional
    public AnuncioDTO create(AnuncioDTO dto, String username) {
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona autor = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Anuncio a = new Anuncio();
        a.setTitulo(InputSanitizer.sanitize(dto.getTitulo()));
        a.setContenido(InputSanitizer.sanitize(dto.getContenido()));
        a.setImportante(dto.getImportante() != null ? dto.getImportante() : false);
        a.setFechaCreacion(LocalDateTime.now());
        a.setAsignatura(asignatura);
        a.setAutor(autor);

        return toDTO(anuncioRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        if (!anuncioRepository.existsById(id))
            throw new RuntimeException("Anuncio no encontrado");
        anuncioRepository.deleteById(id);
    }

    private AnuncioDTO toDTO(Anuncio a) {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setId(a.getId());
        dto.setTitulo(a.getTitulo());
        dto.setContenido(a.getContenido());
        dto.setFechaCreacion(a.getFechaCreacion().toString());
        dto.setImportante(a.getImportante());
        dto.setAsignaturaId(a.getAsignatura().getId());
        dto.setAsignaturaNombre(a.getAsignatura().getNombre());
        dto.setAutorId(a.getAutor().getId());
        dto.setAutorNombre(a.getAutor().getNombreCompleto());
        return dto;
    }
}
