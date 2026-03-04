package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rubricas")
public class Rubrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @OneToOne
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "rubrica", orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<CriterioRubrica> criterios = new ArrayList<>();
}
