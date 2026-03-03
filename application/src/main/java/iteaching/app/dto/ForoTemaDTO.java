package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForoTemaDTO {
    private Long id;
    @NotBlank
    private String titulo;
    @NotBlank
    private String contenido;
    private String fechaCreacion;
    private Boolean fijado;
    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long autorId;
    private String autorNombre;
    private int totalRespuestas;
    private List<ForoRespuestaDTO> respuestas;
}
