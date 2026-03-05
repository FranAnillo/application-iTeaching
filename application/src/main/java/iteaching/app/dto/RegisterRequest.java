package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Size(max = 50)
    private String username;
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
    @NotBlank
    @Size(max = 100)
    private String nombre;
    @NotBlank
    @Size(max = 100)
    private String apellido;
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,6}$")
    private String email;
    @Size(max = 20)
    private String telefono;
}
