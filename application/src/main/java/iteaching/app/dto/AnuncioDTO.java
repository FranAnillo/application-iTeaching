package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnuncioDTO {
    private Long id;
    @NotBlank
    @Size(max = 255)
    private String titulo;
    @NotBlank
    @Size(max = 10000)
    private String contenido;
    private String fechaCreacion;
    private Boolean importante;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long autorId;
    private String autorNombre;
    private boolean global;
    private String destinatarios;

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public String getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(String destinatarios) {
        this.destinatarios = destinatarios;
    }
}
