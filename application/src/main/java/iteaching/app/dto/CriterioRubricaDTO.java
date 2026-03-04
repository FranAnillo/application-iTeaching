package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriterioRubricaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double puntuacionMaxima;
    private Integer orden;
    private String nivelExcelente;
    private String nivelBueno;
    private String nivelSuficiente;
    private String nivelInsuficiente;
}
