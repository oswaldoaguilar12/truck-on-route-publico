// La aplicación Express de Truck on Rute, separada de server.js para poder probarla.
// Contrato: specs/001-ubicacion-tiempo-real/contracts/api-ubicacion.md
// Funciones nuevas (spec 002): historial, estadísticas, colonias, camiones y panel web.
// Versión 2.8 (spec 004): cuentas con rol, administrador, chofer y avisos (rutas-v3.js).

const path = require('node:path');
const crypto = require('node:crypto');
const express = require('express');
const { validar } = require('./validar');
const { buscarColonias } = require('./colonias');
const { recorridoKm, velocidadKmh } = require('./geo');
const db = require('./db');
const { crearDatos } = require('./datos');
const { crearRutasV3 } = require('./rutas-v3');

const MAX_HISTORIAL = 500; // puntos que se guardan en memoria por camión
const SIN_SENAL_MS = 60_000; // igual que la app: más de 60 s = sin señal reciente

// opciones.archivoDatos: dónde se guardan usuarios, camiones y recorridos (null = solo memoria, para pruebas)
function crearApp(opciones = {}) {
  const app = express();
  const inicio = new Date();

  // Memoria: última ubicación general y el historial de cada camión
  let ultimaUbicacion = null;
  const historiales = new Map(); // clave del camión -> [ubicaciones]

  // Datos de la Fase 3: se leen del archivo JSON y, la primera vez, se crean los de ejemplo
  const datos = crearDatos(opciones.archivoDatos);
  const produccion = opciones.produccion ?? process.env.PRODUCCION === '1';
  if (datos.sembrar({ produccion })) {
    console.log(produccion
      ? 'Primera vez (producción): se creó solo la cuenta del administrador y CAMION-01.'
      : 'Primera vez: se crearon los datos de ejemplo (ver LEEME.md, "Cuentas de ejemplo").');
  }

  // v2.14: 8 MB para que quepan fotos y audios de 5 MB en base64 (spec 010)
  app.use(express.json({ limit: '8mb' }));
  // Permite que el panel web se abra desde otra dirección (por ejemplo, otro puerto)
  app.use((req, res, next) => {
    res.set('Access-Control-Allow-Origin', '*');
    res.set('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Clave-GPS');
    res.set('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    if (req.method === 'OPTIONS') return res.sendStatus(204);
    next();
  });
  app.use('/panel', express.static(path.join(__dirname, 'public')));
  // v2.12 (spec 008, FR-805): aviso de privacidad como página web, sin pedir sesión
  app.get('/privacidad', (req, res) => res.sendFile(path.join(__dirname, 'public', 'privacidad.html')));
  // Leaflet (la librería del mapa) se sirve desde node_modules: el panel no depende de un CDN
  app.use('/panel/leaflet', express.static(path.join(__dirname, 'node_modules', 'leaflet', 'dist')));

  app.get('/', (req, res) => {
    res.json({
      mensaje: 'API de Truck on Rute funcionando',
      version: '2.8',
      baseDeDatos: db.activa() ? 'MySQL' : 'memoria',
      rutas: [
        'POST /ubicacion', 'GET /ubicacion', 'GET /ubicacion/historial?limite=50',
        'GET /camiones', 'GET /estadisticas', 'GET /colonias?buscar=zaca', 'GET /panel', 'GET /privacidad',
        'POST /sesion', 'POST /usuarios', 'GET /avisos', 'GET /recorridos/en-curso', '/admin/*', '/chofer/*',
      ],
    });
  });

  // v2.15 (spec 011, FR-1104): si hay CLAVE_GPS, solo el ESP32 o simular.js con esa clave pueden mandar ubicaciones
  const claveGps = opciones.claveGps ?? process.env.CLAVE_GPS ?? '';
  function claveCorrecta(recibida) {
    if (!claveGps) return true; // sin clave configurada funciona como antes (trabajo en local)
    const a = Buffer.from(String(recibida || ''));
    const b = Buffer.from(claveGps);
    return a.length === b.length && crypto.timingSafeEqual(a, b);
  }

  // El ESP32 (o simular.js) envía una ubicación
  app.post('/ubicacion', async (req, res) => {
    if (!claveCorrecta(req.get('X-Clave-GPS'))) return res.status(401).json({ error: 'Falta la clave del GPS o no es correcta' });
    const resultado = validar(req.body);
    if (!resultado.ok) return res.status(400).json({ error: resultado.error });

    const ubicacion = { ...resultado.ubicacion, fecha: new Date().toISOString() };
    ultimaUbicacion = ubicacion;
    const lista = historiales.get(ubicacion.camion) || [];
    lista.push(ubicacion);
    if (lista.length > MAX_HISTORIAL) lista.shift();
    historiales.set(ubicacion.camion, lista);

    if (db.activa()) {
      try {
        await db.guardarUbicacion(ubicacion);
      } catch (err) {
        console.error('No se pudo guardar en MySQL:', err.message);
      }
    }
    console.log(`Ubicación recibida: ${ubicacion.camion} ${ubicacion.lat}, ${ubicacion.lng} (${ubicacion.fuente})`);
    res.status(200).json(ubicacion);
  });

  // Última ubicación (de un camión si se pide ?camion=CAMION-01)
  app.get('/ubicacion', (req, res) => {
    const clave = req.query.camion;
    const u = clave ? (historiales.get(clave) || []).at(-1) : ultimaUbicacion;
    if (!u) return res.status(404).json({ error: 'Todavía no hay ubicación' });
    res.json(u);
  });

  // Recorrido: los últimos puntos, del más viejo al más nuevo (R7)
  app.get('/ubicacion/historial', async (req, res) => {
    const limite = Math.min(Math.max(parseInt(req.query.limite, 10) || 50, 1), MAX_HISTORIAL);
    const clave = req.query.camion || ultimaUbicacion?.camion;
    if (!clave) return res.json([]);
    if (db.activa()) {
      try {
        return res.json(await db.historial(clave, limite));
      } catch (err) {
        console.error('No se pudo leer el historial de MySQL:', err.message);
      }
    }
    res.json((historiales.get(clave) || []).slice(-limite));
  });

  // Lista de camiones con su última ubicación y si están en línea
  app.get('/camiones', (req, res) => {
    const ahora = Date.now();
    const lista = [...historiales.entries()].map(([camion, puntos]) => {
      const u = puntos.at(-1);
      return { camion, ultima: u, enLinea: ahora - new Date(u.fecha) <= SIN_SENAL_MS, puntos: puntos.length };
    });
    res.json(lista);
  });

  // Números para el panel: puntos, distancia, velocidad, tiempo encendida
  app.get('/estadisticas', async (req, res) => {
    const clave = req.query.camion || ultimaUbicacion?.camion;
    const puntos = clave ? historiales.get(clave) || [] : [];
    const n = puntos.length;
    const velocidad = n >= 2 ? velocidadKmh(puntos[n - 2], puntos[n - 1]) : null;
    let guardadasEnBD = null;
    if (db.activa()) {
      try { guardadasEnBD = await db.totalGuardadas(); } catch { /* sin dato */ }
    }
    res.json({
      camion: clave || null,
      puntosRecibidos: n,
      distanciaKm: Number(recorridoKm(puntos).toFixed(3)),
      velocidadKmh: velocidad === null ? null : Number(velocidad.toFixed(1)),
      ultimaFecha: puntos.at(-1)?.fecha || null,
      enLinea: n > 0 && Date.now() - new Date(puntos.at(-1).fecha) <= SIN_SENAL_MS,
      camiones: historiales.size,
      baseDeDatos: db.activa() ? 'MySQL' : 'memoria',
      guardadasEnBD,
      encendidaDesde: inicio.toISOString(),
    });
  });

  // Colonias de Huauchinango, con búsqueda sin acentos (R2)
  app.get('/colonias', (req, res) => {
    res.json(buscarColonias(req.query.buscar || ''));
  });

  // Versión 2.8: sesión, administrador, chofer y avisos
  // v2.13 (spec 009): las rutas v3 leen el historial para pintar de gris lo recorrido
  app.use(crearRutasV3(datos, { historialDe: (clave) => historiales.get(clave) || [] }));

  app.use((req, res) => res.status(404).json({ error: 'Ruta no encontrada' }));

  app.use((err, req, res, next) => {
    if (err.type === 'entity.parse.failed') return res.status(400).json({ error: 'El cuerpo debe ser JSON' });
    console.error('Error inesperado:', err.message);
    res.status(500).json({ error: 'Error interno de la API' });
  });

  return app;
}

module.exports = { crearApp };
