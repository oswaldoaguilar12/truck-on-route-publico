# Truck on Rute Constitution

## Core Principles

### I. Alcance acotado

Solo la cadena GPS NEO-6M → ESP32 → (WiFi, luego SIM800L) → API Node.js → mapa en la app Android. Fuera de alcance: carrito con motores, panel solar, MPU6050, AHT20-BMP280, login real, ETA, notificaciones, historial, administración real, base de datos y nube (estas dos requieren preguntar al equipo).

### II. Tres partes separadas y un contrato

`firmware/` (C++ con Arduino IDE, TinyGPSPlus por Serial2 a 9600), `api/` (Node.js LTS + Express, JavaScript, en memoria) y `app-android/` (la app original en Kotlin + Jetpack Compose). Todas usan el formato de `contracts/api-ubicacion.md`; cada ubicación lleva `fuente` (`gps` o `simulacion`).

### III. Partir de lo que existe

La app original se cambia solo lo necesario para mostrar el mapa. Login, registro, perfil y gestión conservan su comportamiento.

### IV. Sencillo, seguro y en español

Código corto, comentado en español, que el equipo pueda explicar en clase. Sin librerías extra para lo que se resuelve con pocas líneas. Sin secretos en git (`secrets.h`, `.env`, `local.properties`). Errores claros: la API responde `{ "error": "..." }` con 400/404; la app nunca se cierra por falta de datos.

### V. Una etapa a la vez, con prueba

La validación de la API tiene tests con `node --test`. Cada etapa del hardware tiene una prueba física. Una etapa termina solo cuando su prueba pasa y el equipo lo confirma; al terminar se escribe un resumen para Jira. El SIM800L nunca se alimenta del ESP32; no se compra nada sin comprobar que hace falta.

## Flujo de Desarrollo

- Especificaciones, planes y tareas con Spec Kit (`/speckit.specify`, `/speckit.plan`, `/speckit.tasks`, `/speckit.implement`).
- Documentos, comentarios y mensajes de commit en español; identificadores de código en inglés o español, consistentes.
- No borrar ni modificar archivos fuera de la carpeta TruckOnRute2.5.

## Governance

- Esta constitución prevalece sobre cualquier otra práctica del proyecto. Su origen es `reglas.md`.
- Las enmiendas se documentan con fecha y motivo (MAJOR: quitar o redefinir un principio; MINOR: agregar uno; PATCH: redacción).

**Version**: 1.0.0 | **Ratified**: 2026-10-22 | **Last Amended**: 2026-10-22
