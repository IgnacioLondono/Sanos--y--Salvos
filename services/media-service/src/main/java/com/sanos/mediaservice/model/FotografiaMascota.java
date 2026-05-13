package com.sanos.mediaservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotografias_mascotas")
@Schema(name = "FotografiaMascota", description = "Tabla **fotografias_mascotas** (db_media).")
public class FotografiaMascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    @Schema(description = "PK id_foto", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idFoto;

    @Column(name = "id_mascota")
    @Schema(description = "FK mascota", example = "1")
    private Long idMascota;

    @Column(name = "id_reporte")
    @Schema(description = "FK reporte opcional", example = "1")
    private Long idReporte;

    @Column(name = "url_almacenamiento", length = 500)
    @Schema(description = "URL recurso")
    private String urlAlmacenamiento;

    @Column(name = "tags", length = 250)
    @Schema(description = "Tags separados por coma")
    private String tags;

    @Column(name = "fecha_captura")
    @Schema(description = "fecha_captura")
    private LocalDateTime fechaCaptura;

    public Long getIdFoto() { return idFoto; }
    public void setIdFoto(Long idFoto) { this.idFoto = idFoto; }
    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public String getUrlAlmacenamiento() { return urlAlmacenamiento; }
    public void setUrlAlmacenamiento(String urlAlmacenamiento) { this.urlAlmacenamiento = urlAlmacenamiento; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getFechaCaptura() { return fechaCaptura; }
    public void setFechaCaptura(LocalDateTime fechaCaptura) { this.fechaCaptura = fechaCaptura; }
}
