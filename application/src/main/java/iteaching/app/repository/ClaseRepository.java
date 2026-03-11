package iteaching.app.repository;

import iteaching.app.Models.Clase;
import iteaching.app.Models.EstadoClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findByAlumnoUsername(String username);
    List<Clase> findByProfesorUsername(String username);
    List<Clase> findByEstadoClase(EstadoClase estadoClase);
    List<Clase> findByAsignaturaId(Long asignaturaId);
    List<Clase> findByAsignaturaIdAndAlumnoId(Long asignaturaId, Long alumnoId);
    List<Clase> findByGrupoId(Long grupoId);
    List<Clase> findByRecurrenteId(Long recurrenteId);
}
