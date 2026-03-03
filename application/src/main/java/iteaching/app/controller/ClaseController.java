package iteaching.app.controller;

import iteaching.app.Models.EstadoClase;
import iteaching.app.dto.ClaseCreateRequest;
import iteaching.app.dto.ClaseDTO;
import iteaching.app.service.ClaseService;
import iteaching.app.service.HorarioPdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clases")
public class ClaseController {

    private final ClaseService claseService;
    private final HorarioPdfService horarioPdfService;
    private final iteaching.app.repository.PersonaRepository personaRepository;

    public ClaseController(ClaseService claseService,
                           HorarioPdfService horarioPdfService,
                           iteaching.app.repository.PersonaRepository personaRepository) {
        this.claseService = claseService;
        this.horarioPdfService = horarioPdfService;
        this.personaRepository = personaRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClaseDTO>> findAll() {
        return ResponseEntity.ok(claseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(claseService.findById(id));
    }

    @GetMapping("/alumno/{username}")
    public ResponseEntity<List<ClaseDTO>> findByAlumno(@PathVariable String username) {
        return ResponseEntity.ok(claseService.findByAlumno(username));
    }

    @GetMapping("/profesor/{username}")
    public ResponseEntity<List<ClaseDTO>> findByProfesor(@PathVariable String username) {
        return ResponseEntity.ok(claseService.findByProfesor(username));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ClaseDTO>> findByEstado(@PathVariable EstadoClase estado) {
        return ResponseEntity.ok(claseService.findByEstado(estado));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PostMapping
    public ResponseEntity<ClaseDTO> create(@Valid @RequestBody ClaseCreateRequest request) {
        return ResponseEntity.ok(claseService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ClaseDTO> updateEstado(@PathVariable Long id, @RequestParam EstadoClase estado) {
        return ResponseEntity.ok(claseService.updateEstado(id, estado));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        claseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Descarga el PDF de horario del estudiante autenticado para una asignatura.
     */
    @GetMapping("/pdf/asignatura/{asignaturaId}")
    public ResponseEntity<byte[]> descargarHorarioPdf(
            @PathVariable Long asignaturaId,
            Authentication authentication) {

        String username = authentication.getName();
        iteaching.app.Models.Persona persona = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        byte[] pdf = horarioPdfService.generarHorarioPdf(asignaturaId, persona.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "horario_" + asignaturaId + "_" + persona.getId() + ".pdf");
        headers.setCacheControl("no-cache");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    /**
     * Descarga el PDF con el horario completo de una asignatura (admin/profesor).
     */
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    @GetMapping("/pdf/asignatura/{asignaturaId}/completo")
    public ResponseEntity<byte[]> descargarHorarioCompletoPdf(
            @PathVariable Long asignaturaId) {

        byte[] pdf = horarioPdfService.generarHorarioCompletoPdf(asignaturaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "horario_completo_" + asignaturaId + ".pdf");
        headers.setCacheControl("no-cache");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
