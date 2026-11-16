# Tasks: Más funciones para la API y la app (versión 2.6)

**Input**: `specs/002-mas-funciones/spec.md` · **Tests**: `api/test/api.test.js` (8 pruebas de rutas) + las 11 de `validar`

> Desde el 2026-11-02 los requisitos R12–R18 se llaman FR-101 a FR-107 (ver la tabla de `spec.md`).

## Fase 1: API
- [X] T101 [P] `api/geo.js`: distancia con Haversine, km recorridos y velocidad (R13)
- [X] T102 [P] `api/colonias.js`: 83 colonias y búsqueda sin acentos (R15, H12)
- [X] T103 `api/app.js`: guardar historial en memoria por camión (máximo 500 puntos) (R12)
- [X] T104 `api/app.js`: rutas `/ubicacion/historial`, `/camiones`, `/estadisticas`, `/colonias`, 404 (R12–R15)
- [X] T105 `api/test/api.test.js`: pruebas de cada ruta nueva. Resultado: 19 de 19 pasan

## Fase 2: Base de datos
- [X] T106 `api/db.js`: conexión opcional a MySQL con `mysql2`, guarda en `ubicaciones` y lee el historial (R16, H11)
- [X] T107 `api/.env.example`: variables `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`
- [X] T108 Prueba con base real: 13 puntos guardados y leídos de vuelta

## Fase 3: Panel web
- [X] T109 `api/public/index.html`: estado, 5 tarjetas, mapa Leaflet con ruta, tabla y buscador (R17, H10)
- [X] T110 Servir Leaflet desde `node_modules` en `/panel/leaflet`
- [X] T111 Captura del panel funcionando: `reporte/panel-web.png`

## Fase 4: App Android
- [X] T112 `UbicacionApi.kt`: `obtenerHistorial()` y `obtenerEstadisticas()` (R18)
- [X] T113 `TruckMapSection.kt`: línea verde de la ruta (H7)
- [X] T114 `TruckMapSection.kt`: velocidad, distancia y puntos debajo del mapa (H8)
- [X] T115 `TruckMapSection.kt`: dejar de seguir al arrastrar y botón Centrar (H9)
- [X] T116 Compilar en Android Studio y probar en el emulador (adb -e reverse): ruta y velocidad visibles

## Fase 5: Simulación y documentación
- [X] T117 `api/simular.js`: 30 puntos por vuelta (ida y regreso) para que la ruta se vea completa
- [X] T118 Documentar: esta spec, contrato v2.6, `api/README.md`, `DEMO.md`, reporte

## Fase 6: MySQL en la computadora del equipo (2026-10-23)
- [X] T119 Cargar `db/esquema.sql` en MySQL 8 (Workbench): 8 tablas y 83 colonias
- [X] T120 Usuario `truck_app` solo con permisos sobre `truck_on_route` (la API no usa root)
- [X] T121 `.env` con `DB_HOST=localhost` y `DB_USER=truck_app`; la API dice `Conectada a MySQL` y guarda en `ubicaciones`

## Fase 7: Auditoría (2026-11-02)
- [X] T122 Auditoría SDD del proyecto: constitución, artefactos, spec y clarify
- [X] T123 Enmendar la constitución a v2.0.0 y guardar la v1.0.0 en `.specify/memory/historial/`
- [X] T124 Completar esta spec (clarify, casos límite, criterios, supuestos), `plan.md` y `checklists/requirements.md` (registrado después)
- [X] T125 `api/.env.example`: `DB_USER=truck_app` en lugar de `root`
