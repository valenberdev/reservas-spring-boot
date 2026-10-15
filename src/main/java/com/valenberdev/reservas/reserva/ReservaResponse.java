package com.valenberdev.reservas.reserva;

import java.time.Instant;

public record ReservaResponse(
        Long id,
        Long recursoId,
        Long usuarioId,
        Instant inicio,
        Instant fin,
        EstadoReserva estado,
        Instant creadoEn
) {
    public static ReservaResponse desde(Reserva r) {
        return new ReservaResponse(
                r.getId(),
                r.getRecurso().getId(),
                r.getUsuario().getId(),
                r.getInicio(),
                r.getFin(),
                r.getEstado(),
                r.getCreadoEn());
    }
}