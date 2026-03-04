package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogroDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String icono;
    private String categoria;
    private Integer valorObjetivo;
    private boolean obtenido;
}
