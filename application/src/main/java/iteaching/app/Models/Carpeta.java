package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Carpeta (Folder) for organizing materials within a course.
 * Supports nesting: a carpeta can have a parent carpeta.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carpetas")
public class Carpeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    /** Parent folder (null = root level) */
    @ManyToOne
    @JoinColumn(name = "padre_id")
    private Carpeta padre;

    /** Sub-folders */
    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Carpeta> subcarpetas = new ArrayList<>();

    /** Materials inside this folder */
    @OneToMany(mappedBy = "carpeta")
    private List<Material> materiales = new ArrayList<>();
}
