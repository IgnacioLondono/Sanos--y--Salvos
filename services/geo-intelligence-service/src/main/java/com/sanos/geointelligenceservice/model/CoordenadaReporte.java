package com.sanos.geointelligenceservice.model;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "coordenadas_reporte")
public class CoordenadaReporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coordenada") private Long idCoordenada;
    @Column(name = "id_reporte") private Long idReporte;
    private BigDecimal latitud;
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
