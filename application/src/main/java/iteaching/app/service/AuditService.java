package iteaching.app.service;

import iteaching.app.Models.AuditLog;
import iteaching.app.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for recording and querying audit events.
 * Implements traceability requirements per US normativa de seguridad.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logSuccess(String username, String action, String details, String ipAddress) {
        AuditLog entry = new AuditLog(username, action, details, ipAddress, AuditLog.AuditResult.SUCCESS);
        auditLogRepository.save(entry);
        log.info("AUDIT [{}] {} - {} (IP: {})", AuditLog.AuditResult.SUCCESS, username, action, ipAddress);
    }

    public void logFailure(String username, String action, String details, String ipAddress) {
        AuditLog entry = new AuditLog(username, action, details, ipAddress, AuditLog.AuditResult.FAILURE);
        auditLogRepository.save(entry);
        log.warn("AUDIT [{}] {} - {} (IP: {})", AuditLog.AuditResult.FAILURE, username, action, ipAddress);
    }

    public List<AuditLog> getRecent() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<AuditLog> getByUser(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }

    /**
     * Count failed login attempts in the last N minutes.
     * Used for account lockout per US security policy.
     */
    public long countRecentFailedLogins(String username, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return auditLogRepository.countByUsernameAndActionAndResultAndTimestampAfter(
                username, "LOGIN", AuditLog.AuditResult.FAILURE, since);
    }
}
