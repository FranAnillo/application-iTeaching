package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaseCreateRequest {
    @NotBlank
    private String horaComienzo;
    @NotBlank
    private String horaFin;
    @NotNull
    private Long alumnoId;
    @NotNull
    private Long profesorId;
    @NotNull
    private Long asignaturaId;
}
