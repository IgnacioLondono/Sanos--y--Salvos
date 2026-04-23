package com.sanos.matchingservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "desglose_similitud")
@Schema(name = "DesgloseSimilitud", description = "Tabla **desglose_similitud** (db_matching). Detalle por criterio.")
public class DesgloseSimilitud {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_desglose")
    @Schema(description = "PK", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idDesglose;
    @Column(name = "id_match")
    @Schema(description = "FK coincidencias_ia.id_match", example = "1")
    private Long idMatch;
    @Schema(description = "Criterio evaluado (texto libre o codigo)", example = "DISTANCIA_KM")
    private String criterio;

    public Long getIdDesglose() { return idDesglose; }
    public void setIdDesglose(Long idDesglose) { this.idDesglose = idDesglose; }
    public Long getIdMatch() { return idMatch; }
    public void setIdMatch(Long idMatch) { this.idMatch = idMatch; }
    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }
}
