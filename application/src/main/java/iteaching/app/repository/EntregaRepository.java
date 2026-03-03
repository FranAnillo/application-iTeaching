package iteaching.app.repository;

import iteaching.app.Models.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByTareaId(Long tareaId);
    List<Entrega> findByEstudianteUsername(String username);
    Optional<Entrega> findByTareaIdAndEstudianteId(Long tareaId, Long estudianteId);
}
