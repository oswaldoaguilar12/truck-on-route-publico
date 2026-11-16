# Tasks: Roles, recorridos y avisos (versión 2.8)

**Input**: `specs/004-roles-recorridos-avisos/` · **Tests**: `api/test/v3.test.js`

## Fase 1: Preparación
- [ ] T401 Copiar la v2.7 a `C:\Melendez\TruckOR.v8` (la v7 no se toca); en GitHub va en la rama `version-2.8`
- [X] T402 Enmendar la constitución a v3.0.0; guardar la v2.1.0 en `historial/`
- [X] T403 Spec, contrato, plan y tareas de la spec 004

## Fase 2: API
- [X] T404 [P] `seguridad.js`: hash scrypt y tokens (FR-402)
- [X] T405 [P] `datos.js`: datos en memoria, guardado en JSON y datos de ejemplo (FR-411)
- [X] T406 `rutas-v3.js`: sesión y registro (FR-401, FR-403)
- [X] T407 `rutas-v3.js`: permisos por rol (FR-404)
- [X] T408 `rutas-v3.js`: altas del administrador y mensajes (FR-405, FR-406)
- [X] T409 `rutas-v3.js`: recorridos del chofer y avisos (FR-407)
- [X] T410 `test/v3.test.js` y `npm test` en verde (28 de 28: 19 de antes + 9 nuevas, 2026-11-11)

## Fase 3: Panel del administrador
- [X] T411 Inicio de sesión del administrador en `/panel`
- [X] T412 Pestañas Camiones, Choferes, Rutas, Recorridos, Mensajes y Usuarios

## Fase 4: App
- [X] T413 `ApiTor.kt` y `Sesion.kt`
- [X] T414 Login y registro reales; navegación por rol (FR-408)
- [X] T415 `ChoferScreen.kt`: recorrido, Iniciar/Terminar ruta, mensajes
- [X] T416 Inicio del ciudadano: avisos reales, sin `admin123` (FR-409)
- [X] T417 Notificaciones y `ServicioCercania` (FR-410)

## Fase 5: Prueba (equipo)
- [X] T418 Compilar en Android Studio · compiló y corrió en el emulador el 2026-11-11 a la primera
- [X] T419 Demo completa (2026-11-11, emulador): ✅ panel con login del administrador (MySQL activo) · ✅ el chofer ve recorrido, colonias, indicaciones y el mensaje del panel · ✅ Iniciar ruta → el ciudadano ve y recibe "El camión ya está en ruta" · ✅ con el emulador en 20.1765, -98.0494 llega "El camión está cerca" a 329 m · ✅ notificación fija "vigilando el camión"
- [ ] T420 Subir a GitHub (rama `version-2.8`) y resumen para Jira
- [ ] T421 Pasar la versión 2.8 a la carpeta 2.5 original (cuando el equipo lo apruebe)

## Notas de implementación (2026-11-11)
- En esta copia `HomeScreen.kt` tenía otra vez la lista de ejemplo de colonias (de Pachuca). Se regresó a `buscarColonias()` con las 83 colonias reales (versión 2.5, R2).
- `ui/GestionUsariosScreen.kt` ya no se usa (la gestión pasó al panel web). No se borró: borrarlo necesita permiso del equipo (igual que T023).
