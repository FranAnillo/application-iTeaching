package iteaching.app.repository;

import iteaching.app.Models.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE (m.remitente.id = :userId OR m.destinatario.id = :userId) ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findByUsuario(@Param("userId") Long userId);

    @Query("SELECT m FROM Mensaje m WHERE " +
           "(m.remitente.id = :userId1 AND m.destinatario.id = :userId2) OR " +
           "(m.remitente.id = :userId2 AND m.destinatario.id = :userId1) " +
           "ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findConversacion(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.destinatario.id = :userId AND m.leido = false")
    long countNoLeidos(@Param("userId") Long userId);

    @Query("SELECT m FROM Mensaje m WHERE m.destinatario.id = :userId AND m.leido = false ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findNoLeidos(@Param("userId") Long userId);
}
