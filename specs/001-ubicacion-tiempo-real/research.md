# Research: Ubicación del camión en tiempo real

## Decision: el mapa va dentro de la app Android

**Rationale**: el código original es una app Android; "la app de Truck" es esa. Poner el mapa en la pantalla de Inicio cumple el criterio del acta de llegar en máximo 3 pasos.
**Alternatives considered**: página web con Leaflet (lo que suponía el prompt maestro) — sería una segunda interfaz que el ciudadano no usa.

## Decision: OpenStreetMap con osmdroid como mapa (decidido 2026-10-22)

**Rationale**: gratis y sin clave ni tarjeta, así que el equipo puede probar de inmediato. Se usa dentro de Compose con `AndroidView`. Versión: `org.osmdroid:osmdroid-android:6.1.20`.
**Imágenes del mapa**: los servidores de OpenStreetMap (`tile.openstreetmap.org`) respondieron "Access blocked" (403) y CARTO pidió clave ("API key required"), así que se usa "World Street Map" de Esri (`server.arcgisonline.com`), sin clave, con el crédito "Esri, HERE, Garmin, © OpenStreetMap contributors".
**Alternative descartada**: Google Maps para Compose (ver abajo), porque pide una clave de Google Cloud con cuenta de facturación.

## Opción descartada: Google Maps para Compose (`maps-compose`)

**Rationale**: está hecho para Jetpack Compose (la app ya usa Compose), tiene buena documentación y ejemplos en español, y el mapa en Android no tiene costo por uso.
**Cuidado**: necesita una clave de API de Google Cloud, y crearla pide una cuenta de facturación. La clave va en `local.properties`, nunca en el código.
**Alternatives considered**: osmdroid (OpenStreetMap, sin clave, pero no es Compose y hay que envolverlo con `AndroidView`); MapLibre (sin clave, más configuración). **Si el equipo no puede crear la clave, se usa osmdroid**; el resto del plan no cambia.

## Decision: `HttpURLConnection` + `org.json` en la app

**Rationale**: solo hay una petición (GET cada 10 s). Ambas clases vienen con Android, así que no se agregan librerías (regla 9).
**Alternatives considered**: Retrofit + Gson (más limpio para muchas peticiones, pero son dos librerías y más conceptos para explicar).

## Decision: consulta periódica con `LaunchedEffect` + `delay(10_000)`

**Rationale**: la consulta solo corre mientras Inicio está en pantalla y se detiene sola al salir. Es la forma más simple en Compose.
**Alternatives considered**: WebSockets o Firebase en tiempo real (fuera de alcance: requieren decidir nube).

## Decision: permitir HTTP solo para la dirección de la API

**Rationale**: Android 9+ bloquea HTTP sin cifrar. El SIM800L funciona mejor con HTTP, así que la API se publica por HTTP. Un `network_security_config.xml` permite HTTP solo para ese dominio o IP.
**Alternatives considered**: `usesCleartextTraffic="true"` para toda la app (más fácil, menos seguro).

## Decision: Express en memoria, un solo archivo

**Rationale**: dos rutas y una variable con la última ubicación. Express es lo que el equipo ya usó.
**Alternatives considered**: `http` nativo de Node (más código para leer JSON); base de datos (fuera de alcance).

## Decision: validación como función pura con tests `node --test`

**Rationale**: `validar(cuerpo)` no depende de Express, así que se prueba fácil. `node --test` viene con Node (como Vitest en `task-cli`, pero sin instalar nada).

## Decision: URL de la API configurable

**Rationale**: cambia según dónde se prueba: `10.0.2.2:3000` desde el emulador, la IP de la computadora en la misma red WiFi, o la URL pública para el SIM800L. En la app se lee de `local.properties` (`API_URL`) mediante `BuildConfig`; en el ESP32 de `secrets.h`.

## Decision: script de simulación en la API

**Rationale**: permite probar la app (etapa 4) antes de tener GPS real. Envía las 6 coordenadas de ejemplo con `"fuente": "simulacion"`, una cada 10 s, en bucle.
