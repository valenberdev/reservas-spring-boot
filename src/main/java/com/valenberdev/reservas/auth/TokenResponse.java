package com.valenberdev.reservas.auth;

public record TokenResponse(String token, String tipo, long expiraEnSegundos) {
}