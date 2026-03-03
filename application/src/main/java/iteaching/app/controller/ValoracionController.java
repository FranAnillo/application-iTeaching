package iteaching.app.controller;

import iteaching.app.dto.ValoracionDTO;
import iteaching.app.service.ValoracionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/valoraciones")
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    @GetMapping
    public ResponseEntity<List<ValoracionDTO>> findAll() {
        return ResponseEntity.ok(valoracionService.findAll());
    }

    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<ValoracionDTO>> findByProfesor(@PathVariable Long profesorId) {
        return ResponseEntity.ok(valoracionService.findByProfesor(profesorId));
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<ValoracionDTO>> findByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(valoracionService.findByAsignatura(asignaturaId));
    }

    @PostMapping
    public ResponseEntity<ValoracionDTO> create(@Valid @RequestBody ValoracionDTO dto) {
        return ResponseEntity.ok(valoracionService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        valoracionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
