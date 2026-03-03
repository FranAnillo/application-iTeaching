package iteaching.app.controller;

import iteaching.app.dto.UsuarioDTO;
import iteaching.app.service.PersonaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok(personaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(Authentication auth) {
        return ResponseEntity.ok(personaService.findByUsername(auth.getName()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UsuarioDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(personaService.search(q));
    }
}
