package com.sanos.capacityservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipos_colaboracion")
public class EquipoColaboracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long idEquipo;

    @Column(name = "nombre_equipo", length = 180)
    private String nombreEquipo;

    @Column(name = "organizacion", length = 180)
    private String organizacion;

    @Column(name = "zona_operacion", length = 120)
    private String zonaOperacion;

    @Column(name = "voluntarios")
    private Integer voluntarios;

    @Column(name = "horas_disponibles")
    private Integer horasDisponibles;

    @Column(name = "disponible_desde")
    private LocalDateTime disponibleDesde;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Long getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Long idEquipo) { this.idEquipo = idEquipo; }
    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }
    public String getOrganizacion() { return organizacion; }
    public void setOrganizacion(String organizacion) { this.organizacion = organizacion; }
    public String getZonaOperacion() { return zonaOperacion; }
    public void setZonaOperacion(String zonaOperacion) { this.zonaOperacion = zonaOperacion; }
    public Integer getVoluntarios() { return voluntarios; }
    public void setVoluntarios(Integer voluntarios) { this.voluntarios = voluntarios; }
    public Integer getHorasDisponibles() { return horasDisponibles; }
    public void setHorasDisponibles(Integer horasDisponibles) { this.horasDisponibles = horasDisponibles; }
    public LocalDateTime getDisponibleDesde() { return disponibleDesde; }
    public void setDisponibleDesde(LocalDateTime disponibleDesde) { this.disponibleDesde = disponibleDesde; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
