package iteaching.app.Models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "personas")
public class Persona extends Usuarios {

    @NotBlank
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank
    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "telefono")
    @Digits(fraction = 0, integer = 15)
    private String telefono;

    @Column(name = "email", unique = true)
    @NotBlank
    @Pattern(regexp = "^[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,6}$")
    private String email;

    @Column(name = "puntuacion")
    private Double puntuacion = 0.0;

    @Column(nullable = true, length = 64)
    private String avatar;

    /** Grado universitario al que pertenece (opcional) */
    @ManyToOne
    @JoinColumn(name = "grado_id")
    private Grado grado;

    /** Asignaturas creadas por este usuario (como administrador) */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "creador")
    private Set<Asignatura> asignaturasCreadas = new HashSet<>();

    /** Asignaturas en las que es profesor */
    @JsonIgnore
    @ManyToMany(mappedBy = "profesores")
    private Set<Asignatura> asignaturasComoProfesor = new HashSet<>();

    /** Asignaturas en las que está matriculado (como estudiante) */
    @JsonIgnore
    @ManyToMany(mappedBy = "estudiantes")
    private Set<Asignatura> asignaturasInscritas = new HashSet<>();

    /** Materiales subidos por este usuario */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "autor")
    private Set<Material> materiales = new HashSet<>();

    /** Valoraciones recibidas (como creador de curso) */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "profesor")
    private Set<Valoracion> valoracionesRecibidas = new HashSet<>();

    /** Valoraciones escritas (como alumno) */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "alumno")
    private Set<Valoracion> valoracionesEscritas = new HashSet<>();

    /** Logros obtenidos */
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "persona_logro",
        joinColumns = @JoinColumn(name = "persona_id"),
        inverseJoinColumns = @JoinColumn(name = "logro_id")
    )
    private Set<Logro> logros = new HashSet<>();

    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Transient
    public String getAvatarImagePath() {
        if (avatar == null || getId() == null) return null;
        return "/uploads/avatars/" + avatar;
    }
}
