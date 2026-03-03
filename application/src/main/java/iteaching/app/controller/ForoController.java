package iteaching.app.controller;

import iteaching.app.dto.ForoRespuestaDTO;
import iteaching.app.dto.ForoTemaDTO;
import iteaching.app.service.ForoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foro")
public class ForoController {

    private final ForoService foroService;

    public ForoController(ForoService foroService) {
        this.foroService = foroService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<ForoTemaDTO>> findTemasByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(foroService.findTemasByAsignatura(asignaturaId));
    }

    @GetMapping("/temas/{id}")
    public ResponseEntity<ForoTemaDTO> findTemaById(@PathVariable Long id) {
        return ResponseEntity.ok(foroService.findTemaById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping("/temas")
    public ResponseEntity<ForoTemaDTO> createTema(@Valid @RequestBody ForoTemaDTO dto, Authentication auth) {
        return ResponseEntity.ok(foroService.createTema(dto, auth.getName()));
    }

    @PostMapping("/respuestas")
    public ResponseEntity<ForoRespuestaDTO> createRespuesta(@Valid @RequestBody ForoRespuestaDTO dto, Authentication auth) {
        return ResponseEntity.ok(foroService.createRespuesta(dto, auth.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/temas/{id}")
    public ResponseEntity<Void> deleteTema(@PathVariable Long id) {
        foroService.deleteTema(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/respuestas/{id}")
    public ResponseEntity<Void> deleteRespuesta(@PathVariable Long id) {
        foroService.deleteRespuesta(id);
        return ResponseEntity.noContent().build();
    }
}
