# Implementation Plan: Ubicación del camión en tiempo real

**Branch**: `001-ubicacion-tiempo-real` | **Date**: 2026-10-22 | **Spec**: [spec.md](spec.md)

## Summary

Conectar un GPS real a la app Android de Truck on Rute. El ESP32 lee el NEO-6M y envía la ubicación a una API Node.js pequeña (en memoria); la app consulta la API cada 10 s y mueve el camión en un mapa dentro de la pantalla de Inicio. Primero por WiFi, después por SIM800L.

## Technical Context

| Parte | Tecnología | Por qué (ver `research.md`) |
|---|---|---|
| Firmware | C++ con Arduino IDE, `TinyGPSPlus`, `WiFi.h`, `HTTPClient.h` | Ya se usa Arduino IDE; TinyGPSPlus es la librería estándar del NEO-6M |
| API | Node.js LTS + Express, JavaScript | Es lo que el equipo ya usó; un solo archivo es suficiente |
| Pruebas API | `node --test` | Viene con Node, no hay que instalar nada |
| App | Kotlin + Jetpack Compose (la app original) | Ya existe |
| Mapa en la app | OpenStreetMap con osmdroid 6.1.20 | Gratis, sin clave (ver `research.md`) |
| HTTP en la app | `HttpURLConnection` + `org.json` (incluidos en Android) | Sin librerías extra para una sola petición GET |
| Almacenamiento | Memoria de la API | La base de datos está fuera de esta fase |

**Plataformas**: ESP32 (DevKit), Node.js en la computadora del equipo, Android 7.0+ (minSdk 24 de la app original).
**Rendimiento**: actualización visible en máximo 20 s (envío cada 10 s + consulta cada 10 s).
**Escala**: 1 camión, pocos usuarios de prueba.

## Constitution Check

- 1 Alcance: ✅ solo GPS → ESP32 → API → mapa.
- 2 Estructura en tres carpetas: ✅ `firmware/`, `api/`, `app-android/`.
- 3 Firmware con TinyGPSPlus en Serial2: ✅
- 4 API Node + Express en memoria: ✅
- 5 Cambios mínimos en la app: ✅ solo Inicio, manifest y build (ver lista abajo).
- 6 Contrato: ✅ `contracts/api-ubicacion.md`.
- 7 Fuente gps / simulacion: ✅
- 8 Sin secretos en git: ✅ `secrets.h`, `.env`, `local.properties` en `.gitignore`.
- 11 Tests de la validación con `node --test`: ✅

Todas las reglas se cumplen.

## Project Structure

### Documentation

```text
specs/001-ubicacion-tiempo-real/
├── spec.md
├── plan.md                 # este archivo
├── research.md
├── data-model.md
├── quickstart.md
├── hardware-conexiones.md
├── contracts/api-ubicacion.md
├── checklists/requirements.md
└── tasks.md
```

### Source Code

```text
firmware/
├── gps_prueba/gps_prueba.ino         # Etapa 1: solo lee el GPS
├── envio_wifi/envio_wifi.ino         # Etapa 3: GPS + POST por WiFi
├── sim800l_at/sim800l_at.ino         # Etapa 6: comandos AT
├── envio_celular/envio_celular.ino   # Etapa 7: GPS + POST por SIM800L
└── secrets.h.example                 # plantilla; el real (secrets.h) no se sube

api/
├── package.json
├── server.js            # Express: POST y GET /ubicacion
├── validar.js           # función pura que valida el cuerpo
├── simular.js           # envía la ruta de ejemplo con fuente "simulacion"
├── .env.example
└── test/validar.test.js

app-android/             # la app original
└── app/src/main/
    ├── AndroidManifest.xml                    # + permiso INTERNET, + config de red
    ├── res/xml/network_security_config.xml    # nuevo: permite HTTP solo a la API
    └── java/com/example/truckonroute/
        ├── data/Ubicacion.kt                  # nuevo: data class
        ├── data/UbicacionApi.kt               # nuevo: GET /ubicacion
        └── ui/
            ├── TruckMapSection.kt             # nuevo: mapa + estado
            └── HomeScreen.kt                  # cambia TruckCardSection por TruckMapSection
```

**Cambios en la app original** (y nada más): `AndroidManifest.xml`, `app/build.gradle.kts` (dependencia del mapa y la URL de la API), `HomeScreen.kt` (una línea) y los archivos nuevos de arriba. `TruckCard.kt` vacío se reemplaza por `TruckMapSection.kt`.

## Orden de etapas

| Etapa | Qué | Historia |
|---|---|---|
| 0 | Diagnóstico | — |
| 1 | GPS da coordenadas reales | US1 |
| 2 | API cumple el contrato (+ simulación) | Base |
| 3 | ESP32 envía por WiFi | US1 |
| 4 | Mapa en la app con la simulación | US2, US4 |
| 5 | Baterías | US3 |
| 6 | SIM800L responde a comandos AT | US3 |
| 7 | Envío por red celular | US3 |

Las etapas 1, 2 y 4 se pueden repartir entre personas distintas: no dependen del mismo hardware.
