// Pruebas de las rutas de la API (spec 002). Levantan la app en un puerto libre.
// Ejecutar con: npm test

const test = require('node:test');
const assert = require('node:assert');
const { crearApp } = require('../app');

let servidor;
let base;

test.before(async () => {
  await new Promise((listo) => {
    servidor = crearApp({ archivoDatos: null }).listen(0, () => {
      base = `http://localhost:${servidor.address().port}`;
      listo();
    });
  });
});
test.after(() => servidor.close());

const enviar = (cuerpo) =>
  fetch(`${base}/ubicacion`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(cuerpo) });

test('GET /ubicacion da 404 antes de recibir datos', async () => {
  const r = await fetch(`${base}/ubicacion`);
  assert.strictEqual(r.status, 404);
});

test('POST /ubicacion guarda y agrega la fecha', async () => {
  const r = await enviar({ camion: 'CAMION-01', lat: 20.175, lng: -98.051, fuente: 'simulacion' });
  assert.strictEqual(r.status, 200);
  const u = await r.json();
  assert.ok(u.fecha);
});

test('POST /ubicacion rechaza una latitud fuera de rango', async () => {
  const r = await enviar({ camion: 'CAMION-01', lat: 200, lng: -98.051, fuente: 'gps' });
  assert.strictEqual(r.status, 400);
});

test('el historial guarda los puntos en orden', async () => {
  await enviar({ camion: 'CAMION-01', lat: 20.1758, lng: -98.0502, fuente: 'simulacion' });
  const h = await (await fetch(`${base}/ubicacion/historial?limite=10`)).json();
  assert.strictEqual(h.length, 2);
  assert.strictEqual(h[1].lat, 20.1758);
});

test('las estadísticas calculan la distancia recorrida', async () => {
  const e = await (await fetch(`${base}/estadisticas`)).json();
  assert.strictEqual(e.puntosRecibidos, 2);
  assert.ok(e.distanciaKm > 0.1 && e.distanciaKm < 0.2, `distancia ${e.distanciaKm}`);
  assert.strictEqual(e.enLinea, true);
});

test('GET /camiones lista el camión en línea', async () => {
  const c = await (await fetch(`${base}/camiones`)).json();
  assert.strictEqual(c.length, 1);
  assert.strictEqual(c[0].camion, 'CAMION-01');
  assert.strictEqual(c[0].enLinea, true);
});

test('GET /colonias busca sin importar acentos', async () => {
  const c = await (await fetch(`${base}/colonias?buscar=angeles`)).json();
  assert.ok(c.includes('Los Ángeles'));
  const todas = await (await fetch(`${base}/colonias`)).json();
  assert.strictEqual(todas.length, 83);
});

test('el panel web se sirve en /panel', async () => {
  const r = await fetch(`${base}/panel/`);
  assert.strictEqual(r.status, 200);
  assert.match(await r.text(), /Truck on Route/);
});
