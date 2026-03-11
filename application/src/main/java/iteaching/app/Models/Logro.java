package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "logros")
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String codigo;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String icono = "🏆";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaLogro categoria = CategoriaLogro.ACADEMICO;

    /** Condition value (e.g., number of submissions needed) */
    @Column(name = "valor_objetivo")
    private Integer valorObjetivo = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;

    @ManyToMany(mappedBy = "logros")
    private Set<Persona> usuarios = new HashSet<>();

    public enum CategoriaLogro {
        ACADEMICO,
        SOCIAL,
        ASISTENCIA,
        ESPECIAL
    }
}
