package com.sanos.iamservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contactos_usuario")
public class ContactoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    private Long idContacto;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "correo_electronico", unique = true, length = 180)
    private String correoElectronico;

    @Column(name = "telefono_principal", length = 32)
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
