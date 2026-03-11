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
    private String titulo;
    private String aula;
    
    @NotBlank
    private String horaComienzo;
    @NotBlank
    private String horaFin;
    
    private Long alumnoId; // Optional for group classes
    @NotNull
    private Long profesorId;
    @NotNull
    private Long asignaturaId;
    
    private Long grupoId; // Optional
}
