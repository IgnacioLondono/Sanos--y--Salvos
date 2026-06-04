package com.sanos.reportsservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_contacto")
@Schema(name = "SolicitudContacto", description = "Solicitud de contacto entre usuarios vinculada a un reporte en mapa.")
public class SolicitudContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(name = "id_reporte", nullable = false)
    private Long idReporte;

    @Column(name = "id_usuario_emisor", nullable = false)
    private Long idUsuarioEmisor;

    @Column(name = "id_usuario_receptor", nullable = false)
    private Long idUsuarioReceptor;

    @Column(name = "mensaje", length = 500)
    private String mensaje;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public Long getIdUsuarioEmisor() { return idUsuarioEmisor; }
    public void setIdUsuarioEmisor(Long idUsuarioEmisor) { this.idUsuarioEmisor = idUsuarioEmisor; }
    public Long getIdUsuarioReceptor() { return idUsuarioReceptor; }
    public void setIdUsuarioReceptor(Long idUsuarioReceptor) { this.idUsuarioReceptor = idUsuarioReceptor; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
}
