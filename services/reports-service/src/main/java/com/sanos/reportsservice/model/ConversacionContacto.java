package com.sanos.reportsservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversaciones_contacto")
@Schema(name = "ConversacionContacto", description = "Chat entre dos usuarios tras aceptar solicitud de contacto.")
public class ConversacionContacto {

    public static final String OPEN = "OPEN";
    public static final String CLOSED = "CLOSED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Long idConversacion;

    @Column(name = "id_solicitud", nullable = false, unique = true)
    private Long idSolicitud;

    @Column(name = "id_reporte", nullable = false)
    private Long idReporte;

    @Column(name = "id_usuario_emisor", nullable = false)
    private Long idUsuarioEmisor;

    @Column(name = "id_usuario_receptor", nullable = false)
    private Long idUsuarioReceptor;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "id_usuario_cerro")
    private Long idUsuarioCerro;

    public Long getIdConversacion() { return idConversacion; }
    public void setIdConversacion(Long idConversacion) { this.idConversacion = idConversacion; }
    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public Long getIdUsuarioEmisor() { return idUsuarioEmisor; }
    public void setIdUsuarioEmisor(Long idUsuarioEmisor) { this.idUsuarioEmisor = idUsuarioEmisor; }
    public Long getIdUsuarioReceptor() { return idUsuarioReceptor; }
    public void setIdUsuarioReceptor(Long idUsuarioReceptor) { this.idUsuarioReceptor = idUsuarioReceptor; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public Long getIdUsuarioCerro() { return idUsuarioCerro; }
    public void setIdUsuarioCerro(Long idUsuarioCerro) { this.idUsuarioCerro = idUsuarioCerro; }
}
