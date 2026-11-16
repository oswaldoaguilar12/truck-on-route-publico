# Feature Specification: Validar el formato del correo en login y registro

**Feature Branch**: `003-validar-correo` · **Created**: 2026-11-02 · **Status**: Implementada (pendiente de probar en el celular)
**Versión de la app**: 2.7 · **Constitución**: v2.1.0 (Principio III, excepción del correo)
**Input**: Oswaldo, 2026-11-02: "que acepte correos con dominios correctos, porque si le pongo el .com sin @ los acepta".

## Clarifications

### Session 2026-11-02

- Q: ¿Qué es un "dominio correcto"? → A: Cualquier dominio con formato válido: nombre, `@`, dominio y extensión de 2 o más letras (`nombre@gmail.com`, `ana@itsh.edu.mx`). No se usa una lista fija de dominios (gmail, hotmail…) porque rechazaría correos reales como los institucionales. *Propuesta de Claude; el equipo la confirma al probar.*
- Q: ¿Se comprueba que el correo exista o que la cuenta esté registrada? → A: No. Solo el formato. El login real sigue fuera de alcance (constitución, Principio I).
- Q: ¿En qué pantallas? → A: En el login (la pantalla que se usa, en `MainActivity.kt`) y en el registro (`ui/RegistroScreen.kt`). Perfil y gestión no cambian.
- Q: ¿Cuándo se muestra el error? → A: Después de tocar el botón (Iniciar sesión o Registrarme), debajo del campo, en rojo. Así no sale un error mientras la persona apenas empieza a escribir.
- Q: ¿Qué pasa con los espacios al inicio o al final? → A: Se ignoran (`" ana@gmail.com "` es válido), porque el teclado del celular a veces agrega un espacio.

## User Scenarios & Testing

### User Story 1 - El login rechaza correos mal escritos (Priority: P1)

Como ciudadano, quiero que la app me avise si escribí mal mi correo, para no entrar con un dato equivocado.

**Independent Test**: escribir `hola.com` y una contraseña, tocar Iniciar sesión: la app no entra y muestra el mensaje.

1. **Given** el correo `hola.com` y una contraseña, **When** toco Iniciar sesión, **Then** no entro y veo "Escribe un correo válido, por ejemplo nombre@gmail.com".
2. **Given** el correo `nombre@gmail.com` y una contraseña, **When** toco Iniciar sesión, **Then** entro a Inicio.
3. **Given** un correo válido y la contraseña vacía, **When** toco Iniciar sesión, **Then** no entro y veo "Escribe tu contraseña".

### User Story 2 - El registro rechaza correos mal escritos (Priority: P2)

Como ciudadano, quiero que el registro me avise si el correo está mal, para no crear una cuenta que no sirva.

**Independent Test**: en Registrarse, escribir `ana@gmail` y tocar Registrarme: no regresa al login y muestra el mensaje.

1. **Given** el correo `ana@gmail`, **When** toco Registrarme, **Then** me quedo en la pantalla y veo el mensaje debajo del correo.
2. **Given** un correo válido, **When** toco Registrarme, **Then** regreso al login, igual que antes (no se guarda nada).

### Edge Cases

- Vacío o solo espacios: inválido.
- Sin `@` (`hola.com`), con dos `@` (`ana@@gmail.com`) o sin nombre (`@gmail.com`): inválido.
- Sin extensión (`ana@gmail`), extensión de 1 letra (`ana@gmail.c`) o con números (`ana@gmail.c0m`): inválido.
- Puntos mal puestos (`.ana@`, `ana.@`, `ana..x@`, `ana@gmail..com`, `ana@gmail.com.`): inválido.
- Espacio en medio (`ana @gmail.com`): inválido.
- Más de 254 caracteres: inválido.
- Mayúsculas (`OSWALDO_97@Outlook.com`), `+`, `_`, `-` y subdominios (`itsh.edu.mx`): válido.

## Requirements (EARS)

| Id | Patrón | Requisito | Acta |
|---|---|---|---|
| FR-201 | Ubicuo | La app deberá considerar válido un correo solo si tiene nombre, una `@`, un dominio y una extensión de 2 o más letras, sin espacios ni puntos mal puestos, y máximo 254 caracteres. | R1 (solo formato) |
| FR-202 | No deseado | Si el correo del login no es válido cuando se toca Iniciar sesión, entonces la app no deberá entrar y deberá mostrar debajo del campo "Escribe un correo válido, por ejemplo nombre@gmail.com". | R1 |
| FR-203 | No deseado | Si la contraseña del login está vacía cuando se toca Iniciar sesión, entonces la app no deberá entrar y deberá mostrar "Escribe tu contraseña". | R1 |
| FR-204 | Por evento | Cuando se toque Iniciar sesión con un correo válido y una contraseña, la app deberá entrar a Inicio (igual que antes; todavía no hay login real). | R1 |
| FR-205 | No deseado | Si el correo del registro no es válido cuando se toca Registrarme, entonces la app deberá quedarse en el registro y mostrar el mismo mensaje. | R1 |
| FR-206 | Ubicuo | La app deberá ignorar los espacios al inicio y al final del correo al validarlo. | — |

## Success Criteria

- **SC-201**: Las pruebas `ValidarCorreoTest` pasan (4 de 4; 24 correos de ejemplo).
- **SC-202**: En el celular, `hola.com` ya no deja entrar (el caso que encontró el equipo).
- **SC-203**: Con `nombre@gmail.com` y una contraseña se entra a Inicio y el mapa funciona igual que en la 2.6.

## Assumptions

- No hay login real ni base de usuarios en esta fase; solo se revisa el formato.
- El texto del correo no se guarda en ningún lado.
- La pantalla `ui/LoginScreen.kt` no se usa (ver `diagnostico.md`), por eso no se cambia.

## Fuera de alcance

Verificar que el correo exista, enviar correos de confirmación, guardar usuarios y validar teléfono o contraseña segura.
