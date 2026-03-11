package iteaching.app.controller;

import iteaching.app.dto.CsvImportResult;
import iteaching.app.dto.UsuarioDTO;
import iteaching.app.service.PersonaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    // Antes: @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    // Ahora: cualquier usuario autenticado puede obtener la lista de usuarios
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok(personaService.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(Authentication auth) {
        return ResponseEntity.ok(personaService.findByUsername(auth.getName()));
    }

    // Más seguro para el chat: cualquier usuario autenticado puede buscar
    // (el servicio ya controla qué datos expone en UsuarioDTO)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<List<UsuarioDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(personaService.search(q));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import-csv")
    public ResponseEntity<CsvImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            CsvImportResult empty = new CsvImportResult(List.of(),
                    List.of("Archivo CSV faltante o vacío"));
            return ResponseEntity.badRequest().body(empty);
        }

        try {
            CsvImportResult result = personaService.importFromCsv(file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // log server-side for debugging
            System.err.println("[PersonaController] error importing CSV: " + e.getMessage());
            CsvImportResult failure = new CsvImportResult(List.of(),
                    List.of("Error al importar CSV: " + e.getMessage()));
            return ResponseEntity.badRequest().body(failure);
        }
    }
}
