package com.sanos.petcatalogservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "caracteristicas_fisicas")
@Schema(name = "CaracteristicaFisica", description = "Tabla **caracteristicas_fisicas** (db_pets). FK id_mascota.")
public class CaracteristicaFisica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caracteristica")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idCaracteristica;
    @Column(name = "id_mascota")
    @Schema(description = "FK mascotas.id_mascota", example = "1")
    private Long idMascota;
    @Schema(description = "Raza", example = "Mestizo")
    private String raza;
    @Column(name = "color_principal")
    @Schema(description = "Color principal")
    private String colorPrincipal;
    @Schema(description = "Tamano", example = "Mediano")
    private String tamano;

    public Long getIdCaracteristica() { return idCaracteristica; }
    public void setIdCaracteristica(Long idCaracteristica) { this.idCaracteristica = idCaracteristica; }
    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getColorPrincipal() { return colorPrincipal; }
    public void setColorPrincipal(String colorPrincipal) { this.colorPrincipal = colorPrincipal; }
    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }
}
