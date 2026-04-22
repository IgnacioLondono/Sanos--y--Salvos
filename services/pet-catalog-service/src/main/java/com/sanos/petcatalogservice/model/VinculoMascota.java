package com.sanos.petcatalogservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "vinculos_mascotas")
public class VinculoMascota {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vinculo") private Long idVinculo;
    @Column(name = "id_usuario") private Long idUsuario;
    @Column(name = "id_mascota") private Long idMascota;
    @Column(name = "tipo_relacion") private String tipoRelacion;

    public Long getIdVinculo() { return idVinculo; }
    public void setIdVinculo(Long idVinculo) { this.idVinculo = idVinculo; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public String getTipoRelacion() { return tipoRelacion; }
    public void setTipoRelacion(String tipoRelacion) { this.tipoRelacion = tipoRelacion; }
}
