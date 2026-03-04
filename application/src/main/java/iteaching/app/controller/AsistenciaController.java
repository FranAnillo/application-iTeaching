package iteaching.app.controller;

import iteaching.app.dto.AsistenciaDTO;
import iteaching.app.service.AsistenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<AsistenciaDTO>> getByAsignatura(@PathVariable Long asignaturaId) {
        return ResponseEntity.ok(asistenciaService.getByAsignatura(asignaturaId));
    }

    @GetMapping("/asignatura/{asignaturaId}/fecha/{fecha}")
    public ResponseEntity<List<AsistenciaDTO>> getByFecha(
            @PathVariable Long asignaturaId, @PathVariable String fecha) {
        return ResponseEntity.ok(asistenciaService.getByAsignaturaAndFecha(asignaturaId, LocalDate.parse(fecha)));
    }

    @GetMapping("/estudiante/{estudianteId}/asignatura/{asignaturaId}")
    public ResponseEntity<List<AsistenciaDTO>> getByEstudiante(
            @PathVariable Long estudianteId, @PathVariable Long asignaturaId) {
        return ResponseEntity.ok(asistenciaService.getByEstudiante(estudianteId, asignaturaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<AsistenciaDTO> registrar(@RequestBody AsistenciaDTO dto, Authentication auth) {
        return ResponseEntity.ok(asistenciaService.registrar(dto, auth.getName()));
    }

    @PostMapping("/lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<Void> registrarLote(@RequestBody List<AsistenciaDTO> dtos, Authentication auth) {
        asistenciaService.registrarLote(dtos, auth.getName());
        return ResponseEntity.ok().build();
    }
}
