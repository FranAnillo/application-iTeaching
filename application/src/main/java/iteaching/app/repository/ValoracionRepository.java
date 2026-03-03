package iteaching.app.repository;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    Optional<Valoracion> findByAlumnoAndAsignatura(Persona alumno, Asignatura asignatura);
    Optional<Valoracion> findByAlumnoAndProfesorAndAsignatura(Persona alumno, Persona profesor, Asignatura asignatura);
    List<Valoracion> findByAsignatura(Asignatura asignatura);
    List<Valoracion> findByProfesor(Persona profesor);
    List<Valoracion> findByAlumno(Persona alumno);
    List<Valoracion> findByProfesorAndAsignatura(Persona profesor, Asignatura asignatura);

    @Query("SELECT AVG(v.puntuacion) FROM Valoracion v WHERE v.profesor = :profesor")
    Double getPromedioByProfesor(Persona profesor);
}
