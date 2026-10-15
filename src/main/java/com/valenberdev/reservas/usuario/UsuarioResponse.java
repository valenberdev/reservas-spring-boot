package com.valenberdev.reservas.usuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String email,
        Rol rol,
        Instant creadoEn
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getRol(), u.getCreadoEn());
    }
}