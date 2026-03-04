package iteaching.app.repository;

import iteaching.app.Models.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByAsignaturaIdOrderByFechaDesc(Long asignaturaId);

    List<Asistencia> findByAsignaturaIdAndFecha(Long asignaturaId, LocalDate fecha);

    List<Asistencia> findByEstudianteIdAndAsignaturaIdOrderByFechaDesc(Long estudianteId, Long asignaturaId);

    Optional<Asistencia> findByEstudianteIdAndAsignaturaIdAndFecha(Long estudianteId, Long asignaturaId, LocalDate fecha);

    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.estudiante.id = :estudianteId AND a.asignatura.id = :asignaturaId AND a.estado = 'PRESENTE'")
    long countPresentes(@Param("estudianteId") Long estudianteId, @Param("asignaturaId") Long asignaturaId);

    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.estudiante.id = :estudianteId AND a.asignatura.id = :asignaturaId")
    long countTotal(@Param("estudianteId") Long estudianteId, @Param("asignaturaId") Long asignaturaId);
}
