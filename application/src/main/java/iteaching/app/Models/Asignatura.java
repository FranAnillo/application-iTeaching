package iteaching.app.Models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asignaturas")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String siglas;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 255)
    private String aula;

    @Column(name = "url")
    private String url;



    /** Usuario que creó el curso (administrador) */
    @ManyToOne
    @JoinColumn(name = "creador_id")
    private Persona creador;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asignatura")
    private Set<Valoracion> valoraciones = new HashSet<>();

    /** Grupos de la asignatura (Teoría / Práctica) */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asignatura", orphanRemoval = true)
    private List<Grupo> grupos = new ArrayList<>();

    /** Carpetas de materiales */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asignatura", orphanRemoval = true)
    private List<Carpeta> carpetas = new ArrayList<>();

    /** Grado al que pertenece la asignatura */
    @ManyToOne(optional = false)
    @JoinColumn(name = "grado_id", nullable = false)
    private Grado grado;

        /** Profesores de la asignatura */
    @ManyToMany
    @JoinTable(
        name = "asignatura_profesores",
        joinColumns = @JoinColumn(name = "asignatura_id"),
        inverseJoinColumns = @JoinColumn(name = "profesor_id")
    )
    private List<Persona> profesores = new ArrayList<>();

    /** Estudiantes de la asignatura */
    @ManyToMany
    @JoinTable(
        name = "asignatura_estudiantes",
        joinColumns = @JoinColumn(name = "asignatura_id"),
        inverseJoinColumns = @JoinColumn(name = "estudiante_id")
    )
    private List<Persona> estudiantes = new ArrayList<>();

}
