package com.sanos.matchingservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coincidencias_ia")
public class CoincidenciaIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_match")
    private Long idMatch;

    @Column(name = "id_reporte_perdida")
    private Long idReportePerdida;

    @Column(name = "id_reporte_encontrada")
    private Long idReporteEncontrada;

    @Column(name = "score_total")
    private Float scoreTotal;

    @Column(name = "explicacion", length = 400)
    private String explicacion;

    @Column(name = "creado_en")
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
