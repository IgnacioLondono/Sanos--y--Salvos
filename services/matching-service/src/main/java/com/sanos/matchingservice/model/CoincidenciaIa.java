package com.sanos.matchingservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coincidencias_ia")
@Schema(name = "CoincidenciaIa", description = "Tabla **coincidencias_ia** (db_matching).")
public class CoincidenciaIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_match")
    @Schema(description = "PK id_match", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idMatch;

    @Column(name = "id_reporte_perdida")
    @Schema(description = "FK reporte perdida", example = "1")
    private Long idReportePerdida;

    @Column(name = "id_reporte_encontrada")
    @Schema(description = "FK reporte encontrada", example = "2")
    private Long idReporteEncontrada;

    @Column(name = "score_total")
    @Schema(description = "Puntuacion agregada", example = "0.75")
    private Float scoreTotal;

    @Column(name = "explicacion", length = 400)
    @Schema(description = "Explicacion legible")
    private String explicacion;

    @Column(name = "creado_en")
    @Schema(description = "Timestamp creacion")
    private LocalDateTime creadoEn;

    public Long getIdMatch() { return idMatch; }
    public void setIdMatch(Long idMatch) { this.idMatch = idMatch; }
    public Long getIdReportePerdida() { return idReportePerdida; }
    public void setIdReportePerdida(Long idReportePerdida) { this.idReportePerdida = idReportePerdida; }
    public Long getIdReporteEncontrada() { return idReporteEncontrada; }
    public void setIdReporteEncontrada(Long idReporteEncontrada) { this.idReporteEncontrada = idReporteEncontrada; }
    public Float getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(Float scoreTotal) { this.scoreTotal = scoreTotal; }
    public String getExplicacion() { return explicacion; }
    public void setExplicacion(String explicacion) { this.explicacion = explicacion; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
