# Data Model: Ubicación del camión en tiempo real

## Entidad: Ubicación

Un punto reportado por un camión.

| Campo | Tipo | Reglas |
|---|---|---|
| camion | texto | Obligatorio, 1 a 20 caracteres. En esta fase siempre `CAMION-01` |
| lat | número | Obligatorio, entre -90 y 90 (Huauchinango ≈ 20.17) |
| lng | número | Obligatorio, entre -180 y 180 (Huauchinango ≈ -98.05) |
| fuente | texto | `gps` o `simulacion` |
| fecha | texto ISO 8601 UTC | La pone la API al recibir; inmutable |

## Dónde vive en cada parte

| Parte | Representación |
|---|---|
| ESP32 | Variables `double lat, lng` de TinyGPSPlus; se arma el JSON al enviar |
| API | Una variable `ultimaUbicacion` (objeto o `null`) en memoria |
| App | `data class Ubicacion(val camion: String, val lat: Double, val lng: Double, val fuente: String, val fecha: String)` |

## Estados que muestra la app

| Estado | Cuándo | Texto |
|---|---|---|
| Cargando | Primera consulta en curso | "Buscando camión…" |
| Real | `fuente = gps` y `fecha` de hace 60 s o menos | "Ubicación real · 10:32" |
| Simulación | `fuente = simulacion` | "Simulación · 10:32" |
| Sin señal reciente | `fecha` de hace más de 60 s | "Sin señal reciente · 10:20" |
| Sin ubicación | 404, sin internet o API apagada | "Sin ubicación disponible" |

## Persistencia

Ninguna. Si la API se reinicia, `ultimaUbicacion` vuelve a `null` (la app mostrará "Sin ubicación disponible"). El campo `fecha` ya permite guardar un historial (R7) en una fase futura.
