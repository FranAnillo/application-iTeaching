package iteaching.app.controller;

import iteaching.app.dto.AsignaturaDTO;
import iteaching.app.service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    @GetMapping
    public ResponseEntity<List<AsignaturaDTO>> findAll() {
        return ResponseEntity.ok(asignaturaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignaturaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(asignaturaService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AsignaturaDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(asignaturaService.search(q));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AsignaturaDTO> create(@Valid @RequestBody AsignaturaDTO dto) {
        return ResponseEntity.ok(asignaturaService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AsignaturaDTO> update(@PathVariable Long id, @Valid @RequestBody AsignaturaDTO dto) {
        return ResponseEntity.ok(asignaturaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        asignaturaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{asignaturaId}/estudiantes/{estudianteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AsignaturaDTO> addEstudiante(@PathVariable Long asignaturaId, @PathVariable Long estudianteId) {
        return ResponseEntity.ok(asignaturaService.addEstudiante(asignaturaId, estudianteId));
    }

    @DeleteMapping("/{asignaturaId}/estudiantes/{estudianteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AsignaturaDTO> removeEstudiante(@PathVariable Long asignaturaId, @PathVariable Long estudianteId) {
        return ResponseEntity.ok(asignaturaService.removeEstudiante(asignaturaId, estudianteId));
    }
}
