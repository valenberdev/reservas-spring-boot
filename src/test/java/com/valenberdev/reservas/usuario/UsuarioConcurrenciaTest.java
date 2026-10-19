package com.valenberdev.reservas.usuario;

import com.valenberdev.reservas.common.exception.ConflictoException;
import com.valenberdev.reservas.reserva.ReservaRepository;
import com.valenberdev.reservas.support.IntegracionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioConcurrenciaTest extends IntegracionTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ReservaRepository reservaRepository;

    @BeforeEach
    void limpiar() {
        reservaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void soloUnRegistroGanaCuandoLlegan20ConElMismoEmail() throws Exception {
        RegistroRequest request = new RegistroRequest("duplicado@test.com", "clave12345");

        int intentos = 20;
        ExecutorService pool = Executors.newFixedThreadPool(intentos);
        CountDownLatch listos = new CountDownLatch(intentos);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < intentos; i++) {
            resultados.add(pool.submit(() -> {
                listos.countDown();
                largada.await();
                try {
                    usuarioService.registrar(request);
                    return true;
                } catch (ConflictoException e) {
                    return false;
                }
            }));
        }

        listos.await();
        largada.countDown();

        int exitosos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(30, TimeUnit.SECONDS)) {
                exitosos++;
            }
        }
        pool.shutdown();

        assertThat(exitosos).isEqualTo(1);
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }
}