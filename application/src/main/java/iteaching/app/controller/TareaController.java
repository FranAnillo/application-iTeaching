package iteaching.app.controller;

import iteaching.app.dto.TareaDTO;
import iteaching.app.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<TareaDTO>> findByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(tareaService.findByAsignatura(asignaturaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<TareaDTO> create(@Valid @RequestBody TareaDTO dto, Authentication auth) {
        return ResponseEntity.ok(tareaService.create(dto, auth.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tareaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
