package com.sanos.iamservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "rut_documento", unique = true, length = 20)
    private String rutDocumento;

    @Column(name = "nombre_completo", length = 160)
    private String nombreCompleto;

    @Column(name = "comuna", length = 120)
    private String comuna;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "contacto_emergencia_nombre", length = 160)
    private String contactoEmergenciaNombre;

    @Column(name = "contacto_emergencia_telefono", length = 32)
    private String contactoEmergenciaTelefono;

    @Column(name = "rol", length = 20)
    private String rol;

    @Column(name = "acepto_terminos")
    private Boolean aceptoTerminos;

    @Column(name = "acepto_privacidad")
    private Boolean aceptoPrivacidad;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getRutDocumento() { return rutDocumento; }
    public void setRutDocumento(String rutDocumento) { this.rutDocumento = rutDocumento; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) { this.contactoEmergenciaNombre = contactoEmergenciaNombre; }

    public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }
    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) { this.contactoEmergenciaTelefono = contactoEmergenciaTelefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getAceptoTerminos() { return aceptoTerminos; }
    public void setAceptoTerminos(Boolean aceptoTerminos) { this.aceptoTerminos = aceptoTerminos; }

    public Boolean getAceptoPrivacidad() { return aceptoPrivacidad; }
    public void setAceptoPrivacidad(Boolean aceptoPrivacidad) { this.aceptoPrivacidad = aceptoPrivacidad; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
