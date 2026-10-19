package com.valenberdev.reservas.reserva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FinalizadorDeReservas {

    private static final Logger log = LoggerFactory.getLogger(FinalizadorDeReservas.class);

    private final ReservaService service;

    public FinalizadorDeReservas(ReservaService service) {
        this.service = service;
    }

    @Scheduled(cron = "${reservas.finalizacion.cron}", zone = "UTC")
    public void ejecutar() {
        int finalizadas = service.finalizarVencidas();
        if (finalizadas > 0) {
            log.info("Reservas finalizadas: {}", finalizadas);
        }
    }
}