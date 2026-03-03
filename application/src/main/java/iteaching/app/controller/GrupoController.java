package iteaching.app.controller;

import iteaching.app.dto.GrupoDTO;
import iteaching.app.service.GrupoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<GrupoDTO>> findByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(grupoService.findByAsignatura(asignaturaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<GrupoDTO> create(@Valid @RequestBody GrupoDTO dto) {
        return ResponseEntity.ok(grupoService.create(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PutMapping("/{id}")
    public ResponseEntity<GrupoDTO> update(@PathVariable Long id, @Valid @RequestBody GrupoDTO dto) {
        return ResponseEntity.ok(grupoService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        grupoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping("/{grupoId}/miembros/{personaId}")
    public ResponseEntity<GrupoDTO> addMiembro(@PathVariable Long grupoId, @PathVariable Long personaId) {
        return ResponseEntity.ok(grupoService.addMiembro(grupoId, personaId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{grupoId}/miembros/{personaId}")
    public ResponseEntity<GrupoDTO> removeMiembro(@PathVariable Long grupoId, @PathVariable Long personaId) {
        return ResponseEntity.ok(grupoService.removeMiembro(grupoId, personaId));
    }

    /** Professor / admin toggle inscribible flag */
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PatchMapping("/{grupoId}/inscribible")
    public ResponseEntity<GrupoDTO> toggleInscribible(@PathVariable Long grupoId) {
        return ResponseEntity.ok(grupoService.toggleInscribible(grupoId));
    }

    /** Student self-enrol */
    @PostMapping("/{grupoId}/inscribirse")
    public ResponseEntity<GrupoDTO> selfEnrol(@PathVariable Long grupoId, Authentication auth) {
        return ResponseEntity.ok(grupoService.selfEnrol(grupoId, auth.getName()));
    }

    /** Student self-unenrol */
    @DeleteMapping("/{grupoId}/desinscribirse")
    public ResponseEntity<GrupoDTO> selfUnenrol(@PathVariable Long grupoId, Authentication auth) {
        return ResponseEntity.ok(grupoService.selfUnenrol(grupoId, auth.getName()));
    }
}
