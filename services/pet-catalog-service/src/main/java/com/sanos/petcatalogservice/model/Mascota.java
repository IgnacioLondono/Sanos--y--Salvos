package com.sanos.petcatalogservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
@Schema(name = "Mascota", description = "Tabla **mascotas** (db_pets).")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    @Schema(description = "PK id_mascota", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idMascota;

    @Column(name = "nombre", length = 120)
    @Schema(description = "Nombre", example = "Milo")
    private String nombre;

    @Column(name = "especie", length = 40)
    @Schema(description = "Especie codigo", example = "DOG")
    private String especie;

    @Column(name = "numero_chip", length = 40, unique = true)
    @Schema(description = "Chip unico", example = "CHIP-001")
    private String numeroChip;

    @Column(name = "fecha_registro")
    @Schema(description = "fecha_registro")
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
