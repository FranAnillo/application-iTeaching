package iteaching.app.repository;

import iteaching.app.Models.Rubrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RubricaRepository extends JpaRepository<Rubrica, Long> {

    Optional<Rubrica> findByTareaId(Long tareaId);
}
