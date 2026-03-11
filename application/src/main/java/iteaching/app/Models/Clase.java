package iteaching.app.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo")
    private String titulo; // e.g. "Theory Lecture 1"

    @Column(name = "aula")
    private String aula;

    @Column(name = "hora_comienzo")
    private LocalDateTime horaComienzo;

    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    @Column(name = "aceptacion_alumno", columnDefinition = "boolean default false")
    private Boolean aceptacionAlumno = false;

    @Column(name = "aceptacion_profesor", columnDefinition = "boolean default false")
    private Boolean aceptacionProfesor = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_clase")
    private EstadoClase estadoClase = EstadoClase.SOLICITADA;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Persona alumno; // For 1-on-1 tutoring, null for group classes

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Persona profesor;

    @ManyToOne
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo; // For theory/practice group schedules

    @Column(name = "recurrente_id")
    private Long recurrenteId; // Link back to the rule that created this
}
