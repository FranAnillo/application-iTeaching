package iteaching.app.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asistencias", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"estudiante_id", "asignatura_id", "fecha"})
})
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsistencia estado = EstadoAsistencia.PRESENTE;

    @Column(length = 500)
    private String observacion;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Persona estudiante;

    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @ManyToOne
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Persona registradoPor;

    public enum EstadoAsistencia {
        PRESENTE,
        AUSENTE,
        TARDANZA,
        JUSTIFICADO
    }
}
