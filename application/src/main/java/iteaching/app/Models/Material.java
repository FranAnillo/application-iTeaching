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
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "materiales")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descripcion;

    /** URL or file path for the material */
    @Column(name = "url_recurso")
    private String urlRecurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMaterial tipo = TipoMaterial.DOCUMENTO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** El usuario que subió el material */
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Persona autor;

    /** Optional: link to a subject */
    @ManyToOne
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;

    /** Optional: link to a folder */
    @ManyToOne
    @JoinColumn(name = "carpeta_id")
    private Carpeta carpeta;

    public enum TipoMaterial {
        DOCUMENTO,
        VIDEO,
        ENLACE,
        PRESENTACION,
        EJERCICIO,
        OTRO
    }
}
