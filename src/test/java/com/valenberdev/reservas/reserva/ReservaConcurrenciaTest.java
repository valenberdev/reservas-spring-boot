package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.common.exception.ConflictoException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaConcurrenciaTest extends IntegracionTest {

    @Autowired
    ReservaService reservaService;

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
    void soloUnaReservaGanaCuandoLlegan30AlMismoHorario() throws Exception {
        Recurso recurso = recursoRepository.save(new Recurso("Cancha de test", null));
        Usuario usuario = usuarioRepository.save(new Usuario("concurrencia@test.com", "hash", Rol.CLIENTE));

        Instant inicio = Instant.now().plus(7, ChronoUnit.DAYS);
        ReservaRequest request = new ReservaRequest(
                recurso.getId(), inicio, inicio.plus(1, ChronoUnit.HOURS));

        int intentos = 30;
        ExecutorService pool = Executors.newFixedThreadPool(intentos);
        CountDownLatch listos = new CountDownLatch(intentos);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < intentos; i++) {
            resultados.add(pool.submit(() -> {
                listos.countDown();
                largada.await();
                try {
                    reservaService.crear(usuario.getId(), request);
                    return true;
                } catch (ConflictoException e) {
                    return false;
                }
            }));
        }

        listos.await();
        largada.countDown();

        int exitosas = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(30, TimeUnit.SECONDS)) {
                exitosas++;
            }
        }
        pool.shutdown();

        assertThat(exitosas).isEqualTo(1);
        assertThat(reservaRepository.count()).isEqualTo(1);
    }
}