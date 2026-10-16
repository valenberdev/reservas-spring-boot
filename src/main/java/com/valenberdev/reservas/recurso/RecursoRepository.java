package com.valenberdev.reservas.recurso;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Recurso> findWithLockById(Long id);
}