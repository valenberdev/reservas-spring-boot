package com.valenberdev.reservas.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioIdOrderByInicioDesc(Long usuarioId);

    @Modifying
    @Query("""
            update Reserva r
            set r.estado = com.valenberdev.reservas.reserva.EstadoReserva.FINALIZADA
            where r.estado = com.valenberdev.reservas.reserva.EstadoReserva.ACTIVA
              and r.fin < :ahora
            """)
    int finalizarVencidas(@Param("ahora") Instant ahora);
}