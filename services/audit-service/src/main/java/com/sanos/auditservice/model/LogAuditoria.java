package com.sanos.auditservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_auditoria")
@Schema(name = "LogAuditoria", description = "Tabla **log_auditoria** (db_audit).")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    @Schema(description = "PK id_log", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idLog;

    @Column(name = "id_usuario_responsable")
    @Schema(description = "Usuario responsable opcional", example = "1")
    private Long idUsuarioResponsable;

    @Column(name = "entidad", length = 120)
    @Schema(description = "Entidad dominio")
    private String entidad;

    @Column(name = "operacion", length = 40)
    @Schema(description = "Operacion")
    private String operacion;

    @Column(name = "actor", length = 180)
    @Schema(description = "Actor humano o sistema")
    private String actor;

    @Column(name = "tabla_afectada", length = 120)
    @Schema(description = "Tabla fisica afectada (legacy)")
    private String tablaAfectada;

    @Column(name = "accion_realizada", length = 120)
    @Schema(description = "Accion (legacy)")
    private String accionRealizada;

    @Column(name = "cambios_json", columnDefinition = "TEXT")
    @Schema(description = "Detalle JSON")
    private String cambiosJson;

    @Column(name = "creado_en")
    @Schema(description = "Timestamp")
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
