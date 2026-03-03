package iteaching.app.controller;

import iteaching.app.dto.EntregaDTO;
import iteaching.app.service.EntregaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<List<EntregaDTO>> findByTarea(@PathVariable Long tareaId) {
        return ResponseEntity.ok(entregaService.findByTarea(tareaId));
    }

    @GetMapping("/mis-entregas")
    public ResponseEntity<List<EntregaDTO>> misEntregas(Authentication auth) {
        return ResponseEntity.ok(entregaService.findByEstudiante(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<EntregaDTO> submit(@RequestBody EntregaDTO dto, Authentication auth) {
        return ResponseEntity.ok(entregaService.submit(dto, auth.getName()));
    }

    /** Instructor grades a submission */
    @PatchMapping("/{id}/calificar")
    public ResponseEntity<EntregaDTO> calificar(
            @PathVariable Long id,
            @RequestParam Double calificacion,
            @RequestParam(required = false) String comentario) {
        return ResponseEntity.ok(entregaService.calificar(id, calificacion, comentario));
    }
}
