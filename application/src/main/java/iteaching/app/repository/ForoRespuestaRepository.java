package iteaching.app.repository;

import iteaching.app.Models.ForoRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForoRespuestaRepository extends JpaRepository<ForoRespuesta, Long> {
    List<ForoRespuesta> findByTemaIdOrderByFechaCreacionAsc(Long temaId);
}
