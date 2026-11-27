// Conexión OPCIONAL a MySQL (base truck_on_route de db/esquema.sql).
// Si en .env no hay DB_HOST, la API funciona solo en memoria, como antes.

let pool = null;

// v2.15 (spec 011, FR-1102): las dos tablas que usa la API, con las mismas columnas que db/esquema.sql.
// En Railway no se corre esquema.sql a mano: si no existen, se crean al conectarse.
const TABLAS = [
  `CREATE TABLE IF NOT EXISTS camiones (
    id_camion INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(20) NOT NULL, placas VARCHAR(15) NULL, modelo VARCHAR(60) NULL,
    capacidad_kg INT UNSIGNED NULL, activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_camiones_clave UNIQUE (clave)
  )`,
  `CREATE TABLE IF NOT EXISTS ubicaciones (
    id_ubicacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_camion INT UNSIGNED NOT NULL, id_recorrido INT UNSIGNED NULL,
    lat DECIMAL(9,6) NOT NULL, lng DECIMAL(9,6) NOT NULL,
    fuente ENUM('gps','simulacion') NOT NULL,
    fecha DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_ubicaciones_camion FOREIGN KEY (id_camion) REFERENCES camiones (id_camion) ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX ix_ubicaciones_camion_fecha (id_camion, fecha)
  )`,
];

async function conectar() {
  // DB_URL: una sola dirección mysql://usuario:contraseña@host:puerto/base (así la da Railway)
  if (!process.env.DB_HOST && !process.env.DB_URL) return null;
  try {
    const mysql = require('mysql2/promise');
    pool = process.env.DB_URL
      ? mysql.createPool({ uri: process.env.DB_URL, timezone: 'Z', connectionLimit: 5 })
      : mysql.createPool({
        host: process.env.DB_HOST,
        port: Number(process.env.DB_PORT || 3306),
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'truck_on_route',
        timezone: 'Z',
        connectionLimit: 5,
      });
    await pool.query('SELECT 1');
    for (const sql of TABLAS) await pool.query(sql);
    const [[{ base }]] = await pool.query('SELECT DATABASE() AS base');
    console.log(`Conectada a MySQL: base ${base}`);
    return pool;
  } catch (err) {
    console.error(`No se pudo conectar a MySQL (${err.message}). La API sigue funcionando en memoria.`);
    pool = null;
    return null;
  }
}

function activa() {
  return pool !== null;
}

// Busca el camión por su clave; si no existe lo da de alta. Devuelve su id.
async function idCamion(clave) {
  const [filas] = await pool.query('SELECT id_camion FROM camiones WHERE clave = ?', [clave]);
  if (filas.length > 0) return filas[0].id_camion;
  const [r] = await pool.query('INSERT INTO camiones (clave) VALUES (?)', [clave]);
  return r.insertId;
}

async function guardarUbicacion(u) {
  const id = await idCamion(u.camion);
  const fecha = u.fecha.replace('T', ' ').replace('Z', '');
  await pool.query('INSERT INTO ubicaciones (id_camion, lat, lng, fuente, fecha) VALUES (?, ?, ?, ?, ?)', [id, u.lat, u.lng, u.fuente, fecha]);
}

// Últimos puntos de un camión, del más viejo al más nuevo
async function historial(clave, limite) {
  const [filas] = await pool.query(
    `SELECT c.clave AS camion, u.lat, u.lng, u.fuente, u.fecha
       FROM ubicaciones u JOIN camiones c ON c.id_camion = u.id_camion
      WHERE c.clave = ? ORDER BY u.id_ubicacion DESC LIMIT ?`,
    [clave, limite]
  );
  return filas.reverse().map((f) => ({ camion: f.camion, lat: Number(f.lat), lng: Number(f.lng), fuente: f.fuente, fecha: new Date(f.fecha).toISOString() }));
}

async function totalGuardadas() {
  const [filas] = await pool.query('SELECT COUNT(*) AS n FROM ubicaciones');
  return Number(filas[0].n);
}

module.exports = { conectar, activa, guardarUbicacion, historial, totalGuardadas };
