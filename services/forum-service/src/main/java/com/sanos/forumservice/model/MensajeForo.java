package com.sanos.forumservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_foro")
@Schema(description = "Mensaje o respuesta en un hilo (tabla mensajes_foro)")
public class MensajeForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    @Schema(description = "PK mensajes_foro.id_mensaje", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idMensaje;

    @Column(name = "id_hilo", nullable = false)
    @Schema(description = "FK hilos_foro.id_hilo", example = "1")
    private Long idHilo;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Texto del mensaje")
    private String contenido;

    @Column(name = "id_usuario")
    @Schema(description = "FK usuario IAM")
    private Long idUsuario;

    @Column(name = "nombre_autor", length = 120)
    @Schema(description = "Nombre visible del autor")
    private String nombreAutor;

    @Column(name = "fecha_creacion")
    @Schema(description = "Fecha de publicacion", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    public Long getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Long idMensaje) { this.idMensaje = idMensaje; }
    public Long getIdHilo() { return idHilo; }
    public void setIdHilo(Long idHilo) { this.idHilo = idHilo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreAutor() { return nombreAutor; }
    public void setNombreAutor(String nombreAutor) { this.nombreAutor = nombreAutor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
