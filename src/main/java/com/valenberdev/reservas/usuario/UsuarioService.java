package com.valenberdev.reservas.usuario;

import com.valenberdev.reservas.common.exception.ConflictoException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            throw new ConflictoException("El email ya está registrado");
        }

        Usuario usuario = new Usuario(email, passwordEncoder.encode(request.password()), Rol.CLIENTE);
        return UsuarioResponse.desde(repository.save(usuario));
    }
}