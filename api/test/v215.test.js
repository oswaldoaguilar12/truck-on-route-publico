// Pruebas de la versión 2.15 (spec 011): clave del GPS, modo producción y DATOS_DIR.
// Ejecutar con: npm test

const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { crearApp } = require('../app');

// Levanta una API de prueba y devuelve { base, cerrar }
function levantar(opciones) {
  return new Promise((listo) => {
    const s = crearApp(opciones).listen(0, () => listo({ base: `http://localhost:${s.address().port}`, cerrar: () => s.close() }));
  });
}
const temporal = () => path.join(fs.mkdtempSync(path.join(os.tmpdir(), 'tor215-')), 'tor-datos.json');
const ubicacion = { camion: 'CAMION-01', lat: 20.175, lng: -98.051, fuente: 'gps' };
const mandar = (base, headers = {}) => fetch(`${base}/ubicacion`, {
  method: 'POST', headers: { 'Content-Type': 'application/json', ...headers }, body: JSON.stringify(ubicacion),
});

test('FR-1104: con CLAVE_GPS, POST /ubicacion pide la clave correcta', async () => {
  const { base, cerrar } = await levantar({ archivoDatos: temporal(), claveGps: 'clave-secreta-123' });
  try {
    const sinClave = await mandar(base);
    assert.strictEqual(sinClave.status, 401);
    assert.ok((await sinClave.json()).error);
    assert.strictEqual((await mandar(base, { 'X-Clave-GPS': 'otra' })).status, 401);
    assert.strictEqual((await mandar(base, { 'X-Clave-GPS': 'clave-secreta-123' })).status, 200);
  } finally { cerrar(); }
});

test('FR-1104: sin CLAVE_GPS funciona como antes', async () => {
  const { base, cerrar } = await levantar({ archivoDatos: temporal(), claveGps: '' });
  try {
    assert.strictEqual((await mandar(base)).status, 200);
  } finally { cerrar(); }
});

test('FR-1105: en producción no se crean las cuentas de ejemplo', async () => {
  const archivo = temporal();
  const { base, cerrar } = await levantar({ archivoDatos: archivo, produccion: true });
  try {
    const d = JSON.parse(fs.readFileSync(archivo, 'utf8'));
    assert.deepStrictEqual(d.usuarios.map((u) => u.rol), ['administrador']);
    assert.deepStrictEqual(d.camiones.map((c) => c.clave), ['CAMION-01']);
    const chofer = await fetch(`${base}/sesion`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo: 'chofer@truckonroute.mx', contrasena: 'Chofer2026' }),
    });
    assert.strictEqual(chofer.status, 401);
  } finally { cerrar(); }
});

test('FR-1105: con PRODUCCION=1 la API no arranca con una contraseña de ejemplo o corta', () => {
  for (const contrasena of ['', 'corta', 'Admin2026']) {
    const r = spawnSync(process.execPath, [path.join(__dirname, '..', 'server.js')], {
      env: { ...process.env, PRODUCCION: '1', ADMIN_CONTRASENA: contrasena, PORT: '0', DB_HOST: '', DB_URL: '' },
      encoding: 'utf8', timeout: 10_000,
    });
    assert.strictEqual(r.status, 1, `debió salir con 1 usando "${contrasena}"`);
    assert.match(r.stderr, /ADMIN_CONTRASENA/);
  }
});

test('FR-1103: DATOS_DIR cambia dónde se guarda tor-datos.json', () => {
  const carpeta = fs.mkdtempSync(path.join(os.tmpdir(), 'tor215-dir-'));
  const r = spawnSync(process.execPath, ['-e', `
    const { crearDatos } = require(${JSON.stringify(path.join(__dirname, '..', 'datos'))});
    crearDatos().sembrar();
  `], { env: { ...process.env, DATOS_DIR: carpeta }, encoding: 'utf8' });
  assert.strictEqual(r.status, 0, r.stderr);
  assert.ok(fs.existsSync(path.join(carpeta, 'tor-datos.json')));
});
