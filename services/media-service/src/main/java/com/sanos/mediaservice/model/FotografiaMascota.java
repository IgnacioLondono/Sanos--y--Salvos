package com.sanos.mediaservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotografias_mascotas")
public class FotografiaMascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Long idFoto;

    @Column(name = "id_mascota")
    private Long idMascota;

    @Column(name = "id_reporte")
    private Long idReporte;

    @Column(name = "url_almacenamiento", length = 500)
    private String urlAlmacenamiento;

    @Column(name = "tags", length = 250)
    private String tags;

    @Column(name = "fecha_captura")
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
