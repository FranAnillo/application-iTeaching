package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ForoRespuesta (Forum Reply) within a topic — like Blackboard discussion responses.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "foro_respuestas")
public class ForoRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 4000)
    private String contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "tema_id", nullable = false)
    private ForoTema tema;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Persona autor;
}
