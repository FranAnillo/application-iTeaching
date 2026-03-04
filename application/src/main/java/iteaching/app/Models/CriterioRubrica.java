package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "criterios_rubrica")
public class CriterioRubrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "puntuacion_maxima", nullable = false)
    private Double puntuacionMaxima = 10.0;

    @Column(name = "orden")
    private Integer orden = 0;

    /** Descripción del nivel excelente */
    @Column(name = "nivel_excelente", length = 500)
    private String nivelExcelente;

    /** Descripción del nivel bueno */
    @Column(name = "nivel_bueno", length = 500)
    private String nivelBueno;

    /** Descripción del nivel suficiente */
    @Column(name = "nivel_suficiente", length = 500)
    private String nivelSuficiente;

    /** Descripción del nivel insuficiente */
    @Column(name = "nivel_insuficiente", length = 500)
    private String nivelInsuficiente;

    @ManyToOne
    @JoinColumn(name = "rubrica_id", nullable = false)
    private Rubrica rubrica;
}
