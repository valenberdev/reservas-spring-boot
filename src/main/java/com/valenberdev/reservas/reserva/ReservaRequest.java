package com.valenberdev.reservas.reserva;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReservaRequest(
        @NotNull Long recursoId,
        @NotNull Instant inicio,
        @NotNull Instant fin
) {
}