package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.common.PaginaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reservas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReservaRequest request) {
        return service.crear(idUsuario(jwt), request);
    }

    @GetMapping
    public PaginaResponse<ReservaResponse> listar(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        Pageable sinOrdenDelCliente = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return service.listarPorUsuario(idUsuario(jwt), sinOrdenDelCliente);
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.cancelar(idUsuario(jwt), id);
    }

    private static Long idUsuario(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}