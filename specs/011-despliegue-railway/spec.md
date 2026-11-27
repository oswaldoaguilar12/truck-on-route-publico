# Spec 011: Despliegue en Railway y seguridad básica (versión 2.15)

**Feature Branch**: `version-2.15` (sale de `version-2.14`) · **Created**: 2026-11-27 · **Status**: Aprobada (el equipo aprobó el plan el 2026-11-27: "lo demás ya trabájalo"; el ESP32 se deja para el final)

## Por qué

El profesor pidió el 2026-11-27 subir la base de datos, la API y el repositorio a Railway. Hoy todo corre en la computadora del equipo y la app necesita `adb reverse`. Al quedar en internet, la API queda expuesta, así que se toman los puntos de la revisión de seguridad del mismo día que hacen falta antes de publicarla.

## Historias

1. **Como profesor**, abro el panel y la app desde cualquier lugar con una dirección pública, sin instalar nada.
2. **Como equipo**, los usuarios, rutas, chat y fotos no se pierden cuando Railway vuelve a desplegar la API.
3. **Como ciudadano**, veo solo ubicaciones reales del camión: nadie de fuera puede mandar ubicaciones falsas.
4. **Como administrador**, nadie puede entrar al panel con las contraseñas de ejemplo que están en el repositorio.

## Requisitos

- **FR-1101**: La API (con el panel web) se publica en Railway desde el repositorio de GitHub, con la carpeta `api` como raíz, y responde en la dirección HTTPS que da Railway.
- **FR-1102**: Las ubicaciones se guardan en el MySQL de Railway. Si las tablas `camiones` y `ubicaciones` no existen, la API las crea al conectarse. La conexión se puede dar con `DB_URL` (una sola dirección `mysql://…`) o con `DB_HOST`, `DB_USER`, etc. Si MySQL falla, la API sigue en memoria, como siempre.
- **FR-1103**: `tor-datos.json` y los archivos del chat se guardan en la carpeta que diga `DATOS_DIR` (en Railway, un Volume). Sin `DATOS_DIR` se usa `api/datos`, como antes.
- **FR-1104**: Si existe `CLAVE_GPS`, `POST /ubicacion` exige la cabecera `X-Clave-GPS` con esa clave; si falta o no coincide responde 401 `{ "error": "..." }`. Sin `CLAVE_GPS` funciona como antes (para trabajar en local). `simular.js` manda la clave si la tiene.
- **FR-1105**: Con `PRODUCCION=1`, la API no arranca si `ADMIN_CONTRASENA` falta, tiene menos de 8 caracteres o es una de las contraseñas de ejemplo; y la primera vez solo crea la cuenta del administrador y el camión `CAMION-01` (sin el chofer ni la ciudadana de ejemplo).
- **FR-1106**: `npm audit` no marca vulnerabilidades altas en las dependencias de la API.
- **FR-1107**: Hay una guía paso a paso (`docs/railway.md`) para crear el proyecto en Railway, el MySQL, el Volume y las variables, y para apuntar la app y el panel a la nueva dirección.

## Criterios de éxito

- **SC-1101**: Las pruebas (`npm test`) pasan, incluidas las nuevas de clave del GPS, modo producción y `DATOS_DIR`.
- **SC-1102**: En Railway, el panel abre por HTTPS, se inicia sesión como administrador y la app (compilada con la dirección de Railway) muestra el camión simulado.
- **SC-1103**: Después de volver a desplegar, los usuarios y rutas creados siguen ahí.

## Fuera de alcance (por ahora)

- ESP32 con la dirección de Railway y la clave del GPS (se hace al final, con el equipo del GPS; el SIM800L batalla con HTTPS).
- Postgres, límite de intentos de inicio de sesión y los demás puntos de prioridad media y baja de la revisión de seguridad.
