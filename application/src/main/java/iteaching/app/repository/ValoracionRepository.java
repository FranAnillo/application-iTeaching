package iteaching.app.repository;

import iteaching.app.Models.Estudiante;
import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Profesor;
import iteaching.app.Models.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    Optional<Valoracion> findByAlumnoAndAsignatura(Estudiante alumno, Asignatura asignatura);
    List<Valoracion> findByAsignatura(Asignatura asignatura);
    List<Valoracion> findByProfesor(Profesor profesor);
    List<Valoracion> findByAlumno(Estudiante alumno);
}
