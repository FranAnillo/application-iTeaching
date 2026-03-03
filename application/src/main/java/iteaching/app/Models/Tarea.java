package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Tarea (Assignment) within a course — like Blackboard assignments.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(length = 4000)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @NotNull
    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Column(name = "puntuacion_maxima")
    private Double puntuacionMaxima = 10.0;

    /** Type: regular assignment, graded evaluation, or practice mock exam. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarea", nullable = false)
    private TipoTarea tipoTarea = TipoTarea.TAREA;

    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private Persona creador;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tarea")
    private Set<Entrega> entregas = new HashSet<>();

    public enum TipoTarea {
        TAREA,       // regular assignment
        EVALUACION,  // graded evaluation
        SIMULACRO    // practice / mock exam (no grade impact)
    }
}
