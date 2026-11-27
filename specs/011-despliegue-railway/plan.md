# Plan: Despliegue en Railway y seguridad básica (versión 2.15)

**Spec**: `specs/011-despliegue-railway/spec.md` · **Rama**: `version-2.15`

## Revisión de la constitución (v4.5.0)

- **I. Alcance**: la enmienda 4.5.0 agregó la Fase 7 (Railway) y quitó "nube" de fuera de alcance. El ESP32 queda pendiente, como pidió el equipo. ✔
- **II. Guardado**: misma regla de siempre; en Railway la carpeta de datos es un Volume (`DATOS_DIR`). MySQL sigue opcional. ✔
- **III. Partir de lo que existe**: no cambia la app ni el panel; solo la dirección de la API. ✔
- **IV. Seguro**: clave del GPS, modo producción, HTTPS de Railway, `npm audit fix`. MySQL con un usuario que no es `root` (`truck_app`, se crea con la guía). Sin librerías nuevas. ✔
- **V. Pruebas**: `node --test` para la clave del GPS, el modo producción y `DATOS_DIR`. ✔

## Cómo se hace

### API
- `app.js`: si `process.env.CLAVE_GPS` existe, `POST /ubicacion` compara la cabecera `X-Clave-GPS` con `timingSafeEqual`; 401 si no coincide. `crearApp` acepta `opciones.claveGps` para probarlo.
- `datos.js`: `sembrar({ produccion })` solo crea administrador y `CAMION-01` en producción. El archivo por defecto sale de `DATOS_DIR` si existe.
- `server.js`: con `PRODUCCION=1` revisa `ADMIN_CONTRASENA` antes de arrancar y explica en español qué falta.
- `db.js`: acepta `DB_URL`; después de conectarse crea `camiones` y `ubicaciones` con `CREATE TABLE IF NOT EXISTS` (las mismas columnas que `db/esquema.sql`).
- `simular.js`: manda `X-Clave-GPS` si `CLAVE_GPS` está en `.env`.
- `package.json`: `engines.node >= 20.12` (por `process.loadEnvFile`), para que Railway use un Node que sirva.
- `npm audit fix` (body-parser).

### Railway (lo hace el equipo con la guía)
- Proyecto nuevo desde GitHub, raíz `api`, comando `npm start`.
- MySQL de Railway; se crea el usuario `truck_app` con permisos solo sobre la base.
- Volume montado en `/app/datos` y `DATOS_DIR=/app/datos`.
- Variables: `PRODUCCION=1`, `ADMIN_CORREO`, `ADMIN_CONTRASENA`, `CLAVE_GPS`, `DB_URL`.
- Dominio público de Railway (Settings > Networking > Generate Domain).

### App
- `local.properties`: `API_URL=https://<dominio>.up.railway.app` y volver a compilar. Con HTTPS ya no hace falta tocar `network_security_config.xml` ni `adb reverse`.
