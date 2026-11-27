# Plan: Aviso de privacidad (versión 2.12)

**Spec**: `spec.md` · **Rama**: `version-2.12` (sale de `version-2.11`) · **Maqueta y texto**: aprobados por el equipo el 2026-11-27 (`evidencia/v2.12/maqueta_privacidad_v2.12.png`, `aviso-privacidad.md`)

## Revisión con la constitución (v4.2.0)
- I. Alcance: el aviso es parte de la Fase 4 desde la enmienda 4.2.0. No se agrega borrar la cuenta desde la app. ✔
- II. Tres partes y un contrato: el contrato cambia en una sola cosa, `POST /usuarios` pide `aceptaAviso: true` (si falta, 400 con `{ "error": "..." }`). Se agrega `GET /privacidad`. El firmware no cambia. ✔
- III. Partir de lo que existe: el registro conserva sus campos y su validación; solo se agrega la casilla. La pantalla del aviso usa las piezas de la v2.10. No se borra ningún archivo. ✔
- IV. Sencillo, seguro y en español: sin librerías nuevas. La página web es un HTML fijo. Se guarda solo la fecha de aceptación (`avisoAceptado`). La ubicación del celular sigue sin salir del celular. ✔
- V. Una etapa con prueba: `npm test` con 2 pruebas nuevas, y en el emulador registrar con y sin la casilla. ✔

## Enfoque
1. **API**
   - `rutas-v3.js`: `POST /usuarios` responde 400 "Debes aceptar el aviso de privacidad" si `aceptaAviso` no es `true`; si lo es, guarda `avisoAceptado` con la fecha.
   - `datos.js`: `crearUsuario` recibe `avisoAceptado` (las cuentas de chofer y administrador no lo llevan).
   - `app.js` + `public/privacidad.html`: `GET /privacidad` sin sesión.
   - `test/v3.test.js`: las pruebas de registro mandan `aceptaAviso: true` y hay 2 pruebas nuevas.
2. **App**
   - `ui/AvisoPrivacidadScreen.kt`: encabezado verde y 8 tarjetas (la de ubicación resaltada).
   - `ui/RegistroScreen.kt`: casilla con enlace al aviso; sin marcarla sale "Acepta el aviso de privacidad para continuar".
   - `data/ApiTor.kt`: `registrar` manda `aceptaAviso`.
   - `ui/HomeScreen.kt`: opción "Aviso de privacidad" en el menú.

## Riesgos
- El texto está en 3 lugares (md, HTML y Kotlin). Si cambia, hay que cambiarlo en los tres; cada archivo lo dice en un comentario.
- Una app vieja (v2.11 o antes) ya no puede registrar cuentas con la API nueva, porque no manda `aceptaAviso`. Se usa la app v2.12.
- No se puede compilar la app en la nube; el equipo compila en Android Studio.
