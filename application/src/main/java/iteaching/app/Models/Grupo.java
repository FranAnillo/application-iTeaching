package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Grupo (Group) within a course — can be Teoría or Práctica.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "grupos")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGrupo tipo = TipoGrupo.TEORIA;

    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @ManyToMany
    @JoinTable(
        name = "grupo_miembro",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private Set<Persona> miembros = new HashSet<>();

    public enum TipoGrupo {
        TEORIA,
        PRACTICA
    }
}
