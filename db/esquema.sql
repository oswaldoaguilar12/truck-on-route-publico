-- =====================================================================
-- Truck on Route 2.5 - Esquema de la base de datos (DDL)
-- Motor: MySQL 8 (también funciona en MariaDB 10.6 o más nuevo)
-- Cómo usarlo: en MySQL Workbench o phpMyAdmin, abre este archivo y ejecútalo completo.
-- Cubre los requisitos del acta: R1 usuarios, R2 rutas, R3/R9 ubicaciones,
-- R5 ruta y camión asignado, R6 notificaciones, R7 historial de recorridos, R8 administración.
-- =====================================================================

DROP DATABASE IF EXISTS truck_on_route;
CREATE DATABASE truck_on_route CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE truck_on_route;

-- Colonias de Huauchinango (las 83 del buscador de la app)
CREATE TABLE colonias (
  id_colonia     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  codigo_postal  CHAR(5)      NULL,
  CONSTRAINT uq_colonias_nombre UNIQUE (nombre)
);

-- Usuarios de la app: ciudadano, administrador o conductor (R1)
CREATE TABLE usuarios (
  id_usuario       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre           VARCHAR(100) NOT NULL,
  correo           VARCHAR(150) NOT NULL,
  contrasena_hash  VARCHAR(255) NOT NULL,          -- nunca la contraseña en texto plano
  rol              ENUM('ciudadano','administrador','conductor') NOT NULL DEFAULT 'ciudadano',
  id_colonia       INT UNSIGNED NULL,               -- colonia donde vive el ciudadano
  avisos_activos   BOOLEAN      NOT NULL DEFAULT TRUE,
  creado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_usuarios_correo UNIQUE (correo),
  CONSTRAINT fk_usuarios_colonia FOREIGN KEY (id_colonia)
    REFERENCES colonias (id_colonia) ON UPDATE CASCADE ON DELETE SET NULL
);

-- Camiones recolectores; "clave" es la que manda el GPS, por ejemplo CAMION-01
CREATE TABLE camiones (
  id_camion     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  clave         VARCHAR(20)  NOT NULL,
  placas        VARCHAR(15)  NULL,
  modelo        VARCHAR(60)  NULL,
  capacidad_kg  INT UNSIGNED NULL,
  activo        BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_camiones_clave UNIQUE (clave),
  CONSTRAINT uq_camiones_placas UNIQUE (placas)
);

-- Rutas de recolección (R2, R8)
CREATE TABLE rutas (
  id_ruta      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(80)  NOT NULL,
  descripcion  VARCHAR(255) NULL,
  activa       BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_rutas_nombre UNIQUE (nombre)
);

-- Qué colonias recorre cada ruta, en qué orden, qué día y a qué hora (relación N a N)
CREATE TABLE ruta_colonias (
  id_ruta         INT UNSIGNED NOT NULL,
  id_colonia      INT UNSIGNED NOT NULL,
  dia_semana      TINYINT UNSIGNED NOT NULL,        -- 1 = lunes ... 7 = domingo
  orden           SMALLINT UNSIGNED NOT NULL,       -- 1 = primera colonia de la ruta
  hora_estimada   TIME NULL,
  PRIMARY KEY (id_ruta, id_colonia, dia_semana),
  CONSTRAINT ck_ruta_colonias_dia CHECK (dia_semana BETWEEN 1 AND 7),
  CONSTRAINT fk_rc_ruta FOREIGN KEY (id_ruta)
    REFERENCES rutas (id_ruta) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_rc_colonia FOREIGN KEY (id_colonia)
    REFERENCES colonias (id_colonia) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Cada vez que un camión sale a hacer una ruta (R5, R7)
CREATE TABLE recorridos (
  id_recorrido  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_ruta       INT UNSIGNED NOT NULL,
  id_camion     INT UNSIGNED NOT NULL,
  id_conductor  INT UNSIGNED NULL,                  -- usuario con rol conductor
  inicio        DATETIME     NOT NULL,
  fin           DATETIME     NULL,                  -- vacío mientras está en curso
  estado        ENUM('programado','en_curso','terminado','cancelado') NOT NULL DEFAULT 'programado',
  CONSTRAINT ck_recorridos_fechas CHECK (fin IS NULL OR fin >= inicio),
  CONSTRAINT fk_recorridos_ruta FOREIGN KEY (id_ruta)
    REFERENCES rutas (id_ruta) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_recorridos_camion FOREIGN KEY (id_camion)
    REFERENCES camiones (id_camion) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_recorridos_conductor FOREIGN KEY (id_conductor)
    REFERENCES usuarios (id_usuario) ON UPDATE CASCADE ON DELETE SET NULL
);

-- Cada punto que manda el GPS (R3, R9) = mismo formato que POST /ubicacion de la API
CREATE TABLE ubicaciones (
  id_ubicacion  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_camion     INT UNSIGNED NOT NULL,
  id_recorrido  INT UNSIGNED NULL,                  -- vacío si no hay recorrido activo
  lat           DECIMAL(9,6) NOT NULL,
  lng           DECIMAL(9,6) NOT NULL,
  fuente        ENUM('gps','simulacion') NOT NULL,
  fecha         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),   -- hora de recepción (UTC)
  CONSTRAINT ck_ubicaciones_lat CHECK (lat BETWEEN -90 AND 90),
  CONSTRAINT ck_ubicaciones_lng CHECK (lng BETWEEN -180 AND 180),
  CONSTRAINT fk_ubicaciones_camion FOREIGN KEY (id_camion)
    REFERENCES camiones (id_camion) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_ubicaciones_recorrido FOREIGN KEY (id_recorrido)
    REFERENCES recorridos (id_recorrido) ON UPDATE CASCADE ON DELETE SET NULL,
  INDEX ix_ubicaciones_camion_fecha (id_camion, fecha)   -- para pedir rápido la última ubicación
);

-- Avisos al ciudadano, por ejemplo "el camión está cerca" (R6)
CREATE TABLE notificaciones (
  id_notificacion  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario       INT UNSIGNED NOT NULL,
  id_recorrido     INT UNSIGNED NULL,
  titulo           VARCHAR(80)  NOT NULL,
  mensaje          VARCHAR(255) NOT NULL,
  leida            BOOLEAN      NOT NULL DEFAULT FALSE,
  creada_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_notificaciones_recorrido FOREIGN KEY (id_recorrido)
    REFERENCES recorridos (id_recorrido) ON UPDATE CASCADE ON DELETE SET NULL
);

-- =====================================================================
-- Datos iniciales
-- =====================================================================

INSERT INTO colonias (nombre) VALUES
  ('14 de Octubre'),
  ('5 de Mayo'),
  ('5 de Octubre'),
  ('Acatlán'),
  ('Adolfo López Mateos'),
  ('Agua Blanca'),
  ('Ahuacatitla'),
  ('Antonio Amador Quintero'),
  ('Atzopic'),
  ('Bellavista'),
  ('Benito Juárez'),
  ('Carmen Serdán'),
  ('Catalina'),
  ('Centro'),
  ('Chapultepec'),
  ('Constitución'),
  ('Cruz Blanca'),
  ('Cruz del Milagro'),
  ('Cuacuila'),
  ('El Ahuacatal'),
  ('El Cerrito'),
  ('El Derrumbe'),
  ('El Llano'),
  ('El Mirador I'),
  ('El Mirador II'),
  ('El Ocotal'),
  ('El Ocote'),
  ('El Paraíso'),
  ('El Potro'),
  ('Electricista'),
  ('Emiliano Zapata'),
  ('FOVISSSTE'),
  ('FSTSE'),
  ('Francisco Villa'),
  ('Guadalupe'),
  ('Hortalizas'),
  ('INFONAVIT Zacamila'),
  ('Independencia'),
  ('Insurgentes'),
  ('La Aurora'),
  ('La Cumbre'),
  ('La Estrella'),
  ('La Herradura'),
  ('La Joya'),
  ('La Mesita'),
  ('La Morena'),
  ('La Palpa'),
  ('La Partada'),
  ('La Paz'),
  ('La Petrolera'),
  ('La Villa'),
  ('Las Flores'),
  ('Libertad'),
  ('Lomas de Bellavista'),
  ('Lomas de Chapultepec'),
  ('Lomas de Zacamila'),
  ('Lomas del Bosque'),
  ('Los Capulines'),
  ('Los Pinos'),
  ('Los Ángeles'),
  ('Luis Donaldo Colosio'),
  ('Miguel Hidalgo'),
  ('Monterrey'),
  ('Morelos'),
  ('Nuevo Milenio'),
  ('Piedras Pintadas'),
  ('Proyecto 2000'),
  ('Quetzalcoatl'),
  ('Rancho Viejo'),
  ('Rincón de la Cruz'),
  ('Ruiz Cortines'),
  ('Río Chiquito'),
  ('Santa Catarina'),
  ('Santa Cruz'),
  ('Santa Dora'),
  ('Santiago'),
  ('Santiago (Buena Vista)'),
  ('Sección 39'),
  ('Texcapa'),
  ('Valle Fénix'),
  ('Valles del Rincón'),
  ('Vima'),
  ('Zacamila');

INSERT INTO camiones (clave, modelo) VALUES ('CAMION-01', 'Camión recolector de prueba');
