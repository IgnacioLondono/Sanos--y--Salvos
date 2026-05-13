package com.sanos.iamservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "contactos_usuario")
@Schema(name = "ContactoUsuario", description = "Tabla **contactos_usuario** (db_iam). Correo y telefono por usuario.")
public class ContactoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    @Schema(description = "PK id_contacto", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idContacto;

    @Column(name = "id_usuario")
    @Schema(description = "FK usuarios.id_usuario", example = "1")
    private Long idUsuario;

    @Column(name = "correo_electronico", unique = true, length = 180)
    @Schema(description = "Correo unico", example = "user@mail.cl")
    private String correoElectronico;

    @Column(name = "telefono_principal", length = 32)
    @Schema(description = "Telefono", example = "+56 9 1234 5678")
    private String telefonoPrincipal;

    public Long getIdContacto() { return idContacto; }
    public void setIdContacto(Long idContacto) { this.idContacto = idContacto; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public void setTelefonoPrincipal(String telefonoPrincipal) { this.telefonoPrincipal = telefonoPrincipal; }
}
