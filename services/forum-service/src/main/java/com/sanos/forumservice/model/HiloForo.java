package com.sanos.forumservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hilos_foro")
@Schema(description = "Hilo del foro comunitario (tabla hilos_foro)")
public class HiloForo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hilo")
    @Schema(description = "PK hilos_foro.id_hilo", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idHilo;

    @Column(nullable = false, length = 200)
    @Schema(description = "Titulo del hilo", example = "Perdi a mi perro en Providencia")
    private String titulo;

    @Column(nullable = false, length = 32)
    @Schema(description = "Categoria", example = "PERDIDAS")
    private String categoria;

    @Column(name = "id_usuario")
    @Schema(description = "FK usuario IAM", example = "2")
    private Long idUsuario;

    @Column(name = "nombre_autor", length = 120)
    @Schema(description = "Nombre visible del autor")
    private String nombreAutor;

    @Column(name = "fecha_creacion")
    @Schema(description = "Fecha de creacion", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @Schema(description = "Ultima actividad", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) fechaCreacion = now;
        if (fechaActualizacion == null) fechaActualizacion = now;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getIdHilo() { return idHilo; }
    public void setIdHilo(Long idHilo) { this.idHilo = idHilo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreAutor() { return nombreAutor; }
    public void setNombreAutor(String nombreAutor) { this.nombreAutor = nombreAutor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
