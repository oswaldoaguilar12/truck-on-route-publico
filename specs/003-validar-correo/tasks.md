# Tasks: Validar el formato del correo (versión 2.7)

**Input**: `specs/003-validar-correo/` · **Tests**: `ValidarCorreoTest.kt` (JUnit)

## Fase 1: Preparación
- [X] T301 Copiar TruckOnRute2.5 a TruckOnRute2.7, sin tocar la 2.5 (la que se usó para la auditoría)
- [X] T302 Enmendar la constitución a v2.1.0 (excepción del correo en el Principio III); guardar la v2.0.0 en `historial/`

## Fase 2: Lógica con prueba (US1 y US2)
- [X] T303 [P] `data/ValidarCorreo.kt`: `esCorreoValido()` y `MENSAJE_CORREO_INVALIDO` (FR-201, FR-206)
- [X] T304 [P] `test/.../ValidarCorreoTest.kt`: 6 correos válidos, 17 inválidos (incluido `hola.com`) y uno demasiado largo. Resultado: 4 de 4 pruebas pasan (corridas con Kotlin 2.0 y JUnit 4.13.2 el 2026-11-02)

## Fase 3: Pantallas
- [X] T305 [US1] `MainActivity.kt` (LoginScreen): validar al tocar Iniciar sesión y mostrar los mensajes (FR-202, FR-203, FR-204)
- [X] T306 [US2] `ui/RegistroScreen.kt`: validar al tocar Registrarme; parámetros opcionales en `CampoTextoMejorado` (FR-205)

## Fase 4: Prueba en el celular (equipo)
- [x] T307 Compilar en Android Studio (no se pudo compilar la app en la nube: falta el SDK de Android) · compiló y corrió en el emulador el 2026-11-11; el mapa muestra la simulación
- [ ] T308 Probar en el celular: `hola.com` → mensaje; `nombre@gmail.com` + contraseña → entra; contraseña vacía → mensaje; registro con `ana@gmail` → mensaje
- [ ] T309 Correr `ValidarCorreoTest` en Android Studio y tomar captura
- [ ] T310 Resumen para Jira
