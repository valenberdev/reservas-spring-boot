package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.common.exception.ConflictoException;
import com.valenberdev.reservas.common.exception.NoEncontradoException;
import com.valenberdev.reservas.common.exception.SolicitudInvalidaException;
import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.recurso.RecursoRepository;
import com.valenberdev.reservas.usuario.Rol;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-01T12:00:00Z");

    @Mock
    ReservaRepository reservaRepository;

    @Mock
    RecursoRepository recursoRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    ReservaService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        service = new ReservaService(reservaRepository, recursoRepository, usuarioRepository, reloj, 24);
    }

    private Reserva reservaDe(Long usuarioId, Instant inicio) {
        Usuario usuario = new Usuario("u@test.com", "hash", Rol.CLIENTE);
        ReflectionTestUtils.setField(usuario, "id", usuarioId);
        Recurso recurso = new Recurso("Cancha", null);
        return new Reserva(recurso, usuario, inicio, inicio.plus(1, ChronoUnit.HOURS));
    }

    @Test
    void cancelaCuandoFaltaMasDelMinimo() {
        Reserva reserva = reservaDe(1L, AHORA.plus(25, ChronoUnit.HOURS));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        ReservaResponse respuesta = service.cancelar(1L, 10L);

        assertThat(respuesta.estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void permiteCancelarJustoEnElLimite() {
        Reserva reserva = reservaDe(1L, AHORA.plus(24, ChronoUnit.HOURS));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        ReservaResponse respuesta = service.cancelar(1L, 10L);

        assertThat(respuesta.estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void rechazaCancelarCuandoFaltaMenosDelMinimo() {
        Reserva reserva = reservaDe(1L, AHORA.plus(23, ChronoUnit.HOURS));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(1L, 10L)).isInstanceOf(ConflictoException.class);
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void rechazaCancelarReservaDeOtroUsuario() {
        Reserva reserva = reservaDe(1L, AHORA.plus(48, ChronoUnit.HOURS));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.cancelar(2L, 10L)).isInstanceOf(NoEncontradoException.class);
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void rechazaCrearReservaEnElPasado() {
        ReservaRequest request = new ReservaRequest(
                1L, AHORA.minus(1, ChronoUnit.HOURS), AHORA.plus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> service.crear(1L, request)).isInstanceOf(SolicitudInvalidaException.class);
        verifyNoInteractions(recursoRepository, usuarioRepository);
    }

    @Test
    void rechazaCrearReservaConFinAnteriorAlInicio() {
        ReservaRequest request = new ReservaRequest(
                1L, AHORA.plus(2, ChronoUnit.HOURS), AHORA.plus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> service.crear(1L, request)).isInstanceOf(SolicitudInvalidaException.class);
        verifyNoInteractions(recursoRepository, usuarioRepository);
    }

    @Test
    void rechazaCrearReservaEnRecursoInactivo() {
        Recurso inactivo = new Recurso("Cancha cerrada", null);
        inactivo.setActivo(false);
        when(recursoRepository.findWithLockById(1L)).thenReturn(Optional.of(inactivo));
        ReservaRequest request = new ReservaRequest(
                1L, AHORA.plus(2, ChronoUnit.HOURS), AHORA.plus(3, ChronoUnit.HOURS));

        assertThatThrownBy(() -> service.crear(1L, request)).isInstanceOf(ConflictoException.class);
    }
}