// Cálculos de distancia y velocidad con coordenadas GPS.

const RADIO_TIERRA_KM = 6371;

// Distancia en km entre dos puntos {lat, lng} (fórmula de Haversine)
function distanciaKm(a, b) {
  const rad = (g) => (g * Math.PI) / 180;
  const dLat = rad(b.lat - a.lat);
  const dLng = rad(b.lng - a.lng);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
}

// Distancia total recorrida por una lista de puntos en orden
function recorridoKm(puntos) {
  let total = 0;
  for (let i = 1; i < puntos.length; i++) total += distanciaKm(puntos[i - 1], puntos[i]);
  return total;
}

// Velocidad en km/h entre dos puntos con su fecha (ISO). null si no se puede calcular.
function velocidadKmh(a, b) {
  const horas = (new Date(b.fecha) - new Date(a.fecha)) / 3_600_000;
  if (!(horas > 0)) return null;
  return distanciaKm(a, b) / horas;
}

// ---------------------------------------------------------------------------
// Versión 2.13 (spec 009): rutas dibujadas. Un trazo es una lista de [lat, lng].
// ---------------------------------------------------------------------------

// FR-903: un trazo válido tiene de 2 a 1000 puntos con coordenadas reales
function trazoValido(trazo) {
  if (!Array.isArray(trazo) || trazo.length < 2 || trazo.length > 1000) return false;
  return trazo.every((p) => Array.isArray(p) && p.length === 2 &&
    typeof p[0] === 'number' && typeof p[1] === 'number' &&
    p[0] >= -90 && p[0] <= 90 && p[1] >= -180 && p[1] <= 180);
}

// Agrega puntos intermedios para que no haya más de "pasoM" metros entre dos puntos.
// Así el gris avanza poco a poco aunque el administrador haya dado clics muy separados.
function densificar(trazo, pasoM = 20) {
  const salida = [];
  for (let i = 0; i < trazo.length - 1; i++) {
    const [aLat, aLng] = trazo[i];
    const [bLat, bLng] = trazo[i + 1];
    const metros = distanciaKm({ lat: aLat, lng: aLng }, { lat: bLat, lng: bLng }) * 1000;
    const partes = Math.max(1, Math.ceil(metros / pasoM));
    for (let k = 0; k < partes; k++) {
      const t = k / partes;
      salida.push([aLat + (bLat - aLat) * t, aLng + (bLng - aLng) * t]);
    }
  }
  salida.push(trazo[trazo.length - 1]);
  return salida;
}

// FR-905: cuántos puntos del trazo ya recorrió el camión.
// Un punto cuenta cuando alguna ubicación queda a menos de "radioM" metros.
// Solo se busca hacia adelante, así el avance nunca regresa aunque el GPS brinque.
function avanceEnTrazo(trazo, ubicaciones, radioM = 40) {
  let avance = 0;
  for (const u of ubicaciones) {
    for (let j = avance; j < trazo.length; j++) {
      const metros = distanciaKm({ lat: trazo[j][0], lng: trazo[j][1] }, u) * 1000;
      if (metros < radioM) { avance = j + 1; break; }
    }
  }
  return avance;
}

module.exports = { distanciaKm, recorridoKm, velocidadKmh, trazoValido, densificar, avanceEnTrazo };
