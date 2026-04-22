package com.sanos.reportsservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "detalles_reporte")
public class DetalleReporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle") private Long idDetalle;
    @Column(name = "id_reporte") private Long idReporte;
    @Column(name = "estado_actual") private String estadoActual;
    @Column(name = "condicion_salud") private String condicionSalud;

    public Long getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }
    public String getCondicionSalud() { return condicionSalud; }
    public void setCondicionSalud(String condicionSalud) { this.condicionSalud = condicionSalud; }
}
