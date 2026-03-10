package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Anuncio (Announcement) within a course — like Blackboard announcements.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "anuncios")
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "importante")
    private Boolean importante = false;

    @Column(name = "global")
    private Boolean global = false;

    @ManyToMany
    @JoinTable(name = "anuncio_destinatarios", joinColumns = @JoinColumn(name = "anuncio_id"), inverseJoinColumns = @JoinColumn(name = "persona_id"))
    private java.util.Set<Persona> destinatarios;

    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = true)
    private Asignatura asignatura;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Persona autor;

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public java.util.Set<Persona> getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(java.util.Set<Persona> destinatarios) {
        this.destinatarios = destinatarios;
    }
}
