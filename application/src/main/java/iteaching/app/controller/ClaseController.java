package iteaching.app.controller;

import iteaching.app.Models.EstadoClase;
import iteaching.app.dto.ClaseCreateRequest;
import iteaching.app.dto.ClaseDTO;
import iteaching.app.service.ClaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clases")
public class ClaseController {

    private final ClaseService claseService;

    public ClaseController(ClaseService claseService) {
        this.claseService = claseService;
    }

    @GetMapping
    public ResponseEntity<List<ClaseDTO>> findAll() {
        return ResponseEntity.ok(claseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(claseService.findById(id));
    }

    @GetMapping("/alumno/{username}")
    public ResponseEntity<List<ClaseDTO>> findByAlumno(@PathVariable String username) {
        return ResponseEntity.ok(claseService.findByAlumno(username));
    }

    @GetMapping("/profesor/{username}")
    public ResponseEntity<List<ClaseDTO>> findByProfesor(@PathVariable String username) {
        return ResponseEntity.ok(claseService.findByProfesor(username));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ClaseDTO>> findByEstado(@PathVariable EstadoClase estado) {
        return ResponseEntity.ok(claseService.findByEstado(estado));
    }

    @PostMapping
    public ResponseEntity<ClaseDTO> create(@Valid @RequestBody ClaseCreateRequest request) {
        return ResponseEntity.ok(claseService.create(request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ClaseDTO> updateEstado(@PathVariable Long id, @RequestParam EstadoClase estado) {
        return ResponseEntity.ok(claseService.updateEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        claseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
