package com.sanos.iamservice.config;

import com.sanos.iamservice.model.ContactoUsuario;
import com.sanos.iamservice.model.Credencial;
import com.sanos.iamservice.model.Usuario;
import com.sanos.iamservice.repository.ContactoUsuarioRepository;
import com.sanos.iamservice.repository.CredencialRepository;
import com.sanos.iamservice.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final CredencialRepository credencialRepo;
    private final ContactoUsuarioRepository contactoRepo;
    private final PasswordEncoder encoder;

    @Value("${sanos.admin.email}") private String adminEmail;
    @Value("${sanos.admin.password}") private String adminPassword;
    @Value("${sanos.admin.display-name}") private String adminDisplayName;
    @Value("${sanos.admin.rut}") private String adminRut;

    public DataSeeder(UsuarioRepository usuarioRepo,
                      CredencialRepository credencialRepo,
                      ContactoUsuarioRepository contactoRepo,
                      PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.credencialRepo = credencialRepo;
        this.contactoRepo = contactoRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedSampleCitizen();
    }

    private void seedAdmin() {
        if (contactoRepo.findByCorreoElectronico(adminEmail.toLowerCase()).isPresent()) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setRutDocumento(adminRut);
        admin.setNombreCompleto(adminDisplayName);
        admin.setComuna("Santiago");
        admin.setDireccion("Oficina central Sanos y Salvos");
        admin.setContactoEmergenciaNombre("Soporte 24/7");
        admin.setContactoEmergenciaTelefono("+56 2 2000 0000");
        admin.setRol("ADMIN");
        admin.setAceptoTerminos(true);
        admin.setAceptoPrivacidad(true);
        admin.setFechaRegistro(LocalDateTime.now());
        admin = usuarioRepo.save(admin);

        ContactoUsuario contacto = new ContactoUsuario();
        contacto.setIdUsuario(admin.getIdUsuario());
        contacto.setCorreoElectronico(adminEmail.toLowerCase());
        contacto.setTelefonoPrincipal("+56 9 0000 0000");
        contactoRepo.save(contacto);

        Credencial credencial = new Credencial();
        credencial.setIdUsuario(admin.getIdUsuario());
        credencial.setPasswordHash(encoder.encode(adminPassword));
        credencial.setEstadoCuenta("ACTIVA");
        credencialRepo.save(credencial);
    }

    private void seedSampleCitizen() {
        String email = "demo@sanosysalvos.cl";
        if (contactoRepo.findByCorreoElectronico(email).isPresent()) {
            return;
        }

        Usuario demo = new Usuario();
        demo.setRutDocumento("22222222-2");
        demo.setNombreCompleto("Demo Ciudadano");
        demo.setComuna("Providencia");
        demo.setDireccion("Av. Providencia 1234");
        demo.setContactoEmergenciaNombre("Familiar Demo");
        demo.setContactoEmergenciaTelefono("+56 9 1111 2222");
        demo.setRol("CITIZEN");
        demo.setAceptoTerminos(true);
        demo.setAceptoPrivacidad(true);
        demo.setFechaRegistro(LocalDateTime.now());
        demo = usuarioRepo.save(demo);

        ContactoUsuario contacto = new ContactoUsuario();
        contacto.setIdUsuario(demo.getIdUsuario());
        contacto.setCorreoElectronico(email);
        contacto.setTelefonoPrincipal("+56 9 2222 3333");
        contactoRepo.save(contacto);

        Credencial cred = new Credencial();
        cred.setIdUsuario(demo.getIdUsuario());
        cred.setPasswordHash(encoder.encode("Demo#Sanos2026"));
        cred.setEstadoCuenta("ACTIVA");
        credencialRepo.save(cred);
    }
}
