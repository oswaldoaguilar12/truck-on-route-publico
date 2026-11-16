# Bitácora de auditoría SDD · 2026-11-02

Registro de cómo se revisó Truck on Rute 2.5/2.6 contra el ciclo de Spec Kit, qué se encontró y qué se cambió. Se deja como referencia de cómo fue trabajando el equipo.

**Informe completo:** documento "Auditoría MVP · Truck on Rute 2.5" (en el proyecto de Claude).
**Respaldo antes de los cambios:** `../respaldos/TruckOnRute2.6_antes-de-auditoria_2026-11-02.zip`

## Cómo trabajamos (línea de tiempo)

| Fecha | Qué pasó |
|---|---|
| 2026-10-22 | Diagnóstico de la app original (solo visual). Reglas → constitución v1.0.0 → spec 001 → clarify (6 preguntas) → plan → tasks (36). API probada (11 pruebas) y mapa en la app con simulación. |
| 2026-10-23 | El profesor pide más. Se construye la versión 2.6 (historial, velocidad, panel web, MySQL) y se documenta como spec 002 junto con el código. |
| 2026-11-02 | Auditoría: se detecta que la 2.6 salió del alcance de la constitución v1.0.0 y que la spec 002 no tenía clarify ni plan. Se enmienda la constitución y se completa la documentación. |

## Resultado de la auditoría

| # | Punto | Resultado |
|---|---|---|
| 1 | SDD | Cumple en 001 · Parcial en 002 (ahora completada) |
| 2 | Artefactos | Cumple, con inconsistencias menores (corregidas) |
| 3 | Constitución | 002 violaba los principios I y II (corregido con v2.0.0) |
| 4 | Spec | 001 sólida · 002 sin criterios ni casos límite (completada) |
| 5 | Clarify | 001 cumple · 002 no tenía (registrada después) |

**Dictamen:** MVP de software sí. Para MVP completo falta probar el GPS real (T013 y T015).

Pruebas de la API al momento de la auditoría: `npm test` → 19 pass, 0 fail.

## Qué se cambió

| Archivo | Cambio |
|---|---|
| `.specify/memory/constitution.md` | v1.0.0 → v2.0.0 (MAJOR). Principio I por fases, II con MySQL opcional y panel, IV sin `root`. Historial de enmiendas. |
| `.specify/memory/historial/constitution-v1.0.0.md` | Nuevo: copia exacta de la versión anterior. |
| `reglas.md` | Se agregó al final la enmienda; las 14 reglas originales no se tocaron. |
| `specs/002-mas-funciones/spec.md` | Status, nota de trazabilidad, Clarifications (registrado después), Edge Cases, Success Criteria, Assumptions; R12–R18 ahora FR-101–FR-107 (se conserva el nombre anterior); redacción de H8 para no confundir con ETA. |
| `specs/002-mas-funciones/plan.md` | Nuevo: plan con Constitution Check contra v2.0.0 (y contra v1.0.0, donde no cumplía). |
| `specs/002-mas-funciones/checklists/requirements.md` | Nuevo. |
| `specs/002-mas-funciones/tasks.md` | Nota de los nuevos nombres FR y Fase 7 (T122–T125). |
| `specs/001-ubicacion-tiempo-real/spec.md` | Status: de "Draft" a "Implementada en software; hardware pendiente". |
| `specs/001-ubicacion-tiempo-real/tasks.md` | Notas en T024, T026 y T033 con lo que ya hay y lo que falta (no se marcaron sin evidencia). |
| `api/.env.example` | `DB_USER=truck_app` en lugar de `root`. |
| `LEEME.md` | Se agregó esta bitácora. (Primero se quitó `.opencode/` porque no estaba en la copia del proyecto, pero sí existe en la computadora del equipo; se volvió a poner.) |
| `como-usamos-los-apuntes-sdd.md` | 6 preguntas de clarify (no 5), 1 pendiente (no 2), paso 7 al día y paso 8 de auditoría. |

## Ajuste con el pizarrón del ingeniero (2026-11-02, tarde)

El ingeniero pidió que la spec tenga historias de usuario **y requisitos en formato EARS**. TOR tenía historias de usuario, pero los requisitos estaban como "El sistema DEBE…". Se reescribieron FR-001 a FR-013 (spec 001) y FR-101 a FR-107 (spec 002) en EARS, sin cambiar lo que pide cada uno.

| Pizarrón | En TOR |
|---|---|
| Project Charter | `ACTA DE CONSTITUCIÓN DEL PROYECTO.pdf` (en la computadora del equipo) y su resumen (R1–R9, equipo, roles) en `prompt-maestro-v2.md` |
| Backlog | Requisitos del acta + `tasks.md` de cada spec |
| Jira | Resúmenes por etapa (T036, pendiente) |
| Leer y comprender | `diagnostico.md` |
| 1 Constitución | `.specify/memory/constitution.md` v2.0.0 |
| 2 Spec (historias + EARS) | `spec.md` 001 y 002 (EARS desde hoy) |
| 3 Clarify | Sección *Clarifications* de cada spec |
| 4 Plan | `plan.md` de cada spec |
| 5 Tasks | `tasks.md` de cada spec |
| 6 (Implement) | `api/`, `app-android/`, `firmware/` |
| BD: esquema, gráfica, SGBD | `db/esquema.sql`, `db/diagrama-er.png`, MySQL 8 |

## Pendiente (necesita al equipo)

1. **T013 y T015:** probar el GPS real en cielo abierto y el envío por WiFi. Es lo único que falta para el MVP completo.
2. **T024 y T026:** confirmar los pasos 8 y 9 de `DEMO.md` (sin señal reciente y API apagada) y marcarlas.
3. **T034:** cambiar el correo y teléfono de `PerfilScreen.kt` por datos ficticios (pide permiso del equipo).
4. **T023:** borrar `ui/TruckCard.kt` vacío y la función sin uso `TruckCardSection()` de `HomeScreen.kt`, que todavía dice "llega en 23 min" (pide permiso del equipo).
5. **T033:** `README.md` de `firmware/`.
6. **T036:** resúmenes para Jira.

## Lección para la próxima spec

Primero `/speckit.specify` y `/speckit.clarify`, después `plan.md` con el Constitution Check, y si la idea choca con la constitución, se enmienda **antes** de escribir código.
