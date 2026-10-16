package com.valenberdev.reservas.auth;

import com.valenberdev.reservas.usuario.Rol;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminInitializer(UsuarioRepository repository,
                            PasswordEncoder passwordEncoder,
                            @Value("${reservas.admin.email:}") String email,
                            @Value("${reservas.admin.password:}") String password) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (password.length() < 8) {
            throw new IllegalStateException("La contraseña del admin debe tener al menos 8 caracteres");
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmail(emailNormalizado)) {
            return;
        }

        repository.save(new Usuario(emailNormalizado, passwordEncoder.encode(password), Rol.ADMIN));
        log.info("Usuario admin creado: {}", emailNormalizado);
    }
}