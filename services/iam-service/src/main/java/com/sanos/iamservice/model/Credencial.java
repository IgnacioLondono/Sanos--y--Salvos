package com.sanos.iamservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "credenciales")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credencial")
    private Long idCredencial;

    @Column(name = "id_usuario", unique = true)
    private Long idUsuario;

    @Column(name = "password_hash", length = 80)
    private String passwordHash;

    @Column(name = "estado_cuenta", length = 20)
    private String estadoCuenta;

    public Long getIdCredencial() { return idCredencial; }
    public void setIdCredencial(Long idCredencial) { this.idCredencial = idCredencial; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(String estadoCuenta) { this.estadoCuenta = estadoCuenta; }
}
