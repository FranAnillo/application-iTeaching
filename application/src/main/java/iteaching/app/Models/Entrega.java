package iteaching.app.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entrega (Submission) for a Tarea — like Blackboard assignment submissions.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "entregas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tarea_id", "estudiante_id"})
})
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4000)
    private String contenido;

    @Column(name = "url_adjunto")
    private String urlAdjunto;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega = LocalDateTime.now();

    @Column(name = "calificacion")
    private Double calificacion;

    @Column(name = "comentario_profesor", length = 2000)
    private String comentarioProfesor;

    @ManyToOne
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Persona estudiante;
}
