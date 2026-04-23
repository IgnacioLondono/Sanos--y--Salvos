package com.sanos.reportsservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_eventos")
@Schema(name = "ReporteEvento", description = "Tabla **reportes_eventos** (db_reports).")
public class ReporteEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    @Schema(description = "PK id_reporte", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idReporte;

    @Column(name = "id_mascota")
    @Schema(description = "FK mascota", example = "1")
    private Long idMascota;

    @Column(name = "id_usuario_creador")
    @Schema(description = "Usuario creador (id IAM)", example = "1")
    private Long idUsuarioCreador;

    @Column(name = "tipo_reporte", length = 20)
    @Schema(description = "LOST / FOUND", example = "LOST")
    private String tipoReporte;

    @Column(name = "estado", length = 20)
    @Schema(description = "Estado", example = "OPEN")
    private String estado;

    @Column(name = "comuna", length = 120)
    @Schema(description = "Comuna")
    private String comuna;

    @Column(name = "descripcion", length = 500)
    @Schema(description = "Descripcion libre")
    private String descripcion;

    @Column(name = "estado_salud", length = 120)
    @Schema(description = "Estado de salud observado")
    private String estadoSalud;

    @Column(name = "latitud", precision = 10, scale = 6)
    @Schema(description = "Latitud")
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 6)
    @Schema(description = "Longitud")
    private BigDecimal longitud;

    @Column(name = "fecha_creacion")
    @Schema(description = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public Long getIdUsuarioCreador() { return idUsuarioCreador; }
    public void setIdUsuarioCreador(Long idUsuarioCreador) { this.idUsuarioCreador = idUsuarioCreador; }
    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstadoSalud() { return estadoSalud; }
    public void setEstadoSalud(String estadoSalud) { this.estadoSalud = estadoSalud; }
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
