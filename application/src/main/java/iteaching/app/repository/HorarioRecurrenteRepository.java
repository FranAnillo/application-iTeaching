package iteaching.app.repository;

import iteaching.app.Models.HorarioRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRecurrenteRepository extends JpaRepository<HorarioRecurrente, Long> {
    List<HorarioRecurrente> findByAsignaturaId(Long asignaturaId);
    List<HorarioRecurrente> findByProfesorId(Long profesorId);
    List<HorarioRecurrente> findByAsignaturaIdAndGrupoId(Long asignaturaId, Long grupoId);
}
