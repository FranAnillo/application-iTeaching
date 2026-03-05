package iteaching.app.controller;

import iteaching.app.dto.AsignaturaDTO;
import iteaching.app.service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // ===== Admin-only: CRUD =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AsignaturaDTO> create(@Valid @RequestBody AsignaturaDTO dto, Authentication auth) {
        return ResponseEntity.ok(asignaturaService.create(dto, auth.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AsignaturaDTO> update(@PathVariable Long id, @Valid @RequestBody AsignaturaDTO dto) {
        return ResponseEntity.ok(asignaturaService.update(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        asignaturaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Admin-only: CSV Import =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import-csv")
    public ResponseEntity<List<AsignaturaDTO>> importCsv(
            @RequestParam("file") MultipartFile file, Authentication auth) {
        try {
            List<AsignaturaDTO> imported = asignaturaService.importFromCsv(file.getInputStream(), auth.getName());
            return ResponseEntity.ok(imported);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al importar CSV");
        }
    }

    // ===== Admin-only: Assign/remove profesores =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{asignaturaId}/profesores/{personaId}")
    public ResponseEntity<AsignaturaDTO> addProfesor(
            @PathVariable Long asignaturaId, @PathVariable Long personaId) {
        return ResponseEntity.ok(asignaturaService.addProfesor(asignaturaId, personaId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{asignaturaId}/profesores/{personaId}")
    public ResponseEntity<AsignaturaDTO> removeProfesor(
            @PathVariable Long asignaturaId, @PathVariable Long personaId) {
        return ResponseEntity.ok(asignaturaService.removeProfesor(asignaturaId, personaId));
    }

    // ===== Admin-only: Assign/remove estudiantes =====

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{asignaturaId}/estudiantes/{personaId}")
    public ResponseEntity<AsignaturaDTO> addEstudiante(
            @PathVariable Long asignaturaId, @PathVariable Long personaId) {
        return ResponseEntity.ok(asignaturaService.addEstudiante(asignaturaId, personaId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{asignaturaId}/estudiantes/{personaId}")
    public ResponseEntity<AsignaturaDTO> removeEstudiante(
            @PathVariable Long asignaturaId, @PathVariable Long personaId) {
        return ResponseEntity.ok(asignaturaService.removeEstudiante(asignaturaId, personaId));
    }

    // ===== Self-enrollment =====

    @PostMapping("/{asignaturaId}/inscribirse")
    public ResponseEntity<AsignaturaDTO> inscribirse(@PathVariable Long asignaturaId, Authentication auth) {
        return ResponseEntity.ok(asignaturaService.inscribirse(asignaturaId, auth.getName()));
    }

    @DeleteMapping("/{asignaturaId}/desinscribirse")
    public ResponseEntity<AsignaturaDTO> desinscribirse(@PathVariable Long asignaturaId, Authentication auth) {
        return ResponseEntity.ok(asignaturaService.desinscribirse(asignaturaId, auth.getName()));
    }
}
