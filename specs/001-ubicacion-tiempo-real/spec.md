# Feature Specification: Ubicación del camión en tiempo real

**Feature Branch**: `001-ubicacion-tiempo-real`
**Created**: 2026-10-22
**Status**: Implementada en software (API, app y simulación). Pendiente: pruebas del GPS real (US1) y red celular (US3). Actualizado 2026-11-02
**Cubre del acta**: R3 (ubicación en tiempo real en un mapa) y R9 (actualización periódica durante un recorrido)
**Input**: ver `spec-input.md`

## Clarifications

### Session 2026-10-22

- Q: ¿Dónde está la "app" que debe mostrar el camión? → A: Es la app Android original (Kotlin + Compose). El mapa va dentro de ella, en la pantalla de Inicio.
- Q: ¿Existe una API o firmware anteriores? → A: No vienen en el código original. Se crean nuevos en `api/` y `firmware/`.
- Q: ¿Cuántos camiones? → A: Uno, `CAMION-01`.
- Q: ¿Qué hace la app si la última ubicación tiene más de 60 s? → A: Sigue mostrando el camión con el aviso "Sin señal reciente".
- Q: ¿Qué mapa se usa en la app? → A: OpenStreetMap con osmdroid (gratis, sin clave). Decidido por Oswaldo.
- Q: ¿Qué pasa con el texto fijo "El camión llega en 23 min"? → A: Se quita, porque el tiempo de llegada (R4) está fuera de esta fase y ese dato es falso. En su lugar se muestra el estado de la ubicación.

### Pendientes

- [NEEDS CLARIFICATION: operador del chip del SIM800L, su APN y si hay cobertura 2G en Huauchinango. Se comprueba en la historia 3.]

## User Scenarios & Testing *(mandatory)*

### User Story 1 - El dispositivo reporta su ubicación real (Priority: P1)

El dispositivo del camión (ESP32 + GPS NEO-6M) lee su posición y la envía a la API cada 10 segundos por WiFi.

**Why this priority**: sin ubicación real no hay nada que mostrar.

**Independent Test**: con el GPS en cielo abierto, `GET /ubicacion` en el navegador muestra `"fuente": "gps"` con coordenadas que cambian al moverse.

**Acceptance Scenarios**:

1. **Given** el GPS con dato válido, **When** pasan 10 s, **Then** el ESP32 envía `POST /ubicacion` con camión, lat, lng y `"fuente": "gps"`, y la API responde 200.
2. **Given** el GPS sin dato válido, **When** pasan 10 s, **Then** el ESP32 no envía nada e imprime que espera señal.
3. **Given** la API apagada, **When** el ESP32 intenta enviar, **Then** imprime el error y reintenta en el siguiente ciclo sin bloquearse.

---

### User Story 2 - El ciudadano ve el camión en el mapa de la app (Priority: P1)

Al entrar a Inicio, el ciudadano ve un mapa de Huauchinango con el camión, que se mueve solo.

**Why this priority**: es lo que ve el usuario final y lo que pide R3. Se puede probar sin hardware, con la simulación.

**Independent Test**: con la API encendida y el script de simulación corriendo, el ícono se mueve en el mapa de la app.

**Acceptance Scenarios**:

1. **Given** una ubicación en la API, **When** el ciudadano entra a Inicio, **Then** ve el mapa centrado en el camión.
2. **Given** el mapa abierto, **When** llega una ubicación nueva, **Then** el ícono se mueve en máximo 20 s sin salir de la pantalla.
3. **Given** que la API no tiene ubicación (404) o no responde, **When** se abre Inicio, **Then** se ve "Sin ubicación disponible" y ningún punto falso; la app no se cierra.

---

### User Story 3 - El envío funciona por red celular (Priority: P2)

El ESP32 usa el SIM800L para enviar la ubicación sin WiFi, como en el camión real.

**Why this priority**: en la calle no hay WiFi, pero el sistema se puede demostrar primero con WiFi.

**Independent Test**: con el WiFi apagado, la app (con datos móviles) muestra la ubicación real actualizada.

**Acceptance Scenarios**:

1. **Given** el SIM800L registrado en la red y la API publicada en internet por HTTP, **When** pasan 10 s, **Then** la ubicación llega a la API.
2. **Given** que el SIM800L pierde señal, **When** intenta enviar, **Then** el ESP32 lo indica y reintenta en el siguiente ciclo.

---

### User Story 4 - Distinguir dato real de simulación (Priority: P3)

Quien usa la app sabe si ve una ubicación real o simulada, y de cuándo es.

**Why this priority**: evita presentar datos de prueba como reales; no bloquea lo demás.

**Independent Test**: enviar una ubicación con `"fuente": "simulacion"` y comprobar el texto de la app.

**Acceptance Scenarios**:

1. **Given** `"fuente": "simulacion"`, **When** se muestra, **Then** aparece "Simulación".
2. **Given** `"fuente": "gps"`, **When** se muestra, **Then** aparece "Ubicación real" y la hora de la última actualización.
3. **Given** una ubicación de hace más de 60 s, **When** se muestra, **Then** aparece "Sin señal reciente".

### Edge Cases

- El GPS da 0, 0 (sin fix): el ESP32 no envía.
- POST con `lat`/`lng` fuera de rango, como texto o faltantes: 400, no se guarda nada.
- Puerto 3000 ocupado (`EADDRINUSE`): la API explica cómo liberarlo.
- El celular sin internet: la app muestra "Sin ubicación disponible" y reintenta.
- Android bloquea HTTP sin cifrar por defecto: la app debe permitirlo solo para la dirección de la API.
- El SIM800L se reinicia al transmitir: casi siempre es alimentación (ver `hardware-conexiones.md`).

## Requirements *(mandatory)*

### Functional Requirements

Escritos en formato **EARS** (Easy Approach to Requirements Syntax) el 2026-11-02. Los Id y lo que pide cada requisito no cambiaron; la redacción anterior está en `respaldos/TruckOnRute2.6_antes-de-auditoria_2026-11-02.zip`.

| Patrón EARS | Plantilla |
|---|---|
| Ubicuo | El `<sistema>` deberá `<respuesta>`. |
| Por evento | Cuando `<disparador>`, el `<sistema>` deberá `<respuesta>`. |
| Por estado | Mientras `<estado>`, el `<sistema>` deberá `<respuesta>`. |
| No deseado | Si `<condición no deseada>`, entonces el `<sistema>` deberá `<respuesta>`. |
| Opcional | Donde `<característica>`, el `<sistema>` deberá `<respuesta>`. |

| Id | Patrón | Requisito (EARS) | Acta |
|---|---|---|---|
| FR-001 | Ubicuo | El ESP32 deberá leer el GPS NEO-6M por UART2 a 9600 baudios. | R3 |
| FR-002 | Por evento + No deseado | Cuando pasen 10 s y el GPS tenga un dato válido, el ESP32 deberá enviar la ubicación a la API. Si el GPS no tiene dato válido, entonces el ESP32 no deberá enviar nada y deberá imprimir que espera señal. | R9 |
| FR-003 | Ubicuo | El ESP32, la API y la app deberán usar el formato de `contracts/api-ubicacion.md`. | R3 |
| FR-004 | Por evento + No deseado | Cuando la API reciba un `POST /ubicacion` válido, la API deberá guardarlo y responder 200. Si el cuerpo es inválido, entonces la API deberá responder 400 con `{ "error" }` y no guardar nada. | R3 |
| FR-005 | Por evento | Cuando la API guarde una ubicación, la API deberá agregarle `fecha` con la hora de recepción en UTC. | R9 (prepara R7) |
| FR-006 | Por evento + No deseado | Cuando la app pida `GET /ubicacion`, la API deberá devolver la última ubicación. Si todavía no hay ubicación, entonces la API deberá responder 404 con `{ "error" }`. | R3 |
| FR-007 | Por estado | Mientras la pantalla de Inicio esté visible, la app deberá mostrar el mapa y consultar `GET /ubicacion` cada 10 s. | R3, R9 |
| FR-008 | Por estado | Mientras haya una ubicación, la app deberá mostrar "Ubicación real" o "Simulación" según `fuente` y la hora; si la ubicación tiene más de 60 s, deberá mostrar "Sin señal reciente". | R3 |
| FR-009 | Ubicuo | La app deberá permitir al ciudadano llegar al mapa en máximo 3 pasos desde que la abre. | Criterio del acta |
| FR-010 | Opcional | Donde el ESP32 tenga SIM800L, el ESP32 deberá enviar por red celular con el mismo contrato que por WiFi. | R9 |
| FR-011 | Ubicuo | El proyecto deberá guardar contraseñas, APN, URL de la API y claves solo en archivos que no se suben a git (`secrets.h`, `.env`, `local.properties`). | — |
| FR-012 | Por evento | Cuando el equipo ejecute `npm run simular`, el script deberá enviar la ruta de ejemplo con `"fuente": "simulacion"`. | — |
| FR-013 | Ubicuo | La app deberá conservar sin cambios el comportamiento de login, registro, perfil y gestión en esta fase. | — |

### Key Entities

- **Ubicación**: un punto reportado por un camión: camión, latitud, longitud, fuente y fecha de recepción.
- **Camión**: identificado por un texto como `CAMION-01`; no se guarda más información en esta fase.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Con el GPS en cielo abierto, la coordenada cae a menos de ~20 m de la posición real.
- **SC-002**: Al mover el dispositivo, el ícono de la app se actualiza en máximo 20 s.
- **SC-003**: El dispositivo funciona 15 min seguidos con baterías, sin USB.
- **SC-004**: Con el WiFi del ESP32 apagado, la ubicación llega por red celular y se ve en la app.
- **SC-005**: Ninguna ubicación inválida queda guardada en la API.
- **SC-006**: La app no se cierra cuando la API está apagada o el celular no tiene internet.

## Assumptions

- Login actual: acepta cualquier correo y contraseña no vacíos. Se deja así; abrir la app, iniciar sesión e Inicio son 2 pasos hasta el mapa.
- La ubicación vive en memoria; si la API se reinicia, se pierde. Aceptable en esta fase.
- El primer fix del GPS puede tardar varios minutos y necesita cielo abierto.
- El SIM800L es 2G; si no hay 2G en Huauchinango, se documenta como cambio sin comprar nada.
- Para la red celular, la API se publica en internet por HTTP (por ejemplo con un túnel tipo ngrok), porque el SIM800L tiene problemas con HTTPS.
