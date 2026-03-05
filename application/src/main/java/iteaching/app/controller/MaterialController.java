package iteaching.app.controller;

import iteaching.app.dto.MaterialDTO;
import iteaching.app.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialDTO>> getAll() {
        return ResponseEntity.ok(materialService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.findById(id));
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<MaterialDTO>> getByAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(materialService.findByAutor(autorId));
    }

    @GetMapping("/mis-materiales")
    public ResponseEntity<List<MaterialDTO>> getMisMateriales(Authentication authentication) {
        return ResponseEntity.ok(materialService.findByAutorUsername(authentication.getName()));
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<MaterialDTO>> getByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(materialService.findByAsignatura(asignaturaId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MaterialDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(materialService.search(q));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<MaterialDTO> create(@Valid @RequestBody MaterialDTO dto, Authentication authentication) {
        return ResponseEntity.ok(materialService.create(dto, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PutMapping("/{id}")
    public ResponseEntity<MaterialDTO> update(@PathVariable Long id, @Valid @RequestBody MaterialDTO dto) {
        return ResponseEntity.ok(materialService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
