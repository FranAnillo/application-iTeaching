package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionDTO {
    private Long id;
    private String titulo;
    private String mensaje;
    private String tipo;
    private String fechaCreacion;
    private Boolean leida;
    private String enlace;
    private Long usuarioId;
}
