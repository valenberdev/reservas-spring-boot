package com.valenberdev.reservas.recurso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecursoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion
) {
}