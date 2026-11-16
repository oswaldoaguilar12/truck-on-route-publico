// Tests de validar() con el módulo de pruebas que ya trae Node.
// Ejecutar con: npm test

const test = require('node:test');
const assert = require('node:assert');
const { validar } = require('../validar');

const valida = { camion: 'CAMION-01', lat: 20.175, lng: -98.051, fuente: 'gps' };

test('acepta una ubicación válida', () => {
  const r = validar(valida);
  assert.strictEqual(r.ok, true);
  assert.deepStrictEqual(r.ubicacion, valida);
});

test('acepta fuente "simulacion"', () => {
  assert.strictEqual(validar({ ...valida, fuente: 'simulacion' }).ok, true);
});

test('ignora campos extra, incluida una fecha enviada por el ESP32', () => {
  const r = validar({ ...valida, fecha: '2000-01-01T00:00:00Z', otro: 1 });
  assert.strictEqual(r.ok, true);
  assert.deepStrictEqual(r.ubicacion, valida);
});

test('rechaza si falta lat', () => {
  const { lat, ...sinLat } = valida;
  assert.deepStrictEqual(validar(sinLat), { ok: false, error: 'Falta el campo lat' });
});

test('rechaza lat fuera de rango (200)', () => {
  assert.deepStrictEqual(validar({ ...valida, lat: 200 }), {
    ok: false,
    error: 'lat debe ser un número entre -90 y 90',
  });
});

test('rechaza lat como texto', () => {
  assert.strictEqual(validar({ ...valida, lat: '20.175' }).ok, false);
});

test('rechaza lng fuera de rango', () => {
  assert.deepStrictEqual(validar({ ...valida, lng: -181 }), {
    ok: false,
    error: 'lng debe ser un número entre -180 y 180',
  });
});

test('rechaza una fuente inválida', () => {
  assert.deepStrictEqual(validar({ ...valida, fuente: 'otro' }), {
    ok: false,
    error: 'fuente debe ser gps o simulacion',
  });
});

test('rechaza camion vacío', () => {
  assert.deepStrictEqual(validar({ ...valida, camion: '' }), {
    ok: false,
    error: 'Falta el campo camion',
  });
});

test('rechaza camion de más de 20 caracteres', () => {
  assert.strictEqual(validar({ ...valida, camion: 'X'.repeat(21) }).ok, false);
});

test('rechaza un cuerpo que no es objeto', () => {
  assert.deepStrictEqual(validar(null), { ok: false, error: 'El cuerpo debe ser JSON' });
  assert.deepStrictEqual(validar([1, 2]), { ok: false, error: 'El cuerpo debe ser JSON' });
});
