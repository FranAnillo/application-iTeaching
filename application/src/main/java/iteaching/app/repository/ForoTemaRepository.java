package iteaching.app.repository;

import iteaching.app.Models.ForoTema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForoTemaRepository extends JpaRepository<ForoTema, Long> {
    List<ForoTema> findByAsignaturaIdOrderByFijadoDescFechaCreacionDesc(Long asignaturaId);
}
