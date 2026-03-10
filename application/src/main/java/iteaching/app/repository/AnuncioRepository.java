package iteaching.app.repository;

import iteaching.app.Models.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    List<Anuncio> findByAsignaturaIdOrderByFechaCreacionDesc(Long asignaturaId);

    List<Anuncio> findByAsignaturaIdOrGlobalTrueOrderByFechaCreacionDesc(Long asignaturaId);

    List<Anuncio> findByGlobalTrueOrderByFechaCreacionDesc();
}
