package iteaching.app.repository;

import iteaching.app.Models.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    Optional<Profesor> findByUsername(String username);
    List<Profesor> findByNombreContainingIgnoreCase(String nombre);
}
