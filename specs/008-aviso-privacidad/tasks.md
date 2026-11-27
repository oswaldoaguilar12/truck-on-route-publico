# Tasks: Aviso de privacidad (versión 2.12)

**Input**: `specs/008-aviso-privacidad/`

- [X] T801 Spec 008, enmienda 4.2.0, texto del aviso y maqueta (2026-11-27)
- [X] T802 Texto y maqueta aprobados por el equipo; plan con su revisión (2026-11-27)
- [X] T803 API: `POST /usuarios` pide `aceptaAviso` y guarda `avisoAceptado` (FR-806) — 2026-11-27, `npm test` 30/30
- [X] T804 API: `GET /privacidad` con `public/privacidad.html` (FR-805) — 2026-11-27, `npm test` 30/30
- [X] T805 App: `AvisoPrivacidadScreen.kt` (FR-804, FR-807) — programada 2026-11-27, falta compilar (T808)
- [X] T806 App: casilla en `RegistroScreen.kt` y `ApiTor.registrar` con `aceptaAviso` (FR-801, FR-802) — programada 2026-11-27, falta compilar (T808)
- [X] T807 App: opción "Aviso de privacidad" en el menú de `HomeScreen.kt` (FR-803) — programada 2026-11-27, falta compilar (T808)
- [ ] T808 Compilar en Android Studio sin errores (SC-801) — equipo
- [ ] T809 En el emulador: registrar sin la casilla (no deja) y con la casilla (sí); abrir el aviso desde el registro y el menú; abrir `http://localhost:3000/privacidad` (SC-802, SC-803) — equipo
