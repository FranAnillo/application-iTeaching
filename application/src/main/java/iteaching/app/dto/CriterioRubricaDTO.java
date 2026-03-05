package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriterioRubricaDTO {
    private Long id;
    @NotBlank
    @Size(max = 255)
    private String nombre;
    @Size(max = 2000)
    private String descripcion;
    private Double puntuacionMaxima;
    private Integer orden;
    @Size(max = 1000)
    private String nivelExcelente;
    @Size(max = 1000)
    private String nivelBueno;
    @Size(max = 1000)
    private String nivelSuficiente;
    @Size(max = 1000)
    private String nivelInsuficiente;
}
