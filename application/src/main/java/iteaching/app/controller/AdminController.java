package iteaching.app.controller;

import iteaching.app.Models.AuditLog;
import iteaching.app.Models.EstadoClase;
import iteaching.app.dto.ClaseDTO;
import iteaching.app.service.AuditService;
import iteaching.app.service.ClaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ClaseService claseService;
    private final AuditService auditService;

    public AdminController(ClaseService claseService, AuditService auditService) {
        this.claseService = claseService;
        this.auditService = auditService;
    }

    /** Cancel a fraudulent class – only ROLE_ADMIN (enforced by SecurityConfig URL rule). */
    @PatchMapping("/clases/{id}/cancelar")
    public ResponseEntity<ClaseDTO> cancelarClase(@PathVariable Long id) {
        return ResponseEntity.ok(claseService.updateEstado(id, EstadoClase.CANCELADA));
    }

    /** View recent audit logs – admin only. */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getRecent());
    }

    /** View audit logs for a specific user. */
    @GetMapping("/audit-logs/user/{username}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditService.getByUser(username));
    }
}
