package com.valenberdev.reservas.auth;

import com.valenberdev.reservas.common.exception.CredencialesInvalidasException;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        return usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .map(tokenService::generar)
                .orElseThrow(CredencialesInvalidasException::new);
    }
}