package com.sanos.geointelligenceservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "zonas_incidencia")
public class ZonaIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private Long idZona;

    @Column(name = "id_coordenada")
    private Long idCoordenada;

    @Column(name = "nombre_comuna", length = 120)
    private String nombreComuna;

    @Column(name = "nivel_riesgo", length = 20)
    private String nivelRiesgo;

    @Column(name = "latitud", precision = 10, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 6)
    private BigDecimal longitud;

    @Column(name = "id_reporte")
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
