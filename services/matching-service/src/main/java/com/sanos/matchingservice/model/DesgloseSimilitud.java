package com.sanos.matchingservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "desglose_similitud")
public class DesgloseSimilitud {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_desglose") private Long idDesglose;
    @Column(name = "id_match") private Long idMatch;
    private String criterio;

    public Long getIdDesglose() { return idDesglose; }
    public void setIdDesglose(Long idDesglose) { this.idDesglose = idDesglose; }
    public Long getIdMatch() { return idMatch; }
    public void setIdMatch(Long idMatch) { this.idMatch = idMatch; }
    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }
}
