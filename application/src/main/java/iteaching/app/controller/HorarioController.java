package iteaching.app.controller;

import iteaching.app.dto.HorarioRecurrenteDTO;
import iteaching.app.service.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public List<HorarioRecurrenteDTO> getByAsignatura(@PathVariable Long asignaturaId) {
        return horarioService.findByAsignatura(asignaturaId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public HorarioRecurrenteDTO create(@RequestBody HorarioRecurrenteDTO dto) {
        return horarioService.createAndGenerate(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        horarioService.delete(id);
        return ResponseEntity.ok().build();
    }
}
