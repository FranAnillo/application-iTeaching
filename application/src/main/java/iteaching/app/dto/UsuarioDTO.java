package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String role;
    private Double puntuacion;
    private String avatar;

    // grado universitario (si aplica)
    private Long gradoId;
    private String gradoNombre;
}
