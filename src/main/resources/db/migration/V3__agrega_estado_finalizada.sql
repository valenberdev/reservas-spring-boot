ALTER TABLE reservas DROP CONSTRAINT reservas_estado_check;

ALTER TABLE reservas
    ADD CONSTRAINT reservas_estado_check
    CHECK (estado IN ('ACTIVA', 'CANCELADA', 'FINALIZADA'));