package iteaching.app.service;

import iteaching.app.Models.Anuncio;
import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AnuncioDTO;
import iteaching.app.repository.AnuncioRepository;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;
    private final NotificationService notificationService;

    public AnuncioService(AnuncioRepository anuncioRepository,
            AsignaturaRepository asignaturaRepository,
            PersonaRepository personaRepository,
            NotificationService notificationService) {
        this.anuncioRepository = anuncioRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AnuncioDTO> findByAsignatura(Long asignaturaId) {
        return anuncioRepository.findByAsignaturaIdOrGlobalTrueOrderByFechaCreacionDesc(asignaturaId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnuncioDTO> findGlobal() {
        return anuncioRepository.findByGlobalTrueOrderByFechaCreacionDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnuncioDTO findById(Long id) {
        return toDTO(anuncioRepository.findById(id != null ? id : 0L)
                .orElseThrow(() -> new RuntimeException("Anuncio no encontrado")));
    }

    /**
     * Crea un anuncio aplicando las reglas por rol:
     * <ul>
     * <li><b>ADMIN</b>: puede crear un anuncio global (sin asignatura) o asociado a
     * cualquier asignatura existente.</li>
     * <li><b>PROFESOR</b>: la asignatura es obligatoria y debe ser una en la que el
     * profesor esté asignado.</li>
     * </ul>
     */
    @Transactional
    public AnuncioDTO create(AnuncioDTO dto, String username, Usuarios.Role role) {
        Persona autor = personaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Anuncio a = new Anuncio();
        a.setTitulo(InputSanitizer.sanitize(dto.getTitulo()));
        a.setContenido(InputSanitizer.sanitize(dto.getContenido()));
        a.setImportante(dto.getImportante() != null ? dto.getImportante() : false);
        a.setFechaCreacion(LocalDateTime.now());
        a.setAutor(autor);

        if (role == Usuarios.Role.ROLE_ADMIN) {
            // ADMIN: asignatura opcional; si viene, se valida que exista
            if (dto.getAsignaturaId() != null) {
                Long asigId = dto.getAsignaturaId();
                Asignatura asignatura = asignaturaRepository.findById(asigId)
                        .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + asigId));
                a.setAsignatura(asignatura);
                a.setGlobal(false);
            } else {
                // Sin asignatura → anuncio global de plataforma
                a.setAsignatura(null);
                a.setGlobal(true);
            }
        } else {
            // PROFESOR: la asignatura es obligatoria y debe estar asignada al profesor
            if (dto.getAsignaturaId() == null) {
                throw new IllegalArgumentException(
                        "Los profesores deben indicar la asignatura a la que pertenece el anuncio");
            }
            Long asigId = dto.getAsignaturaId();
            if (!asignaturaRepository.existsByIdAndProfesoresId(asigId, autor.getId())) {
                throw new SecurityException(
                        "No tienes permiso para publicar anuncios en esta asignatura");
            }
            Asignatura asignatura = asignaturaRepository.findById(asigId)
                    .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + asigId));
            a.setAsignatura(asignatura);
            a.setGlobal(false);
        }

        anuncioRepository.save(a);
        notificationService.sendAnnouncement(a);
        return toDTO(a);
    }

    @Transactional
    public void delete(Long id) {
        if (!anuncioRepository.existsById(id != null ? id : 0L))
            throw new RuntimeException("Anuncio no encontrado");
        anuncioRepository.deleteById(id != null ? id : 0L);
    }

    public Anuncio crearAnuncioGlobalOGrupo(AnuncioDTO dto) {
        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo(dto.getTitulo());
        anuncio.setContenido(dto.getContenido());
        anuncio.setGlobal(dto.isGlobal());
        anuncio.setFechaCreacion(LocalDateTime.now());
        if (!dto.isGlobal() && dto.getDestinatarios() != null) {
            // Find personas by subgroup and set destinatarios
            Set<Persona> destinatarios = personaRepository.findByRole(dto.getDestinatarios());
            anuncio.setDestinatarios(destinatarios);
        } else {
            anuncio.setDestinatarios(null); // or empty set for global
        }
        anuncioRepository.save(anuncio);
        // Send notification emails
        notificationService.sendAnnouncement(anuncio);
        return anuncio;
    }

    private AnuncioDTO toDTO(Anuncio a) {
        AnuncioDTO dto = new AnuncioDTO();
        dto.setId(a.getId());
        dto.setTitulo(a.getTitulo());
        dto.setContenido(a.getContenido());
        dto.setFechaCreacion(a.getFechaCreacion() != null ? a.getFechaCreacion().toString() : null);
        dto.setImportante(a.getImportante());
        dto.setGlobal(a.isGlobal());
        if (a.getAsignatura() != null) {
            dto.setAsignaturaId(a.getAsignatura().getId());
            dto.setAsignaturaNombre(a.getAsignatura().getNombre());
        }
        if (a.getAutor() != null) {
            dto.setAutorId(a.getAutor().getId());
            dto.setAutorNombre(a.getAutor().getNombreCompleto());
        }
        return dto;
    }
}
