package com.sanos.reportsservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "detalles_reporte")
@Schema(name = "DetalleReporte", description = "Tabla **detalles_reporte** (db_reports). FK id_reporte.")
public class DetalleReporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idDetalle;
    @Column(name = "id_reporte")
    @Schema(description = "FK reportes_eventos.id_reporte", example = "1")
    private Long idReporte;
    @Column(name = "estado_actual")
    @Schema(description = "Estado detallado")
    private String estadoActual;
    @Column(name = "condicion_salud")
    @Schema(description = "Condicion salud")
    private String condicionSalud;

    public Long getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }
    public String getCondicionSalud() { return condicionSalud; }
    public void setCondicionSalud(String condicionSalud) { this.condicionSalud = condicionSalud; }
}
