package iteaching.app.controller;

import iteaching.app.dto.ProgresoDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.service.ProgresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progreso")
public class ProgresoController {

    private final ProgresoService progresoService;
    private final PersonaRepository personaRepository;

    public ProgresoController(ProgresoService progresoService, PersonaRepository personaRepository) {
        this.progresoService = progresoService;
        this.personaRepository = personaRepository;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<ProgresoDTO> getProgreso(@PathVariable Long asignaturaId, Authentication auth) {
        var persona = personaRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(progresoService.getProgresoEstudiante(persona.getId(), asignaturaId));
    }

    @GetMapping("/global")
    public ResponseEntity<List<ProgresoDTO>> getProgresoGlobal(Authentication auth) {
        return ResponseEntity.ok(progresoService.getProgresoGlobal(auth.getName()));
    }
}
