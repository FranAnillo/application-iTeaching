package iteaching.app.repository;

import iteaching.app.Models.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {
    List<Asignatura> findByGradoId(Long gradoId);
    List<Asignatura> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Verifica que la asignatura existe Y que el profesor (persona) pertenece a
     * ella
     */
    boolean existsByIdAndProfesoresId(Long asignaturaId, Long profesorId);
}
