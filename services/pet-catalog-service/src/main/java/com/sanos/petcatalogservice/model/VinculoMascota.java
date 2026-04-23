package com.sanos.petcatalogservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "vinculos_mascotas")
@Schema(name = "VinculoMascota", description = "Tabla **vinculos_mascotas** (db_pets). Relacion usuario-mascota.")
public class VinculoMascota {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vinculo")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idVinculo;
    @Column(name = "id_usuario")
    @Schema(description = "FK usuario IAM (id logico)", example = "1")
    private Long idUsuario;
    @Column(name = "id_mascota")
    @Schema(description = "FK mascotas.id_mascota", example = "1")
    private Long idMascota;
    @Column(name = "tipo_relacion")
    @Schema(description = "Tipo relacion", example = "OWNER")
    private String tipoRelacion;

    public Long getIdVinculo() { return idVinculo; }
    public void setIdVinculo(Long idVinculo) { this.idVinculo = idVinculo; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public String getTipoRelacion() { return tipoRelacion; }
    public void setTipoRelacion(String tipoRelacion) { this.tipoRelacion = tipoRelacion; }
}
