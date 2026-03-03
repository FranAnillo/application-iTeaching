package iteaching.app.repository;

import iteaching.app.Models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);
    List<AuditLog> findTop100ByOrderByTimestampDesc();
    long countByUsernameAndActionAndResultAndTimestampAfter(
            String username, String action, AuditLog.AuditResult result, LocalDateTime after);
}
