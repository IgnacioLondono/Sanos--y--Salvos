package com.sanos.petcatalogservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "caracteristicas_fisicas")
public class CaracteristicaFisica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caracteristica") private Long idCaracteristica;
    @Column(name = "id_mascota") private Long idMascota;
    private String raza;
    @Column(name = "color_principal") private String colorPrincipal;
    private String tamano;

    // Getters / Setters
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
