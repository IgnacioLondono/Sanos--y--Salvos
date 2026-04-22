package com.sanos.auditservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "notificaciones_sistema")
public class NotificacionSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion") private Long idNotificacion;
    @Column(name = "id_usuario_destino") private Long idUsuarioDestino;
    @Column(name = "contenido_mensaje", columnDefinition = "TEXT") private String contenidoMensaje;
    @Column(name = "fue_leido") private Boolean fueLeido;

    public Long getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Long idNotificacion) { this.idNotificacion = idNotificacion; }
    public Long getIdUsuarioDestino() { return idUsuarioDestino; }
    public void setIdUsuarioDestino(Long idUsuarioDestino) { this.idUsuarioDestino = idUsuarioDestino; }
    public String getContenidoMensaje() { return contenidoMensaje; }
    public void setContenidoMensaje(String contenidoMensaje) { this.contenidoMensaje = contenidoMensaje; }
    public Boolean getFueLeido() { return fueLeido; }
    public void setFueLeido(Boolean fueLeido) { this.fueLeido = fueLeido; }
}
