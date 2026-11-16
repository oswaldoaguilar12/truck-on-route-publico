# Contrato: API de ubicación

Este es el "acuerdo" entre las tres partes del sistema: **ESP32 → API → app Android**. Si las tres respetan este formato, cada una se puede programar y probar por separado.

## 1. Ubicación (el dato principal)

| Campo | Tipo | Obligatorio | Regla | Ejemplo |
|---|---|---|---|---|
| `camion` | texto | Sí | No vacío, máximo 20 caracteres | `"CAMION-01"` |
| `lat` | número | Sí | Entre -90 y 90. En Huauchinango ≈ 20.1 | `20.1750` |
| `lng` | número | Sí | Entre -180 y 180. En Huauchinango ≈ -98.0 | `-98.0510` |
| `fuente` | texto | Sí | Solo `"gps"` o `"simulacion"` | `"gps"` |
| `fecha` | texto (ISO 8601, UTC) | La pone la API | Hora en que la API recibió el dato. Si el ESP32 la manda, se ignora. | `"2026-10-14T17:00:00Z"` |

- `lat` y `lng` deben ser **números**, no texto: `20.175` es válido, `"20.175"` no.
- Se envían con al menos 6 decimales (≈ 10 cm de precisión), aunque el GPS no sea tan preciso.
- Campos extra que lleguen se ignoran.

## 2. `POST /ubicacion` — el ESP32 envía

**Encabezado:** `Content-Type: application/json`

**Cuerpo:**

```json
{
  "camion": "CAMION-01",
  "lat": 20.1750,
  "lng": -98.0510,
  "fuente": "gps"
}
```

**Respuestas:**

| Caso | Código | Cuerpo |
|---|---|---|
| Datos válidos, se guardó | `200` | La ubicación guardada, con `fecha` |
| Falta un campo | `400` | `{ "error": "Falta el campo lat" }` |
| `lat` o `lng` fuera de rango o no es número | `400` | `{ "error": "lat debe ser un número entre -90 y 90" }` |
| `fuente` no es `gps` ni `simulacion` | `400` | `{ "error": "fuente debe ser gps o simulacion" }` |
| El cuerpo no es JSON | `400` | `{ "error": "El cuerpo debe ser JSON" }` |

Cuando responde 400, **no se guarda nada** y la última ubicación anterior sigue igual.

## 3. `GET /ubicacion` — la app consulta

Devuelve la **última** ubicación recibida.

| Caso | Código | Cuerpo |
|---|---|---|
| Hay al menos una ubicación | `200` | `{ "camion": "CAMION-01", "lat": 20.1750, "lng": -98.0510, "fuente": "gps", "fecha": "2026-10-14T17:00:00Z" }` |
| Todavía no llega ninguna | `404` | `{ "error": "Todavía no hay ubicación" }` |

## 4. Reglas de la app al usar el dato

- Consulta `GET /ubicacion` cada 10 segundos mientras la pantalla de Inicio está visible.
- Si la API no responde en 5 segundos, cuenta como "no responde".
- Si `fuente` es `"simulacion"`, muestra el texto "Simulación". Si es `"gps"`, muestra "Ubicación real".
- Muestra la hora de `fecha`. Si tiene más de 60 segundos, muestra "Sin señal reciente".
- Si recibe 404 o la API no responde, muestra "Sin ubicación disponible" y no inventa una posición.

## 5. Direcciones de la API según dónde se prueba

| Desde | URL |
|---|---|
| La misma computadora (curl, navegador) | `http://localhost:3000` |
| Emulador de Android Studio | `http://10.0.2.2:3000` |
| Celular o ESP32 en la misma red WiFi | `http://<IP de la computadora>:3000` (ver con `ipconfig`) |
| SIM800L o celular con datos móviles | URL pública HTTP (túnel tipo ngrok o hosting) |

## 6. Pruebas rápidas con curl

```sh
# Válido → 200
curl -X POST http://localhost:3000/ubicacion -H "Content-Type: application/json" -d "{\"camion\":\"CAMION-01\",\"lat\":20.1750,\"lng\":-98.0510,\"fuente\":\"gps\"}"

# Sin lat → 400
curl -X POST http://localhost:3000/ubicacion -H "Content-Type: application/json" -d "{\"camion\":\"CAMION-01\",\"lng\":-98.0510,\"fuente\":\"gps\"}"

# lat fuera de rango → 400
curl -X POST http://localhost:3000/ubicacion -H "Content-Type: application/json" -d "{\"camion\":\"CAMION-01\",\"lat\":200,\"lng\":-98.0510,\"fuente\":\"gps\"}"

# Consultar la última
curl http://localhost:3000/ubicacion
```

(Las comillas con `\"` son para que funcione en la terminal de Windows.)

## 7. Pensado para después

Este formato ya permite, sin cambiarlo, agregar más adelante:
- **Historial (R7):** cada ubicación ya trae camión y fecha, así que se puede guardar en una lista o base de datos.
- **Tiempo estimado de llegada (R4):** con dos ubicaciones y sus fechas se puede calcular la velocidad.
- **Varios camiones:** el campo `camion` ya los distingue.
