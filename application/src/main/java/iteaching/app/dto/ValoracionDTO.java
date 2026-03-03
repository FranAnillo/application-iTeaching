package iteaching.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValoracionDTO {
    private Long id;

    @NotNull
    @Min(1) @Max(5)
    private Double puntuacion;

    private String comentario;

    /** Puntos de mejora sugeridos */
    private String puntosMejora;

    private String fechaCreacion;

    @NotNull
    private Long profesorId;
    private String profesorNombre;

    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;

    // La valoración es ANÓNIMA: no se exponen datos del alumno
}
