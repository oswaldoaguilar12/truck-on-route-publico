# API de Truck on Rute (versión 2.6)

Recibe la ubicación del camión, guarda su historial (en memoria o en MySQL) y entrega datos para la app y el panel web.
Contratos: `../specs/001-ubicacion-tiempo-real/contracts/api-ubicacion.md` (2.5) y `../specs/002-mas-funciones/contracts/api-v2.6.md` (lo nuevo).

## Archivos

| Archivo | Qué hace |
|---|---|
| `server.js` | Arranca la API (lee `.env`, se conecta a MySQL si está configurado). |
| `app.js` | Todas las rutas: ubicación, historial, camiones, estadísticas, colonias y panel. |
| `db.js` | Guardado opcional en MySQL (`truck_on_route`, tabla `ubicaciones`). |
| `geo.js` | Distancia (Haversine), km recorridos y velocidad. |
| `colonias.js` | Las 83 colonias de Huauchinango y búsqueda sin acentos. |
| `validar.js` | Revisa que la ubicación cumpla el contrato. |
| `simular.js` | Simula el camión: 30 puntos por vuelta (ida y regreso) cada 10 s. |
| `public/` | Panel web del administrador (`/panel`). |
| `test/` | 19 pruebas (`validar` y rutas). |

## Cómo usarla (dentro de `api`)

```sh
npm install      # la primera vez y después de esta actualización (trae mysql2 y leaflet)
npm test         # deben pasar las 19 pruebas
npm start        # deja esta terminal abierta
npm run simular  # en otra terminal
```

Panel del administrador: **http://localhost:3000/panel**

## Rutas

| Ruta | Para qué |
|---|---|
| `POST /ubicacion` | El ESP32 o la simulación manda una ubicación |
| `GET /ubicacion` | Última ubicación |
| `GET /ubicacion/historial?limite=50` | Últimos puntos (la ruta que dibujan la app y el panel) |
| `GET /camiones` | Camiones y si están en línea |
| `GET /estadisticas` | Puntos, km recorridos, velocidad, si hay MySQL |
| `GET /colonias?buscar=zaca` | Buscar colonias |

## Guardar en MySQL (opcional)

1. Carga `../db/esquema.sql` en MySQL Workbench (crea la base `truck_on_route`).
2. Copia `.env.example` como `.env` y llena `DB_HOST=localhost`, `DB_USER=root` y **tu** contraseña en `DB_PASSWORD`.
3. `npm start`. Debe decir `Conectada a MySQL`. Si falla, sigue funcionando en memoria.
4. En Workbench: `SELECT * FROM ubicaciones ORDER BY id_ubicacion DESC;` muestra lo que va llegando.

Requisito: Node.js 20.12 o más nuevo (`node -v`).
