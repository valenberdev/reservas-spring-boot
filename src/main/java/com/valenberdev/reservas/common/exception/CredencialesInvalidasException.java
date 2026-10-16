package com.valenberdev.reservas.common.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}