package iteaching.app.repository;

import iteaching.app.Models.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GradoRepository extends JpaRepository<Grado, Long> {
    Optional<Grado> findByNombreIgnoreCase(String nombre);
}
