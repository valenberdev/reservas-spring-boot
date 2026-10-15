package com.valenberdev.reservas.recurso;

import java.time.Instant;

public record RecursoResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activo,
        Instant creadoEn
) {
    public static RecursoResponse desde(Recurso r) {
        return new RecursoResponse(r.getId(), r.getNombre(), r.getDescripcion(), r.isActivo(), r.getCreadoEn());
    }
}