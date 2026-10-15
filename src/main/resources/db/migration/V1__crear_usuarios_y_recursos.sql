CREATE TABLE usuarios (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'CLIENTE')),
    creado_en     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE recursos (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    activo      BOOLEAN      NOT NULL DEFAULT true,
    creado_en   TIMESTAMPTZ  NOT NULL DEFAULT now()
);