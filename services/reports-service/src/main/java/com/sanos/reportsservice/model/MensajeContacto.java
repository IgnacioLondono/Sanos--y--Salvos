package com.sanos.reportsservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_contacto")
@Schema(name = "MensajeContacto", description = "Mensaje dentro de una conversacion de contacto.")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long idMensaje;

    @Column(name = "id_conversacion", nullable = false)
    private Long idConversacion;

    @Column(name = "id_usuario_autor", nullable = false)
    private Long idUsuarioAutor;

    @Column(name = "contenido", length = 2000, nullable = false)
    private String contenido;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Long getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Long idMensaje) { this.idMensaje = idMensaje; }
    public Long getIdConversacion() { return idConversacion; }
    public void setIdConversacion(Long idConversacion) { this.idConversacion = idConversacion; }
    public Long getIdUsuarioAutor() { return idUsuarioAutor; }
    public void setIdUsuarioAutor(Long idUsuarioAutor) { this.idUsuarioAutor = idUsuarioAutor; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
