package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false, updatable = false)
    private Recurso recurso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    protected Reserva() {
    }

    public Reserva(Recurso recurso, Usuario usuario, Instant inicio, Instant fin) {
        this.recurso = recurso;
        this.usuario = usuario;
        this.inicio = inicio;
        this.fin = fin;
    }

    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    public Long getId() { return id; }
    public Recurso getRecurso() { return recurso; }
    public Usuario getUsuario() { return usuario; }
    public Instant getInicio() { return inicio; }
    public Instant getFin() { return fin; }
    public EstadoReserva getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
}