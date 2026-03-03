package iteaching.app.controller;

import iteaching.app.dto.ProfesorDTO;
import iteaching.app.service.ProfesorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profesores")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    @GetMapping
    public ResponseEntity<List<ProfesorDTO>> findAll() {
        return ResponseEntity.ok(profesorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesorDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(profesorService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesorDTO> update(@PathVariable Long id, @RequestBody ProfesorDTO dto) {
        return ResponseEntity.ok(profesorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profesorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
