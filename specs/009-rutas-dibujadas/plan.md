# Plan: Rutas dibujadas (versión 2.13)

**Spec**: `specs/009-rutas-dibujadas/spec.md` · **Rama**: `version-2.13`

## Revisión de la constitución (v4.3.0)

- **I. Alcance**: la enmienda 4.3.0 agregó la Fase 5 (rutas dibujadas, gris y desvío). FR-912 es parte del desvío. ✔
- **II. Etapas con prueba**: API con `npm test`; panel y app los prueba el equipo en el emulador. ✔
- **III. Sencillo**: sin librerías nuevas. El panel usa Leaflet (ya está) y la app osmdroid (ya está). ✔
- **IV. Datos**: el trazo y el desvío son del camión y la ruta, no de personas. La ubicación del ciudadano sigue sin salir del celular. ✔
- **V. Estilo**: verde para lo que falta, gris para lo recorrido, ámbar punteado para el desvío. ✔

## Cómo se hace

### API
- `geo.js`: `trazoValido`, `densificar` (un punto cada 20 m) y `avanceEnTrazo` (radio 40 m, solo busca hacia adelante, así nunca regresa).
- `datos.js`: `guardarTrazo(ruta, trazo)` y `guardarDesvio(recorrido, trazo, motivo)`; al terminar el recorrido se borra el desvío.
- `rutas-v3.js`:
  - `PUT /admin/rutas/:id/trazo`.
  - `PUT /admin/recorridos/:id/desvio` (manda mensaje al chofer y crea aviso `desvio`) y `DELETE` para quitarlo.
  - `GET /recorridos/en-curso` agrega `trazo`, `avance`, `desvio` y `motivoDesvio`.
- `app.js`: pasa el historial a las rutas v3 y permite el método PUT en CORS.

### Panel web
- Vista "Rutas": botón "Dibujar camino" por ruta; mapa donde cada clic agrega un punto; botones Deshacer punto, Borrar todo y Guardar camino.
- Vista "Recorridos": en los que están en curso, botón "Desvío" con el mismo mapa y un campo de motivo.
- Mapa de monitoreo: dibuja el trazo gris/verde y el desvío ámbar.

### App
- `ApiTor`: leer `trazo`, `avance`, `desvio` y `motivoDesvio` del recorrido en curso.
- Mapa (`TruckMapSection`): línea gris (recorrido), verde (falta) y ámbar punteada (desvío).
- Chofer: aviso "Ruta cambiada por bloqueo" con el motivo.
- Avisos: el tipo `desvio` se muestra como notificación.
