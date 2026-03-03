package iteaching.app.dto;

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
    private Double puntuacion;
    private String comentario;
    private Long profesorId;
    private String profesorNombre;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long alumnoId;
    private String alumnoNombre;
}
