package com.sanos.auditservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "notificaciones_sistema")
@Schema(name = "NotificacionSistema", description = "Tabla **notificaciones_sistema** (db_audit). Mensajes a usuario.")
public class NotificacionSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idNotificacion;
    @Column(name = "id_usuario_destino")
    @Schema(description = "FK usuario destino", example = "1")
    private Long idUsuarioDestino;
    @Column(name = "contenido_mensaje", columnDefinition = "TEXT")
    @Schema(description = "Texto mensaje")
    private String contenidoMensaje;
    @Column(name = "fue_leido")
    @Schema(description = "Leido", example = "false")
    private Boolean fueLeido;

    public Long getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Long idNotificacion) { this.idNotificacion = idNotificacion; }
    public Long getIdUsuarioDestino() { return idUsuarioDestino; }
    public void setIdUsuarioDestino(Long idUsuarioDestino) { this.idUsuarioDestino = idUsuarioDestino; }
    public String getContenidoMensaje() { return contenidoMensaje; }
    public void setContenidoMensaje(String contenidoMensaje) { this.contenidoMensaje = contenidoMensaje; }
    public Boolean getFueLeido() { return fueLeido; }
    public void setFueLeido(Boolean fueLeido) { this.fueLeido = fueLeido; }
}
