package iteaching.app.controller;

import iteaching.app.Models.Anuncio;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AnuncioDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.service.AnuncioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioController {

    private final AnuncioService anuncioService;
    private final PersonaRepository personaRepository;

    public AnuncioController(AnuncioService anuncioService, PersonaRepository personaRepository) {
        this.anuncioService = anuncioService;
        this.personaRepository = personaRepository;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<AnuncioDTO>> findByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(anuncioService.findByAsignatura(asignaturaId));
    }

    @GetMapping("/global")
    public ResponseEntity<List<AnuncioDTO>> findGlobal() {
        return ResponseEntity.ok(anuncioService.findGlobal());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnuncioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(anuncioService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<AnuncioDTO> create(@RequestBody AnuncioDTO dto, Authentication auth) {
        Usuarios.Role role = personaRepository.findByUsername(auth.getName())
                .map(p -> p.getRole())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(anuncioService.create(dto, auth.getName(), role));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping("/global-or-grupo")
    public ResponseEntity<AnuncioDTO> crearGlobalOGrupo(@RequestBody AnuncioDTO dto) {
        Anuncio anuncio = anuncioService.crearAnuncioGlobalOGrupo(dto);
        AnuncioDTO response = new AnuncioDTO();
        response.setId(anuncio.getId());
        response.setTitulo(anuncio.getTitulo());
        response.setContenido(anuncio.getContenido());
        response.setGlobal(anuncio.isGlobal());
        response.setFechaCreacion(anuncio.getFechaCreacion() != null ? anuncio.getFechaCreacion().toString() : null);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        anuncioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
