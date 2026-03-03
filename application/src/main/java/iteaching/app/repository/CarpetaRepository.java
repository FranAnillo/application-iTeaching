package iteaching.app.repository;

import iteaching.app.Models.Carpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarpetaRepository extends JpaRepository<Carpeta, Long> {
    List<Carpeta> findByAsignaturaId(Long asignaturaId);
    List<Carpeta> findByAsignaturaIdAndPadreIsNull(Long asignaturaId);
    List<Carpeta> findByPadreId(Long padreId);
}
