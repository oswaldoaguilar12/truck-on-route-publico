# Feature Specification: Avisos con historial y chat chofer-administrador (versión 2.14)

**Feature Branch**: `version-2.14` (sale de `version-2.13`) · **Created**: 2026-11-27 · **Status**: Terminada (probada por el equipo el 2026-11-27)

**Input**: "Hay que meter la modificación a las alertas tanto para usuario como para los choferes, al igual que hay que ampliar la sección o crear una donde el chofer y el admin estén mensajeándose en caso de alguna falla o querer bajar por unos tacos" y "las alertas, deja los puntos 2 y 3 que recomendaste. En el chat el chofer tendrá mensajes rápidos y a su vez los de texto libre, así como poder enviar fotos o audios para mayor comunicación, el admin podrá responder con audios también y fotos, y sí mete el punto dos que me diste para el chat" (Oswaldo, 2026-11-27)

## Contexto

Hoy los avisos solo se ven como los 5 más nuevos en la hoja de Inicio, sin saber cuáles ya se leyeron. Solo hay dos tipos automáticos ("ya está en ruta" y, desde la v2.13, "cambió su ruta"). Los mensajes van en un solo sentido: el administrador escribe y el chofer lee. Como no estaba en el alcance, se hizo la enmienda 4.4.0 de la constitución antes de escribir código.

## Historias de usuario

**H25 (P1) Avisos con historial**: Como ciudadano o chofer, quiero ver todos mis avisos en una sección, con los nuevos marcados, para no perderme ninguno.

**H26 (P1) Más tipos de aviso**: Como administrador, quiero avisar "hoy no pasa el camión", "el camión va retrasado" o un aviso general, a los ciudadanos, a los choferes o a todos.

**H27 (P1) Chat chofer-administrador**: Como chofer, quiero avisarle al administrador de una falla o una pausa con un toque, con texto, foto o audio, y que me conteste.

**Prueba independiente**: en el panel mandar un aviso "Va retrasado" a ciudadanos: llega a la app del ciudadano (notificación y sección Avisos, marcado como nuevo). En la app del chofer tocar "Falla mecánica", mandar una foto y un audio; en el panel se ve el contador de no leídos, se abren, se escuchan y se contesta con texto, foto y audio; el chofer los ve y escucha.

## Requisitos (EARS)

### Parte 1: avisos con historial
- **FR-1001**: La app DEBE tener la sección "Avisos" (en el menú del ciudadano y con una campanita en la pantalla del chofer) con los avisos de los últimos 7 días, del más nuevo al más viejo.
- **FR-1002**: Cada aviso DEBE mostrar su ícono y color según el tipo, título, mensaje y hora; los no leídos llevan un punto verde y fondo verde claro.
- **FR-1003**: CUANDO el usuario abra la sección, los avisos DEBEN quedar como leídos. Lo leído se guarda solo en el celular (no se manda a la API).
- **FR-1004**: La campanita y la opción del menú DEBEN mostrar cuántos avisos no se han leído.

### Parte 2: más tipos de aviso
- **FR-1005**: El panel DEBE tener la pestaña "Avisos" para mandar un aviso con tipo (`no_pasa`, `retraso` o `general`), ruta (opcional), destinatarios (`ciudadanos`, `choferes` o `todos`) y mensaje, y la lista de avisos enviados.
- **FR-1006**: La API DEBE crear el aviso con `POST /admin/avisos` (solo administrador) y rechazar con 400 un tipo o destinatario inválido o un mensaje vacío.
- **FR-1007**: `GET /avisos` (ciudadano, sin sesión) DEBE dar solo los avisos para ciudadanos o todos; `GET /chofer/avisos` DEBE dar los avisos para choferes o todos.
- **FR-1008**: Los avisos nuevos DEBEN llegar como notificación del celular, igual que "ya está en ruta" (al ciudadano; al chofer con la app abierta).

### Parte 3: chat chofer-administrador
- **FR-1009**: La pantalla del chofer DEBE tener el botón "Chat con el administrador" con el número de mensajes sin leer, que abre el chat.
- **FR-1010**: El chat del chofer DEBE tener mensajes rápidos de un toque: "Falla mecánica", "Ponchadura", "Tráfico", "Pausa para comer", "Ya terminé" y "Necesito ayuda"; además texto libre, foto (cámara o galería) y audio (mantener presionado para grabar, máximo 60 s).
- **FR-1011**: El panel DEBE tener la pestaña "Chat" con la lista de choferes, cuántos mensajes sin leer tiene cada uno y un contador total en la pestaña; al abrir un chofer se ven sus mensajes y se marcan como leídos.
- **FR-1012**: El administrador DEBE poder contestar con texto, foto (archivo) y audio (grabado en el navegador).
- **FR-1013**: La API DEBE guardar cada mensaje con `deId`, `paraId`, `tipo` (`texto`, `rapido`, `foto` o `audio`), `texto`, `archivo` (si tiene), `fecha` y `leido`. Las fotos y audios se mandan en base64 dentro del JSON, se guardan en `api/datos/archivos/` y la API rechaza con 400 los de más de 5 MB o de otro formato (foto: JPG o PNG; audio: M4A/AAC, WebM u OGG).
- **FR-1014**: Un archivo del chat solo lo pueden descargar el chofer de esa conversación y el administrador (con su token).
- **FR-1015**: Los mensajes actuales del administrador al chofer (v2.8) y el mensaje automático del desvío (v2.13) DEBEN verse dentro del chat.
- **FR-1016**: El chat DEBE revisar mensajes nuevos cada 5 s mientras está abierto.

### General
- **FR-1017**: Ningún cambio DEBE alterar lo que ya hace la v2.13. Los colores son los de la app; cada tipo de aviso usa uno de los colores que ya existen (verde, ámbar, rojo y azul).

## Criterios de éxito

- **SC-1001**: `npm test` pasa con pruebas nuevas de avisos por destinatario, chat, archivos y permisos.
- **SC-1002**: Compila en Android Studio y la prueba independiente funciona en el emulador y el panel.

## Fuera de alcance

- Chat entre ciudadanos, o ciudadano-administrador.
- Videos, llamadas o ubicación en el chat.
- Notificaciones al chofer con la app cerrada (sería con Firebase, que sigue fuera de alcance).
- Borrar o editar mensajes ya enviados.
