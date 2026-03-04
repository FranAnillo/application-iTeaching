package iteaching.app.repository;

import iteaching.app.Models.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByAsignaturaIdOrderByFechaEntregaAsc(Long asignaturaId);
    List<Tarea> findByAsignaturaId(Long asignaturaId);
}
