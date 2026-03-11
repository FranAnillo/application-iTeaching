package iteaching.app.controller;

import iteaching.app.dto.GradoDTO;
import iteaching.app.service.GradoService;
import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Grado;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grados")
public class GradoController {

    private final GradoService gradoService;

    public GradoController(GradoService gradoService) {
        this.gradoService = gradoService;
    }

    @GetMapping
    public ResponseEntity<List<GradoDTO>> list() {
        return ResponseEntity.ok(gradoService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GradoDTO> create(@RequestBody GradoDTO dto) {
        return ResponseEntity.ok(gradoService.save(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/asignaturas/{aid}")
    public ResponseEntity<GradoDTO> addAsignatura(@PathVariable Long id, @PathVariable("aid") Long asignaturaId) {
        return ResponseEntity.ok(gradoService.addAsignatura(id, asignaturaId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/asignaturas/{aid}")
    public ResponseEntity<GradoDTO> removeAsignatura(@PathVariable Long id,
                                                     @PathVariable("aid") Long asignaturaId) {
        return ResponseEntity.ok(gradoService.removeAsignatura(id, asignaturaId));
    }

    @GetMapping("/{id}/asignaturas")
    public ResponseEntity<java.util.List<Asignatura>> listAsignaturas(@PathVariable Long id) {
        // convert set to list to evitar problemas de serialización
        return ResponseEntity.ok(new java.util.ArrayList<>(gradoService.getAsignaturas(id)));
    }
}
