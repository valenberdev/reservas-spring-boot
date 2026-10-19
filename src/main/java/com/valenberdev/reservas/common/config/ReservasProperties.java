package com.valenberdev.reservas.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "reservas")
public record ReservasProperties(
        @NotNull @Valid Cancelacion cancelacion,
        @NotNull @Valid Token jwt,
        @NotNull @Valid Finalizacion finalizacion,
        Admin admin
) {

    public record Cancelacion(@Min(0) long horasMinimas) {
    }

    public record Token(@NotBlank @Size(min = 32) String secret, @Min(1) long expiracionMinutos) {
    }

    public record Finalizacion(@NotBlank String cron) {
    }

    public record Admin(String email, String password) {
    }
}