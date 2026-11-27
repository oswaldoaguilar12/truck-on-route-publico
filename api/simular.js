// Simulación: envía a la API una ubicación del camión cada 10 segundos.
// Sirve para probar la app Android sin tener el GPS.
// Versión 2.13: si el recorrido en curso del camión tiene un camino dibujado en el panel,
// el camión simulado sigue ese camino (y el desvío, si hay). Si no, usa las 6 coordenadas de ejemplo.
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
const CAMION = 'CAMION-01';
const { densificar, distanciaKm } = require('./geo');
// En el camino dibujado avanza 2 puntos (unos 40 m) por envío: cerca de 15 km/h cada 10 s
const PUNTOS_POR_ENVIO = 2;

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

// ---------- Versión 2.13: seguir el camino dibujado ----------
let camino = null;     // lista de [lat, lng] que se está siguiendo
let firma = '';        // para saber si el administrador cambió el camino o puso un desvío
let paso = 0;          // en qué punto del camino va
let ultimo = null;     // último punto enviado
let alFinal = false;   // para avisar una sola vez que llegó al final

// Punto del camino más cercano a "p"
function masCercano(lista, p) {
  let mejor = 0, menor = Infinity;
  lista.forEach(([lat, lng], i) => {
    const d = distanciaKm({ lat, lng }, { lat: p[0], lng: p[1] });
    if (d < menor) { menor = d; mejor = i; }
  });
  return mejor;
}

// Pregunta a la API si el camión tiene un recorrido en curso con camino dibujado
async function buscarCamino() {
  try {
    const lista = await (await fetch(`${API_URL}/recorridos/en-curso`)).json();
    const rec = lista.find((r) => r.camion === CAMION && r.trazo?.length >= 2);
    if (!rec) return null;
    let nuevo = rec.trazo;
    // Con desvío: va por el camino normal hasta donde empieza el desvío, lo sigue y regresa al camino normal
    if (rec.desvio?.length >= 2) {
      const desde = masCercano(rec.trazo, rec.desvio[0]);
      const hasta = masCercano(rec.trazo, rec.desvio.at(-1));
      nuevo = [...rec.trazo.slice(0, desde + 1), ...densificar(rec.desvio), ...rec.trazo.slice(hasta)];
    }
    return { puntos: nuevo, firma: JSON.stringify([rec.id, rec.trazo.length, rec.trazo[0], rec.desvio]) };
  } catch {
    return null;
  }
}

// El siguiente punto a enviar: del camino dibujado o, si no hay, de las coordenadas de ejemplo
async function siguientePunto() {
  const c = await buscarCamino();
  if (c) {
    if (c.firma !== firma) {
      // Camino nuevo o con desvío: sigue desde el punto más cercano a donde iba
      paso = ultimo && camino ? masCercano(c.puntos, ultimo) : 0;
      if (camino) console.log(c.firma.includes('null]') ? 'Se cambió el camino.' : 'Hay desvío: el camión lo va a seguir.');
      else console.log('Siguiendo el camino dibujado en el panel.');
      firma = c.firma;
    }
    camino = c.puntos;
    const punto = camino[Math.min(paso, camino.length - 1)];
    if (paso >= camino.length - 1 && !alFinal) console.log('El camión llegó al final del camino. Termina la ruta en la app del chofer.');
    alFinal = paso >= camino.length - 1;
    paso = Math.min(paso + PUNTOS_POR_ENVIO, camino.length - 1);
    return { punto, texto: `camino ${Math.min(paso, camino.length)}/${camino.length}` };
  }
  if (camino) { console.log('Ya no hay camino dibujado en curso: vuelvo a las coordenadas de ejemplo.'); camino = null; firma = ''; }
  const punto = RECORRIDO[indice];
  const texto = `${indice + 1}/${RECORRIDO.length}`;
  indice = (indice + 1) % RECORRIDO.length; // al llegar al final vuelve a empezar
  return { punto, texto };
}

async function enviarSiguiente() {
  const { punto, texto } = await siguientePunto();
  const [lat, lng] = [+punto[0].toFixed(6), +punto[1].toFixed(6)];
  ultimo = [lat, lng];

  try {
    const respuesta = await fetch(`${API_URL}/ubicacion`, {
      method: 'POST',
      // v2.15 (spec 011): la misma clave del GPS que tiene la API, si la hay
      headers: { 'Content-Type': 'application/json', ...(process.env.CLAVE_GPS ? { 'X-Clave-GPS': process.env.CLAVE_GPS } : {}) },
      body: JSON.stringify({ camion: CAMION, lat, lng, fuente: 'simulacion' }),
    });
    console.log(`Enviado punto ${texto} (${lat}, ${lng}) → ${respuesta.status}`);
    if (respuesta.status === 401) console.log('La API pide la clave del GPS: pon la misma CLAVE_GPS en tu .env');
  } catch {
    console.log(`No se pudo conectar con la API en ${API_URL}. ¿Está encendida (npm start)?`);
  }
}

console.log(`Simulando el camión hacia ${API_URL} cada ${INTERVALO_MS / 1000} s. Ctrl+C para detener.`);
enviarSiguiente();
setInterval(enviarSiguiente, INTERVALO_MS);
