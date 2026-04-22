package com.sanos.capacityservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "asignacion_capacidad")
public class AsignacionCapacidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion") private Long idAsignacion;
    @Column(name = "id_usuario") private Long idUsuario;
    @Column(name = "id_equipo") private Long idEquipo;
    @Column(name = "horas_dedicadas") private Integer horasDedicadas;

    public Long getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(Long idAsignacion) { this.idAsignacion = idAsignacion; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Long idEquipo) { this.idEquipo = idEquipo; }
    public Integer getHorasDedicadas() { return horasDedicadas; }
    public void setHorasDedicadas(Integer horasDedicadas) { this.horasDedicadas = horasDedicadas; }
}
