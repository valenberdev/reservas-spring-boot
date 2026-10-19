package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.recurso.RecursoRepository;
import com.valenberdev.reservas.support.IntegracionTest;
import com.valenberdev.reservas.usuario.Rol;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaFinalizacionTest extends IntegracionTest {

    @Autowired
    ReservaService service;

    @Autowired
    ReservaRepository reservaRepository;

    @Autowired
    RecursoRepository recursoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @BeforeEach
    void limpiar() {
        reservaRepository.deleteAll();
        usuarioRepository.deleteAll();
        recursoRepository.deleteAll();
    }

    @Test
    void finalizaSoloLasActivasQueYaTerminaron() {
        Recurso recurso = recursoRepository.save(new Recurso("Cancha", null));
        Usuario usuario = usuarioRepository.save(new Usuario("finaliza@test.com", "hash", Rol.CLIENTE));
        Instant ahora = Instant.now();

        Reserva vencida = reservaRepository.save(
                new Reserva(recurso, usuario, ahora.minus(3, ChronoUnit.HOURS), ahora.minus(2, ChronoUnit.HOURS)));
        Reserva futura = reservaRepository.save(
                new Reserva(recurso, usuario, ahora.plus(48, ChronoUnit.HOURS), ahora.plus(49, ChronoUnit.HOURS)));
        Reserva cancelada = new Reserva(
                recurso, usuario, ahora.minus(6, ChronoUnit.HOURS), ahora.minus(5, ChronoUnit.HOURS));
        cancelada.cancelar();
        reservaRepository.save(cancelada);

        int finalizadas = service.finalizarVencidas();

        assertThat(finalizadas).isEqualTo(1);
        assertThat(estadoDe(vencida)).isEqualTo(EstadoReserva.FINALIZADA);
        assertThat(estadoDe(futura)).isEqualTo(EstadoReserva.ACTIVA);
        assertThat(estadoDe(cancelada)).isEqualTo(EstadoReserva.CANCELADA);
    }

    private EstadoReserva estadoDe(Reserva reserva) {
        return reservaRepository.findById(reserva.getId()).orElseThrow().getEstado();
    }
}