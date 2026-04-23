package com.sanos.geointelligenceservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "zonas_incidencia")
@Schema(name = "ZonaIncidencia", description = "Tabla **zonas_incidencia** (db_geo).")
public class ZonaIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    @Schema(description = "PK id_zona", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idZona;

    @Column(name = "id_coordenada")
    @Schema(description = "FK coordenadas_reporte.id_coordenada (opcional)")
    private Long idCoordenada;

    @Column(name = "nombre_comuna", length = 120)
    @Schema(description = "Comuna")
    private String nombreComuna;

    @Column(name = "nivel_riesgo", length = 20)
    @Schema(description = "Nivel riesgo", example = "HIGH")
    private String nivelRiesgo;

    @Column(name = "latitud", precision = 10, scale = 6)
    @Schema(description = "Latitud")
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 6)
    @Schema(description = "Longitud")
    private BigDecimal longitud;

    @Column(name = "id_reporte")
    @Schema(description = "FK reporte (dominio reportes)")
    private Long idReporte;

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }
    public Long getIdCoordenada() { return idCoordenada; }
    public void setIdCoordenada(Long idCoordenada) { this.idCoordenada = idCoordenada; }
    public String getNombreComuna() { return nombreComuna; }
    public void setNombreComuna(String nombreComuna) { this.nombreComuna = nombreComuna; }
    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
}
