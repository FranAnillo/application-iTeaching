package iteaching.app.controller;

import iteaching.app.dto.CarpetaDTO;
import iteaching.app.service.CarpetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carpetas")
public class CarpetaController {

    private final CarpetaService carpetaService;

    public CarpetaController(CarpetaService carpetaService) {
        this.carpetaService = carpetaService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<CarpetaDTO>> findByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(carpetaService.findByAsignatura(asignaturaId));
    }

    @GetMapping("/asignatura/{asignaturaId}/root")
    public ResponseEntity<List<CarpetaDTO>> findRootByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(carpetaService.findRootByAsignatura(asignaturaId));
    }

    @GetMapping("/{id}/subcarpetas")
    public ResponseEntity<List<CarpetaDTO>> findSubcarpetas(@PathVariable Long id) {
        return ResponseEntity.ok(carpetaService.findSubcarpetas(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarpetaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carpetaService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<CarpetaDTO> create(@Valid @RequestBody CarpetaDTO dto) {
        return ResponseEntity.ok(carpetaService.create(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PutMapping("/{id}")
    public ResponseEntity<CarpetaDTO> update(@PathVariable Long id, @Valid @RequestBody CarpetaDTO dto) {
        return ResponseEntity.ok(carpetaService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carpetaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
