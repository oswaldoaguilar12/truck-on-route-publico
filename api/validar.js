// Valida el cuerpo de un POST /ubicacion según contracts/api-ubicacion.md.
// Es una función pura: no usa Express ni guarda nada, por eso es fácil de probar.
//
// Devuelve:
//   { ok: true,  ubicacion: { camion, lat, lng, fuente } }  si todo está bien
//   { ok: false, error: "mensaje" }                          si algo falla

const FUENTES_VALIDAS = ['gps', 'simulacion'];

function esNumero(valor) {
  // typeof evita aceptar texto como "20.17"; isFinite evita NaN e Infinity
  return typeof valor === 'number' && Number.isFinite(valor);
}

function validar(cuerpo) {
  // El cuerpo debe ser un objeto JSON (no null, no un arreglo, no texto)
  if (cuerpo === null || typeof cuerpo !== 'object' || Array.isArray(cuerpo)) {
    return { ok: false, error: 'El cuerpo debe ser JSON' };
  }

  // Campos obligatorios, en el orden del contrato
  for (const campo of ['camion', 'lat', 'lng', 'fuente']) {
    if (cuerpo[campo] === undefined || cuerpo[campo] === null || cuerpo[campo] === '') {
      return { ok: false, error: `Falta el campo ${campo}` };
    }
  }

  const { camion, lat, lng, fuente } = cuerpo;

  if (typeof camion !== 'string' || camion.trim() === '' || camion.length > 20) {
    return { ok: false, error: 'camion debe ser un texto de 1 a 20 caracteres' };
  }
  if (!esNumero(lat) || lat < -90 || lat > 90) {
    return { ok: false, error: 'lat debe ser un número entre -90 y 90' };
  }
  if (!esNumero(lng) || lng < -180 || lng > 180) {
    return { ok: false, error: 'lng debe ser un número entre -180 y 180' };
  }
  if (!FUENTES_VALIDAS.includes(fuente)) {
    return { ok: false, error: 'fuente debe ser gps o simulacion' };
  }

  // Solo se copian los campos del contrato; los extra se ignoran
  return { ok: true, ubicacion: { camion: camion.trim(), lat, lng, fuente } };
}

module.exports = { validar };
