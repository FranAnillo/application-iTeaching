package iteaching.app.controller;

import iteaching.app.dto.RubricaDTO;
import iteaching.app.service.RubricaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rubricas")
public class RubricaController {

    private final RubricaService rubricaService;

    public RubricaController(RubricaService rubricaService) {
        this.rubricaService = rubricaService;
    }

    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<RubricaDTO> getByTarea(@PathVariable Long tareaId) {
        RubricaDTO rubrica = rubricaService.getByTarea(tareaId);
        if (rubrica == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rubrica);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RubricaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rubricaService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<RubricaDTO> crear(@Valid @RequestBody RubricaDTO dto) {
        return ResponseEntity.ok(rubricaService.crear(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rubricaService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
