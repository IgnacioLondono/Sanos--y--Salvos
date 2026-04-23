package com.sanos.iamservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "credenciales")
@Schema(name = "Credencial", description = "Tabla **credenciales** (db_iam). Hash BCrypt; no exponer en respuestas publicas.")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credencial")
    @Schema(description = "PK id_credencial", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idCredencial;

    @Column(name = "id_usuario", unique = true)
    @Schema(description = "FK usuarios.id_usuario", example = "1")
    private Long idUsuario;

    @Column(name = "password_hash", length = 80)
    @Schema(description = "BCrypt hash", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String passwordHash;

    @Column(name = "estado_cuenta", length = 20)
    @Schema(description = "Estado cuenta", example = "ACTIVA")
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
