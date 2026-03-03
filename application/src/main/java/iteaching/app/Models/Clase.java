package iteaching.app.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hora_comienzo")
    private String horaComienzo;

    @Column(name = "hora_fin")
    private String horaFin;

    @Column(name = "aceptacion_alumno", columnDefinition = "boolean default false")
    private Boolean aceptacionAlumno = false;

    @Column(name = "aceptacion_profesor", columnDefinition = "boolean default false")
    private Boolean aceptacionProfesor = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_clase")
    private EstadoClase estadoClase = EstadoClase.SOLICITADA;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Estudiante alumno;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @ManyToOne
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;
}
