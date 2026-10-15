CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE reservas (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recurso_id BIGINT      NOT NULL REFERENCES recursos (id),
    usuario_id BIGINT      NOT NULL REFERENCES usuarios (id),
    inicio     TIMESTAMPTZ NOT NULL,
    fin        TIMESTAMPTZ NOT NULL,
    estado     VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
               CHECK (estado IN ('ACTIVA', 'CANCELADA')),
    creado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT reservas_fin_posterior CHECK (fin > inicio),

    CONSTRAINT reservas_sin_solapamiento EXCLUDE USING gist (
        recurso_id WITH =,
        tstzrange(inicio, fin) WITH &&
    ) WHERE (estado = 'ACTIVA')
);

CREATE INDEX idx_reservas_usuario ON reservas (usuario_id);