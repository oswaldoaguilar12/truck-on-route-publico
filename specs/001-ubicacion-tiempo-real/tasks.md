# Tasks: Ubicación del camión en tiempo real

**Input**: documentos de `/specs/001-ubicacion-tiempo-real/`
**Prerequisites**: plan.md (✓), spec.md (✓), research.md (✓), data-model.md (✓), contracts/ (✓), quickstart.md (✓)
**Tests**: incluidos para la validación de la API (regla 11). El resto se valida con la prueba de cada etapa.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede hacer en paralelo (archivos distintos, sin dependencias)
- **[Story]**: historia de usuario (US1..US4)
- **Etapa**: la etapa del prompt maestro, para el resumen de Jira

---

## Phase 1: Setup

- [X] T001 Crear carpetas `firmware/` y `api/` en la raíz de TruckOnRute2.5
- [X] T002 Crear `.gitignore` en la raíz con `node_modules/`, `.env`, `secrets.h`, `local.properties`, `build/`, `.gradle/`
- [X] T003 [P] Inicializar `api/package.json` (nombre `truck-on-rute-api`, scripts `start`, `test`, `simular`) e instalar `express`
- [X] T004 [P] Crear `firmware/secrets.h.example` con `WIFI_SSID`, `WIFI_PASSWORD`, `API_URL`, `APN` vacíos y comentados

---

## Phase 2: Foundational (Etapa 0 y Etapa 2: la API)

**⚠️ Ninguna historia empieza sin la API**, porque todas envían o leen de ella.

- [ ] T005 Etapa 0: llenar la tabla de `hardware-conexiones.md` y las versiones de `diagnostico.md`; el equipo aprueba
- [X] T006 [P] Implementar `validar(cuerpo)` en `api/validar.js`: devuelve `{ ok: true, ubicacion }` o `{ ok: false, error }` según `contracts/api-ubicacion.md`
- [X] T007 [P] Tests de `validar` en `api/test/validar.test.js`: válido, falta `lat`, `lat: 200`, `lat` como texto, `fuente` inválida, `camion` vacío
- [X] T008 Implementar `api/server.js`: `POST /ubicacion` (usa `validar`, agrega `fecha`, guarda en memoria) y `GET /ubicacion` (200 o 404); errores como `{ error }`
- [X] T009 Mensaje claro si el puerto está ocupado (`EADDRINUSE`) en `api/server.js`
- [X] T010 Implementar `api/simular.js`: envía las 6 coordenadas de ejemplo con `"fuente": "simulacion"`, una cada 10 s, en bucle
- [X] T011 Validar con los curl de `contracts/api-ubicacion.md` (sección 6) y anotar el código de cada respuesta — probado por Claude y confirmado por Oswaldo en su computadora (2026-10-22)

**Checkpoint (Etapa 2)**: la API cumple el contrato; `npm test` en verde (11 pruebas). ✅ Confirmado en la computadora del equipo.

---

## Phase 3: User Story 1 - El dispositivo reporta su ubicación real (P1)

**Goal**: el ESP32 envía la ubicación real por WiFi cada 10 s.
**Independent Test**: `GET /ubicacion` muestra `"fuente": "gps"` y cambia al moverse.

- [X] T012 [P] [US1] Etapa 1: `firmware/gps_prueba/gps_prueba.ino`: lee el NEO-6M por Serial2 (9600) con TinyGPSPlus e imprime `valido`, satélites, lat y lng cada segundo
- [ ] T013 [US1] Etapa 1: prueba en cielo abierto; comparar una coordenada con Google Maps (< ~20 m)
- [X] T014 [US1] Etapa 3: `firmware/envio_wifi/envio_wifi.ino`: conecta al WiFi y cada 10 s hace el POST solo si el dato es válido; imprime el código de respuesta o el error
- [ ] T015 [US1] Etapa 3: prueba con la API encendida y apagada (el ESP32 no se bloquea)

**Checkpoint**: US1 funciona de forma independiente.

---

## Phase 4: User Story 2 - El ciudadano ve el camión en el mapa (P1) — Etapa 4

**Goal**: mapa en Inicio que sigue al camión.
**Independent Test**: con `npm run simular`, el camión se mueve en la app.

- [X] T016 [US2] Agregar el permiso `INTERNET` en `app-android/app/src/main/AndroidManifest.xml`
- [X] T017 [P] [US2] Crear `res/xml/network_security_config.xml` que permite HTTP solo para la dirección de la API, y referenciarlo en el manifest
- [X] T018 [US2] En `app/build.gradle.kts`: dependencia del mapa (ver `research.md`), leer `API_URL` de `local.properties` hacia `BuildConfig` (OpenStreetMap no necesita clave)
- [X] T019 [P] [US2] Crear `data/Ubicacion.kt` (data class del `data-model.md`)
- [X] T020 [P] [US2] Crear `data/UbicacionApi.kt`: `suspend fun obtenerUltima(): Ubicacion?` con `HttpURLConnection` en `Dispatchers.IO`, timeout 5 s; `null` si 404 o error
- [X] T021 [US2] Crear `ui/TruckMapSection.kt`: mapa centrado en Huauchinango, marcador con `camion.png`, consulta cada 10 s con `LaunchedEffect`
- [X] T022 [US2] En `ui/HomeScreen.kt`, cambiar `TruckCardSection()` por `TruckMapSection()` (se quita el texto fijo "llega en 23 min")
- [ ] T023 [US2] Borrar el archivo vacío `ui/TruckCard.kt` (con permiso del equipo)
- [ ] T024 [US2] Probar los pasos 2 y 3 de `quickstart.md`, incluida la API apagada
  - *Nota 2026-11-02: T116 (spec 002) confirma que la app compila y muestra la simulación en el emulador. Falta registrar la prueba con la API apagada; se marca cuando el equipo lo confirme (pasos 8 y 9 de `DEMO.md`).*

**Checkpoint**: US2 funciona con la simulación, sin hardware. Código escrito; falta compilarlo y probarlo en Android Studio (T024).

---

## Phase 5: User Story 4 - Distinguir dato real de simulación (P3) — Etapa 4

**Goal**: la app dice si es real o simulado y de cuándo.
**Independent Test**: cambiar `fuente` y ver el texto.

- [X] T025 [US4] En `TruckMapSection.kt`, mostrar debajo del mapa los estados de la tabla de `data-model.md` (Cargando, Real, Simulación, Sin señal reciente, Sin ubicación)
- [ ] T026 [US4] Probar: simulación → "Simulación"; GPS real → "Ubicación real"; detener envíos 70 s → "Sin señal reciente"
  - *Nota 2026-11-02: "Ubicación real" depende de T013/T015 (GPS real).*

**Checkpoint**: US2 + US4 completas = el mapa de la app está terminado.

---

## Phase 6: User Story 3 - Envío por red celular (P2) — Etapas 5, 6 y 7

**Goal**: la ubicación llega sin WiFi.
**Independent Test**: WiFi apagado, la app muestra la ubicación real.

- [ ] T027 [US3] Etapa 5: propuesta de batería y regulador con costo y diagrama; medir 3.7–4.2 V en el SIM800L; 15 min sin USB
- [ ] T028 [US3] Etapa 6: confirmar operador, APN y cobertura 2G (resuelve la aclaración pendiente del spec)
- [ ] T029 [P] [US3] Etapa 6: `firmware/sim800l_at/sim800l_at.ino`: pasa comandos del monitor serial al SIM800L; probar `AT`, `AT+CPIN?`, `AT+CSQ`, `AT+CREG?` y conexión GPRS
- [ ] T030 [US3] Etapa 7: publicar la API en internet por HTTP y actualizar `API_URL` en la app
- [ ] T031 [US3] Etapa 7: `firmware/envio_celular/envio_celular.ino`: mismo POST que T014 pero por SIM800L
- [ ] T032 [US3] Etapa 7: prueba con el WiFi apagado y el celular con datos móviles

**Checkpoint**: todas las historias funcionan.

---

## Phase N: Polish

- [ ] T033 [P] `README.md` de `api/` y de `firmware/` con cómo correr cada parte
  - *Nota 2026-11-02: `api/README.md` ya existe; falta el de `firmware/`.*
- [X] T034 [P] Cambiar los datos personales de ejemplo de `PerfilScreen.kt` por datos ficticios (con permiso del equipo; no cambia el comportamiento). También se cambiaron los de `GestionUsariosScreen.kt`.
- [ ] T035 Correr el `quickstart.md` completo
- [ ] T036 Resumen para Jira de cada etapa terminada

---

## Dependencies & Execution Order

- **Phase 1** → sin dependencias.
- **Phase 2 (API)** → depende de Phase 1; bloquea todas las historias.
- **US1 (Phase 3)** y **US2 + US4 (Phases 4–5)** → se pueden hacer en paralelo después de la API: una persona con el ESP32, otra con Android Studio.
- **US3 (Phase 6)** → depende de US1 (reutiliza el sketch de envío).
- **Polish** → al final.

## Implementation Strategy

### MVP primero

1. Phase 1 + Phase 2 → la API funciona.
2. Phase 4 (US2) con la simulación → ya se puede presentar el mapa en la app.
3. Phase 3 (US1) → el camión es real.
4. Parar y validar con el `quickstart.md`.

### Entrega incremental

API → mapa con simulación → GPS real por WiFi → estados real/simulación → baterías → SIM800L → red celular.
