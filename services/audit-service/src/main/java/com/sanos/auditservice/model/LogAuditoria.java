package com.sanos.auditservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "id_usuario_responsable")
    private Long idUsuarioResponsable;

    @Column(name = "entidad", length = 120)
    private String entidad;

    @Column(name = "operacion", length = 40)
    private String operacion;

    @Column(name = "actor", length = 180)
    private String actor;

    @Column(name = "tabla_afectada", length = 120)
    private String tablaAfectada;

    @Column(name = "accion_realizada", length = 120)
    private String accionRealizada;

    @Column(name = "cambios_json", columnDefinition = "TEXT")
    private String cambiosJson;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }
    public Long getIdUsuarioResponsable() { return idUsuarioResponsable; }
    public void setIdUsuarioResponsable(Long idUsuarioResponsable) { this.idUsuarioResponsable = idUsuarioResponsable; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }
    public String getAccionRealizada() { return accionRealizada; }
    public void setAccionRealizada(String accionRealizada) { this.accionRealizada = accionRealizada; }
    public String getCambiosJson() { return cambiosJson; }
    public void setCambiosJson(String cambiosJson) { this.cambiosJson = cambiosJson; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
