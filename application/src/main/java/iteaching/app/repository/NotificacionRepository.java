package iteaching.app.repository;

import iteaching.app.Models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long userId);

    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Long userId);

    long countByUsuarioIdAndLeidaFalse(Long userId);
}
