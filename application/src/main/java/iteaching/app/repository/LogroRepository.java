package iteaching.app.repository;

import iteaching.app.Models.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Long> {

    Optional<Logro> findByCodigo(String codigo);
    java.util.List<Logro> findByAsignaturaId(Long asignaturaId);
    java.util.List<Logro> findByAsignaturaIdIsNull();
}
