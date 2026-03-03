package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Carpeta;
import iteaching.app.Models.Material;
import iteaching.app.Models.Persona;
import iteaching.app.dto.MaterialDTO;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.CarpetaRepository;
import iteaching.app.repository.MaterialRepository;
import iteaching.app.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final PersonaRepository personaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final CarpetaRepository carpetaRepository;

    public MaterialService(MaterialRepository materialRepository,
                           PersonaRepository personaRepository,
                           AsignaturaRepository asignaturaRepository,
                           CarpetaRepository carpetaRepository) {
        this.materialRepository = materialRepository;
        this.personaRepository = personaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.carpetaRepository = carpetaRepository;
    }

    public List<MaterialDTO> findAll() {
        return materialRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MaterialDTO findById(Long id) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado con id: " + id));
        return toDTO(m);
    }

    public List<MaterialDTO> findByAutor(Long autorId) {
        return materialRepository.findByAutorId(autorId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MaterialDTO> findByAutorUsername(String username) {
        return materialRepository.findByAutorUsername(username).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MaterialDTO> findByAsignatura(Long asignaturaId) {
        return materialRepository.findByAsignaturaId(asignaturaId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MaterialDTO> search(String query) {
        return materialRepository.findByTituloContainingIgnoreCase(query).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public MaterialDTO create(MaterialDTO dto, String username) {
        Persona autor = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Material material = new Material();
        material.setTitulo(dto.getTitulo());
        material.setDescripcion(dto.getDescripcion());
        material.setUrlRecurso(dto.getUrlRecurso());
        material.setTipo(dto.getTipo() != null
                ? Material.TipoMaterial.valueOf(dto.getTipo())
                : Material.TipoMaterial.DOCUMENTO);
        material.setFechaCreacion(LocalDateTime.now());
        material.setAutor(autor);

        if (dto.getAsignaturaId() != null) {
            Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                    .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
            material.setAsignatura(asignatura);
        }

        if (dto.getCarpetaId() != null) {
            Carpeta carpeta = carpetaRepository.findById(dto.getCarpetaId())
                    .orElseThrow(() -> new RuntimeException("Carpeta no encontrada"));
            material.setCarpeta(carpeta);
        }

        return toDTO(materialRepository.save(material));
    }

    @Transactional
    public MaterialDTO update(Long id, MaterialDTO dto) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado con id: " + id));

        material.setTitulo(dto.getTitulo());
        material.setDescripcion(dto.getDescripcion());
        material.setUrlRecurso(dto.getUrlRecurso());
        if (dto.getTipo() != null) {
            material.setTipo(Material.TipoMaterial.valueOf(dto.getTipo()));
        }

        if (dto.getAsignaturaId() != null) {
            Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                    .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
            material.setAsignatura(asignatura);
        } else {
            material.setAsignatura(null);
        }

        if (dto.getCarpetaId() != null) {
            Carpeta carpeta = carpetaRepository.findById(dto.getCarpetaId())
                    .orElseThrow(() -> new RuntimeException("Carpeta no encontrada"));
            material.setCarpeta(carpeta);
        } else {
            material.setCarpeta(null);
        }

        return toDTO(materialRepository.save(material));
    }

    @Transactional
    public void delete(Long id) {
        if (!materialRepository.existsById(id))
            throw new RuntimeException("Material no encontrado con id: " + id);
        materialRepository.deleteById(id);
    }

    private MaterialDTO toDTO(Material m) {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(m.getId());
        dto.setTitulo(m.getTitulo());
        dto.setDescripcion(m.getDescripcion());
        dto.setUrlRecurso(m.getUrlRecurso());
        dto.setTipo(m.getTipo().name());
        dto.setFechaCreacion(m.getFechaCreacion().toString());
        dto.setAutorId(m.getAutor() != null ? m.getAutor().getId() : null);
        dto.setAutorNombre(m.getAutor() != null
                ? m.getAutor().getNombre() + " " + m.getAutor().getApellido()
                : null);
        dto.setAsignaturaId(m.getAsignatura() != null ? m.getAsignatura().getId() : null);
        dto.setAsignaturaNombre(m.getAsignatura() != null ? m.getAsignatura().getNombre() : null);
        dto.setCarpetaId(m.getCarpeta() != null ? m.getCarpeta().getId() : null);
        dto.setCarpetaNombre(m.getCarpeta() != null ? m.getCarpeta().getNombre() : null);
        return dto;
    }
}
