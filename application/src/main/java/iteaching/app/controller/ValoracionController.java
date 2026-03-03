package iteaching.app.controller;

import iteaching.app.dto.ValoracionDTO;
import iteaching.app.service.ValoracionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/profesor/{profesorId}/asignatura/{asignaturaId}")
    public ResponseEntity<List<ValoracionDTO>> findByProfesorAndAsignatura(
            @PathVariable Long profesorId, @PathVariable Long asignaturaId) {
        return ResponseEntity.ok(valoracionService.findByProfesorAndAsignatura(profesorId, asignaturaId));
    }

    @GetMapping("/profesor/{profesorId}/promedio")
    public ResponseEntity<Map<String, Double>> getPromedio(@PathVariable Long profesorId) {
        return ResponseEntity.ok(Map.of("promedio", valoracionService.getPromedioByProfesor(profesorId)));
    }

    /** Solo estudiantes matriculados pueden crear valoraciones (anónimas) */
    @PostMapping
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ValoracionDTO> create(@Valid @RequestBody ValoracionDTO dto, Authentication auth) {
        return ResponseEntity.ok(valoracionService.create(dto, auth.getName()));
    }

    /** Solo administradores pueden eliminar valoraciones */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        valoracionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
