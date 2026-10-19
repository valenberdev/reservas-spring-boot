package com.valenberdev.reservas.auth;

import com.valenberdev.reservas.common.config.ReservasProperties;
import com.valenberdev.reservas.usuario.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Clock clock;
    private final long expiracionMinutos;

    public TokenService(JwtEncoder encoder, Clock clock, ReservasProperties propiedades) {
        this.encoder = encoder;
        this.clock = clock;
        this.expiracionMinutos = propiedades.jwt().expiracionMinutos();
    }

    public TokenResponse generar(Usuario usuario) {
        Instant ahora = clock.instant();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiresAt(expira)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResponse(token, "Bearer", expiracionMinutos * 60);
    }
}