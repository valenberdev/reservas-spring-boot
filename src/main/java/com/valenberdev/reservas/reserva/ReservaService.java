package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.common.exception.ConflictoException;
import com.valenberdev.reservas.common.exception.NoEncontradoException;
import com.valenberdev.reservas.common.exception.SolicitudInvalidaException;
import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.recurso.RecursoRepository;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;

@Service
public class ReservaService {

    private static final String SQLSTATE_EXCLUSION_VIOLATION = "23P01";

    private final ReservaRepository reservaRepository;
    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          RecursoRepository recursoRepository,
                          UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.recursoRepository = recursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        if (!request.fin().isAfter(request.inicio())) {
            throw new SolicitudInvalidaException("La fecha de fin debe ser posterior a la de inicio");
        }
        if (!request.inicio().isAfter(Instant.now())) {
            throw new SolicitudInvalidaException("La reserva debe comenzar en el futuro");
        }

        Recurso recurso = recursoRepository.findById(request.recursoId())
                .orElseThrow(() -> new NoEncontradoException("No existe un recurso con id " + request.recursoId()));
        if (!recurso.isActivo()) {
            throw new ConflictoException("El recurso no está disponible");
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new NoEncontradoException("No existe un usuario con id " + request.usuarioId()));

        try {
            Reserva reserva = new Reserva(recurso, usuario, request.inicio(), request.fin());
            return ReservaResponse.desde(reservaRepository.saveAndFlush(reserva));
        } catch (DataIntegrityViolationException ex) {
            if (esSolapamiento(ex)) {
                throw new ConflictoException("El recurso ya está reservado en ese horario");
            }
            throw ex;
        }
    }

    private boolean esSolapamiento(DataIntegrityViolationException ex) {
        return ex.getMostSpecificCause() instanceof SQLException sql
                && SQLSTATE_EXCLUSION_VIOLATION.equals(sql.getSQLState());
    }
}