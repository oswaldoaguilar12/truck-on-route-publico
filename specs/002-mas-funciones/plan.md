# Implementation Plan: Más funciones para la API y la app (versión 2.6)

**Spec**: [spec.md](spec.md) · **Fecha**: 2026-11-02 (registrado después) · **Constitución**: v2.0.0

> Este plan describe lo que ya se construyó el 2026-10-23. Se escribió el 2026-11-02, después de la auditoría, para que la spec 002 tenga el mismo ciclo que la 001 (spec → clarify → plan → tasks). No se cambió el código para que cuadre con el plan: el plan describe el código tal como está.

## Summary

Aprovechar que la API ya recibe todas las ubicaciones para guardar el historial, calcular velocidad y distancia, mostrarlos en la app y en un panel web para el administrador, y guardarlos opcionalmente en MySQL.

## Technical Context

| Parte | Tecnología | Por qué |
|---|---|---|
| Cálculos | `api/geo.js` con la fórmula de Haversine | Unas pocas líneas; no hace falta librería (Principio IV) |
| Historial | Arreglo en memoria por camión, máximo 500 puntos | La API debe funcionar sin base de datos (Principio II) |
| Base de datos | MySQL 8 con `mysql2`, opcional | Lo que el equipo ya conoce (Workbench); se activa con `DB_HOST` |
| Panel web | `api/public/index.html` + Leaflet desde `node_modules` | Lo sirve la misma API; funciona sin internet para su código |
| App | `TruckMapSection.kt` con `Polyline` de osmdroid | El mapa ya era osmdroid desde la 001 |
| Pruebas | `api/test/api.test.js` con `node --test` | Igual que la 001 |

## Constitution Check (contra v2.0.0)

- I Alcance por fases: ✅ historial, panel de solo lectura y MySQL opcional están en la Fase 2. ETA, login real y notificaciones siguen fuera.
- II Tres partes y un contrato: ✅ el panel vive en `api/public/`; el contrato de la 001 no cambia y lo nuevo está en `contracts/api-v2.6.md`; la API funciona en memoria si no hay MySQL.
- III Partir de lo que existe: ✅ en la app solo cambian `UbicacionApi.kt` y `TruckMapSection.kt`.
- IV Sencillo y seguro: ✅ sin secretos en git (`.env` en `.gitignore`); la API usa el usuario `truck_app`, no `root` (T120). ⚠️ `.env.example` sugería `root`; se corrigió el 2026-11-02.
- V Etapas con prueba: ✅ 19 de 19 pruebas pasan; prueba con base real (T108) y en el emulador (T116).

**Contra la v1.0.0 (la vigente el 2026-10-23):** ❌ los principios I y II no permitían historial, administración ni base de datos. Por eso se hizo la enmienda v2.0.0.

## Project Structure

```text
specs/002-mas-funciones/
├── spec.md
├── plan.md                    # este archivo
├── contracts/api-v2.6.md
├── checklists/requirements.md
└── tasks.md

api/
├── app.js           # rutas nuevas: historial, estadísticas, camiones, colonias, panel
├── geo.js           # nuevo: Haversine, recorrido y velocidad
├── colonias.js      # nuevo: 83 colonias y búsqueda sin acentos
├── db.js            # nuevo: MySQL opcional
├── public/index.html  # nuevo: panel del administrador
└── test/api.test.js   # nuevo: 8 pruebas de rutas

db/esquema.sql       # 8 tablas; la API usa ubicaciones y colonias

app-android/.../data/UbicacionApi.kt     # + obtenerHistorial(), obtenerEstadisticas()
app-android/.../ui/TruckMapSection.kt    # + ruta, velocidad, distancia, Centrar
```

## Data Model (cambios respecto a la 001)

- **Ubicación**: mismos campos. Ahora se guardan todas, no solo la última.
- **Estadísticas** (calculadas, no se guardan): `puntosRecibidos`, `distanciaKm`, `velocidadKmh` (o `null`), `enLinea`.
- **Tabla `ubicaciones`** en MySQL: ver `db/esquema.sql` y `db/diagrama-er.png`.

## Cómo se prueba

Ver `DEMO.md` (pasos 5b, 5c, 6, 6b y 7) y `npm test` en `api/`.
