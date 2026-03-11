package iteaching.app.controller;

import iteaching.app.dto.LogroDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.service.LogroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/logros")
public class LogroController {

    private final LogroService logroService;
    private final PersonaRepository personaRepository;

    public LogroController(LogroService logroService, PersonaRepository personaRepository) {
        this.logroService = logroService;
        this.personaRepository = personaRepository;
    }

    @GetMapping
    public ResponseEntity<List<LogroDTO>> getAll(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(logroService.getAllLogros(persona.getId()));
    }

    @GetMapping("/mis-logros")
    public ResponseEntity<List<LogroDTO>> getMisLogros(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(logroService.getLogrosObtenidos(persona.getId()));
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<LogroDTO>> getByAsignatura(Authentication auth, @PathVariable Long asignaturaId) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(logroService.getLogrosByAsignatura(persona.getId(), asignaturaId));
    }

    @GetMapping("/generales")
    public ResponseEntity<List<LogroDTO>> getGenerales(Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(logroService.getLogrosGenerales(persona.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LogroDTO> crear(@Valid @RequestBody LogroDTO dto) {
        return ResponseEntity.ok(logroService.crearLogro(dto));
    }
}
