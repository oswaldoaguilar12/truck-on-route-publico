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

module.exports = { distanciaKm, recorridoKm, velocidadKmh };
