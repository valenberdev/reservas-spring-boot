package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.common.exception.ConflictoException;
import com.valenberdev.reservas.common.exception.NoEncontradoException;
import com.valenberdev.reservas.common.exception.SolicitudInvalidaException;
import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.recurso.RecursoRepository;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaService {

    private static final String SQLSTATE_EXCLUSION_VIOLATION = "23P01";

    private final ReservaRepository reservaRepository;
    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;
    private final long horasMinimasCancelacion;

    public ReservaService(ReservaRepository reservaRepository,
                          RecursoRepository recursoRepository,
                          UsuarioRepository usuarioRepository,
                          Clock clock,
                          @Value("${reservas.cancelacion.horas-minimas}") long horasMinimasCancelacion) {
        this.reservaRepository = reservaRepository;
        this.recursoRepository = recursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
        this.horasMinimasCancelacion = horasMinimasCancelacion;
    }

    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        if (!request.fin().isAfter(request.inicio())) {
            throw new SolicitudInvalidaException("La fecha de fin debe ser posterior a la de inicio");
        }
        if (!request.inicio().isAfter(clock.instant())) {
            throw new SolicitudInvalidaException("La reserva debe comenzar en el futuro");
        }

        Recurso recurso = recursoRepository.findWithLockById(request.recursoId())
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

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioIdOrderByInicioDesc(usuarioId).stream()
                .map(ReservaResponse::desde)
                .toList();
    }

    @Transactional
    public ReservaResponse cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe una reserva con id " + id));

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ConflictoException("La reserva ya está cancelada");
        }

        Instant limite = reserva.getInicio().minus(horasMinimasCancelacion, ChronoUnit.HOURS);
        if (clock.instant().isAfter(limite)) {
            throw new ConflictoException(
                    "Solo se puede cancelar hasta " + horasMinimasCancelacion + " horas antes del inicio");
        }

        reserva.cancelar();
        return ReservaResponse.desde(reserva);
    }

    private boolean esSolapamiento(DataIntegrityViolationException ex) {
        return ex.getMostSpecificCause() instanceof SQLException sql
                && SQLSTATE_EXCLUSION_VIOLATION.equals(sql.getSQLState());
    }
}