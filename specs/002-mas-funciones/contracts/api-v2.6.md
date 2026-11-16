# Contrato API v2.6 (lo nuevo)

Lo de la versión 2.5 (`POST /ubicacion`, `GET /ubicacion`) no cambia. Ver `specs/001-ubicacion-tiempo-real/contracts/api-ubicacion.md`.

| Método y ruta | Respuesta 200 |
|---|---|
| `GET /` | `{ mensaje, version: "2.6", baseDeDatos: "MySQL" o "memoria", rutas: [...] }` |
| `GET /ubicacion?camion=CAMION-01` | Última ubicación de ese camión (404 si no hay) |
| `GET /ubicacion/historial?limite=50&camion=CAMION-01` | `[{ camion, lat, lng, fuente, fecha }, ...]` del más viejo al más nuevo; `[]` si no hay |
| `GET /camiones` | `[{ camion, ultima, enLinea, puntos }]` |
| `GET /estadisticas` | `{ camion, puntosRecibidos, distanciaKm, velocidadKmh (o null), ultimaFecha, enLinea, camiones, baseDeDatos, guardadasEnBD, encendidaDesde }` |
| `GET /colonias?buscar=zaca` | `["INFONAVIT Zacamila", "Lomas de Zacamila", "Zacamila"]` |
| `GET /panel` | Página web del administrador |
| Ruta que no existe | 404 `{ error }` |

Ejemplo de `/estadisticas`:
```json
{ "camion": "CAMION-01", "puntosRecibidos": 13, "distanciaKm": 0.842, "velocidadKmh": 18.4,
  "ultimaFecha": "2026-10-23T01:20:10.000Z", "enLinea": true, "camiones": 1,
  "baseDeDatos": "MySQL", "guardadasEnBD": 13, "encendidaDesde": "2026-10-23T01:18:00.000Z" }
```
