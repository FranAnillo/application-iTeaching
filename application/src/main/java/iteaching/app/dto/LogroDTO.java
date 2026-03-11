package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogroDTO {
    private Long id;
    @NotBlank(message = "El código del logro es obligatorio")
    private String codigo;
    @NotBlank(message = "El nombre del logro es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;
    private String icono;
    private String categoria;
    private Integer valorObjetivo;
    private Long asignaturaId;
    private String asignaturaNombre;
    private boolean obtenido;
}
