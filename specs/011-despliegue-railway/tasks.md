# Tasks: Despliegue en Railway y seguridad básica (versión 2.15)

**Input**: `specs/011-despliegue-railway/`

- [X] T1101 Enmienda 4.5.0, spec 011 y plan con su revisión (2026-11-27)
- [X] T1102 API: clave del GPS en `POST /ubicacion` y en `simular.js`, con pruebas (FR-1104) — 2026-11-27
- [X] T1103 API: modo producción (sin cuentas de ejemplo y sin arrancar con contraseña débil), con pruebas (FR-1105) — 2026-11-27
- [X] T1104 API: `DATOS_DIR`, `DB_URL`, tablas creadas solas y `engines` (FR-1102, FR-1103) — 2026-11-27; probado con MariaDB local y el usuario truck_app con permisos limitados: crea las tablas y guarda la ubicación
- [X] T1105 `npm audit fix` y `npm test` en verde (FR-1106, SC-1101) — 2026-11-27, `npm audit` 0 vulnerabilidades, `npm test` 39/39
- [X] T1106 Guía `docs/railway.md` (FR-1107) — 2026-11-27
- [ ] T1107 El equipo crea el proyecto en Railway con la guía y prueba panel y app (SC-1102, SC-1103) — equipo
- [ ] T1108 ESP32 con la dirección de Railway y la clave del GPS — al final, con el equipo del GPS
