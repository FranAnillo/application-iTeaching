package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ForoTema (Forum Topic) within a course — like Blackboard discussion boards.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "foro_temas")
public class ForoTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fijado")
    private Boolean fijado = false;

    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Persona autor;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tema", orphanRemoval = true)
    @OrderBy("fechaCreacion ASC")
    private List<ForoRespuesta> respuestas = new ArrayList<>();
}
