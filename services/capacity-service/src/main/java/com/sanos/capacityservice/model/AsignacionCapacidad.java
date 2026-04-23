package com.sanos.capacityservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "asignacion_capacidad")
@Schema(name = "AsignacionCapacidad", description = "Tabla **asignacion_capacidad** (db_capacity). Usuario en equipo.")
public class AsignacionCapacidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idAsignacion;
    @Column(name = "id_usuario")
    @Schema(description = "FK usuario IAM", example = "1")
    private Long idUsuario;
    @Column(name = "id_equipo")
    @Schema(description = "FK equipos_colaboracion.id_equipo", example = "1")
    private Long idEquipo;
    @Column(name = "horas_dedicadas")
    @Schema(description = "Horas dedicadas", example = "5")
    private Integer horasDedicadas;

    public Long getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(Long idAsignacion) { this.idAsignacion = idAsignacion; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Long idEquipo) { this.idEquipo = idEquipo; }
    public Integer getHorasDedicadas() { return horasDedicadas; }
    public void setHorasDedicadas(Integer horasDedicadas) { this.horasDedicadas = horasDedicadas; }
}
