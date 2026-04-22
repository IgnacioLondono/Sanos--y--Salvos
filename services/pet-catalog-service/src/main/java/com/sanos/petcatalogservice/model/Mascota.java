package com.sanos.petcatalogservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    private Long idMascota;

    @Column(name = "nombre", length = 120)
    private String nombre;

    @Column(name = "especie", length = 40)
    private String especie;

    @Column(name = "numero_chip", length = 40, unique = true)
    private String numeroChip;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getNumeroChip() { return numeroChip; }
    public void setNumeroChip(String numeroChip) { this.numeroChip = numeroChip; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
