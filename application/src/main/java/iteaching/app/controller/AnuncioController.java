package iteaching.app.controller;

import iteaching.app.dto.AnuncioDTO;
import iteaching.app.service.AnuncioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioController {

    private final AnuncioService anuncioService;

    public AnuncioController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<AnuncioDTO>> findByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(anuncioService.findByAsignatura(asignaturaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnuncioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(anuncioService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<AnuncioDTO> create(@Valid @RequestBody AnuncioDTO dto, Authentication auth) {
        return ResponseEntity.ok(anuncioService.create(dto, auth.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        anuncioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
