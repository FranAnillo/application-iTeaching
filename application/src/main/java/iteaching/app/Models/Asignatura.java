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
import jakarta.validation.constraints.NotNull;

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
@Table(name = "asignaturas")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(name = "titulo_anuncio")
    private String tituloAnuncio;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "url")
    private String url;

    @NotNull
    @Column(name = "precio")
    private Double precio = 0.0;

    @ManyToMany
    @JoinTable(
        name = "asignatura_estudiante",
        joinColumns = @JoinColumn(name = "asignatura_id"),
        inverseJoinColumns = @JoinColumn(name = "estudiante_id")
    )
    private Set<Estudiante> estudiantes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "asignatura")
    private Set<Valoracion> valoraciones = new HashSet<>();
}
