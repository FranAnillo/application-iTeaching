package iteaching.app.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "valoraciones")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1) @Max(5)
    @Column(name = "puntuacion")
    private Double puntuacion;

    @Column(name = "comentario", length = 1000)
    private String comentario;

    /** Puntos de mejora sugeridos por el estudiante */
    @Column(name = "puntos_mejora", length = 2000)
    private String puntosMejora;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** El profesor valorado */
    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    private Persona profesor;

    /** La asignatura en que se valora al profesor */
    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    /** El estudiante que escribe la valoración (se guarda internamente para evitar duplicados, pero NO se expone) */
    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Persona alumno;
}
