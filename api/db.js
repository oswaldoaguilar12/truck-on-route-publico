// Conexión OPCIONAL a MySQL (base truck_on_route de db/esquema.sql).
// Si en .env no hay DB_HOST, la API funciona solo en memoria, como antes.

let pool = null;

async function conectar() {
  if (!process.env.DB_HOST) return null;
  try {
    const mysql = require('mysql2/promise');
    pool = mysql.createPool({
      host: process.env.DB_HOST,
      port: Number(process.env.DB_PORT || 3306),
      user: process.env.DB_USER || 'root',
      password: process.env.DB_PASSWORD || '',
      database: process.env.DB_NAME || 'truck_on_route',
      timezone: 'Z',
      connectionLimit: 5,
    });
    await pool.query('SELECT 1');
    console.log(`Conectada a MySQL: base ${process.env.DB_NAME || 'truck_on_route'}`);
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
