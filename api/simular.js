// Simulación: envía a la API las 6 coordenadas de ejemplo, una cada 10 segundos, en bucle.
// Sirve para probar la app Android sin tener el GPS.
// Las ubicaciones van con "fuente": "simulacion" para que nunca se confundan con las reales.
//
// Ejecutar con: npm run simular   (más rápido: en PowerShell $env:INTERVALO_MS=3000)
//   (la API debe estar encendida en otra terminal)

try {
  process.loadEnvFile();
} catch {
  // Sin .env: se usa la dirección por defecto
}

const API_URL = process.env.API_URL || 'http://localhost:3000';
const INTERVALO_MS = Number(process.env.INTERVALO_MS) || 10_000;

// Coordenadas de EJEMPLO (no son una ruta real), del prompt maestro
const RUTA = [
  [20.175, -98.051],
  [20.1758, -98.0502],
  [20.1765, -98.0494],
  [20.1773, -98.0485],
  [20.178, -98.0477],
  [20.1788, -98.0468],
];

// Entre cada par de puntos se agregan pasos intermedios para que el camión avance suave en el mapa
const PASOS_ENTRE_PUNTOS = 3;
const RECORRIDO = [];
for (let i = 0; i < RUTA.length - 1; i++) {
  const [lat1, lng1] = RUTA[i];
  const [lat2, lng2] = RUTA[i + 1];
  for (let p = 0; p < PASOS_ENTRE_PUNTOS; p++) {
    const t = p / PASOS_ENTRE_PUNTOS;
    RECORRIDO.push([+(lat1 + (lat2 - lat1) * t).toFixed(6), +(lng1 + (lng2 - lng1) * t).toFixed(6)]);
  }
}
RECORRIDO.push(RUTA.at(-1));
// De regreso por el mismo camino, para que el recorrido sea un ciclo de ida y vuelta
RECORRIDO.push(...RECORRIDO.slice(1, -1).reverse());

let indice = 0;

async function enviarSiguiente() {
  const [lat, lng] = RECORRIDO[indice];
  const numero = indice + 1;
  indice = (indice + 1) % RECORRIDO.length; // al llegar al final vuelve a empezar

  try {
    const respuesta = await fetch(`${API_URL}/ubicacion`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ camion: 'CAMION-01', lat, lng, fuente: 'simulacion' }),
    });
    console.log(`Enviado punto ${numero}/${RECORRIDO.length} (${lat}, ${lng}) → ${respuesta.status}`);
  } catch {
    console.log(`No se pudo conectar con la API en ${API_URL}. ¿Está encendida (npm start)?`);
  }
}

console.log(`Simulando el camión hacia ${API_URL} cada ${INTERVALO_MS / 1000} s. Ctrl+C para detener.`);
enviarSiguiente();
setInterval(enviarSiguiente, INTERVALO_MS);
