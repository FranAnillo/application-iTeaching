package iteaching.app.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo = TipoNotificacion.INFO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "leida", columnDefinition = "boolean default false")
    private Boolean leida = false;

    @Column(name = "enlace")
    private String enlace;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Persona usuario;

    public enum TipoNotificacion {
        INFO,
        ANUNCIO,
        TAREA,
        CALIFICACION,
        MENSAJE,
        LOGRO
    }
}
