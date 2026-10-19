package com.valenberdev.reservas.auth;

import com.valenberdev.reservas.common.config.ReservasProperties;
import com.valenberdev.reservas.usuario.Rol;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final ReservasProperties.Admin admin;

    public AdminInitializer(UsuarioRepository repository,
                            PasswordEncoder passwordEncoder,
                            ReservasProperties propiedades) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.admin = propiedades.admin();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (admin == null || isBlank(admin.email()) || isBlank(admin.password())) {
            return;
        }
        if (admin.password().length() < 8) {
            throw new IllegalStateException("La contraseña del admin debe tener al menos 8 caracteres");
        }

        String emailNormalizado = admin.email().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmail(emailNormalizado)) {
            return;
        }

        repository.save(new Usuario(emailNormalizado, passwordEncoder.encode(admin.password()), Rol.ADMIN));
        log.info("Usuario admin creado: {}", emailNormalizado);
    }

    private static boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}