package com.sanos.geointelligenceservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "coordenadas_reporte")
@Schema(name = "CoordenadaReporte", description = "Tabla **coordenadas_reporte** (db_geo). FK id_reporte.")
public class CoordenadaReporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coordenada")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idCoordenada;
    @Column(name = "id_reporte")
    @Schema(description = "FK reportes_eventos (id logico)", example = "1")
    private Long idReporte;
    @Schema(description = "Latitud WGS84")
    private BigDecimal latitud;
    @Schema(description = "Longitud WGS84")
    private BigDecimal longitud;

    public Long getIdCoordenada() { return idCoordenada; }
    public void setIdCoordenada(Long idCoordenada) { this.idCoordenada = idCoordenada; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
}
