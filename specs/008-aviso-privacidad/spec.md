# Feature Specification: Aviso de privacidad (versión 2.12)

**Feature Branch**: `version-2.12` (sale de `version-2.11`) · **Created**: 2026-11-27 · **Status**: Aprobada (texto y maqueta aprobados por el equipo el 2026-11-27)

**Input**: "Oye hay que meterle las políticas de privacidad, ¿cómo lo recomiendas más?" (Oswaldo, 2026-11-26)

## Contexto

La app pide nombre, correo, contraseña, teléfono y colonia al registrarse, y usa la ubicación del celular para el aviso de cercanía. En México, quien pide datos personales debe dar un aviso de privacidad (Ley Federal de Protección de Datos Personales en Posesión de los Particulares). Como no estaba en el alcance, se hizo la enmienda 4.2.0 de la constitución antes de escribir código.

El texto del aviso está en `aviso-privacidad.md` (un solo texto para la app y el panel web).

## Historia de usuario

**H24 (P1)**: Como ciudadano, quiero saber qué datos míos guarda la app y para qué, y poder pedir que los borren, para confiar en ella antes de registrarme.

**Prueba independiente**: crear una cuenta en el emulador; sin marcar la casilla no deja registrarse; con la casilla marcada sí. Abrir el aviso desde el registro, desde el menú y en `http://localhost:3000/privacidad`.

## Requisitos (EARS)

- **FR-801**: La pantalla de registro DEBE tener la casilla "Acepto el aviso de privacidad" con el texto "aviso de privacidad" como enlace que abre el aviso completo.
- **FR-802**: SI la casilla no está marcada, el botón "Registrarme" DEBE mostrar el mensaje "Acepta el aviso de privacidad para continuar" y no crear la cuenta.
- **FR-803**: El menú lateral DEBE tener la opción "Aviso de privacidad" que abre el aviso completo.
- **FR-804**: El aviso en la app DEBE verse en una pantalla con encabezado verde (piezas de la v2.10), con secciones que se pueden leer deslizando y un botón para regresar.
- **FR-805**: La API DEBE servir el mismo aviso en `GET /privacidad` como página web con los colores de la app, sin pedir sesión.
- **FR-806**: La API DEBE guardar la fecha en que el usuario aceptó el aviso (`avisoAceptado`) al crear la cuenta, y rechazar con 400 `{ "error": "Debes aceptar el aviso de privacidad" }` un registro sin aceptarlo.
- **FR-807**: El aviso DEBE decir que la ubicación del celular del ciudadano no se manda al servidor (así funciona hoy: `Vigilancia.ubicacionDelCelular` solo se usa dentro de la app).
- **FR-808**: Ningún cambio DEBE alterar lo que ya hace la v2.11.

## Criterios de éxito

- **SC-801**: Compila en Android Studio sin errores y `npm test` pasa con las pruebas nuevas.
- **SC-802**: En el emulador no se puede registrar sin aceptar el aviso; aceptándolo, sí.
- **SC-803**: `http://localhost:3000/privacidad` abre el aviso en el navegador.

## Fuera de alcance

- Un botón para borrar la cuenta desde la app (por ahora se pide por correo, como dice el aviso).
- Revisión legal formal (queda anotada en el aviso).
- Aviso para las cuentas de chofer y administrador (las crea el administrador).
