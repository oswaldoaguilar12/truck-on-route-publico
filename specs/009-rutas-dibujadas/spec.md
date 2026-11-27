# Feature Specification: Rutas dibujadas, lo recorrido en gris y desvíos (versión 2.13)

**Feature Branch**: `version-2.13` (sale de `version-2.12`) · **Created**: 2026-11-27 · **Status**: Terminada (probada en el emulador el 2026-11-27 con las tres cuentas; maqueta aprobada por el equipo el 2026-11-27; FR-912 agregado a petición del equipo)

**Input**: "Al pasar el camión por la ruta se debe pintar de gris por donde ya pasó. Otro, el administrador pueda dibujar la ruta para que el chofer la siga en caso de haber un bloqueo en alguna calle, y para crear las rutas que los camiones usan." (Oswaldo, 2026-11-27)

## Contexto

Hoy una ruta es solo una lista de colonias y un horario, y el mapa dibuja en verde el camino que el camión ya hizo (su historial). No hay forma de ver por dónde va a pasar ni de cambiar el camino si una calle está cerrada. Esto estaba fuera de alcance; la enmienda 4.3.0 agrega la Fase 5 antes de escribir código.

Orden acordado con el equipo (2026-11-27): primero las partes 1 y 2; la parte 3 (desvío) si da tiempo antes del 30 de noviembre.

## Historias de usuario

- **H25 (P1)**: Como administrador, quiero dibujar en el mapa el camino de cada ruta, para que quede claro por qué calles pasa el camión.
- **H26 (P1)**: Como ciudadano, quiero ver la ruta del camión con lo que ya recorrió en gris, para saber de un vistazo si ya pasó por mi calle.
- **H27 (P2)**: Como administrador, quiero dibujar un desvío cuando haya una calle bloqueada, para que el chofer sepa qué camino seguir.

## Requisitos (EARS)

### Parte 1: dibujar la ruta (panel web)
- **FR-901**: En la vista "Rutas" del panel, el administrador DEBE poder elegir una ruta y dibujar su camino dando clics sobre el mapa; cada clic agrega un punto y se ve la línea.
- **FR-902**: El panel DEBE tener los botones "Deshacer punto", "Borrar todo" y "Guardar camino".
- **FR-903**: La API DEBE guardar el camino en la ruta (`trazo`: lista de `[lat, lng]`) con `PUT /admin/rutas/:id/trazo`, solo para el administrador. DEBE rechazar con 400 un trazo con menos de 2 puntos o con coordenadas inválidas.

### Parte 2: pintar de gris lo recorrido (app y panel)
- **FR-904**: `GET /recorridos/en-curso` DEBE incluir el `trazo` de la ruta y `avance`: cuántos puntos del trazo ya se recorrieron.
- **FR-905**: Un punto del trazo cuenta como recorrido CUANDO alguna ubicación del camión, desde que empezó el recorrido, queda a menos de 40 m de él. El avance nunca regresa (si el GPS brinca hacia atrás, lo recorrido se queda gris).
- **FR-906**: La app (ciudadano, administrador y chofer) y el panel DEBEN dibujar la parte recorrida en gris y la que falta en verde, con el camión encima.
- **FR-907**: SI la ruta no tiene trazo, el mapa DEBE verse como en la v2.12 (solo el historial).

### Parte 3: desvío por bloqueo
- **FR-908**: En un recorrido en curso, el administrador DEBE poder dibujar un desvío (igual que el trazo) y escribir el motivo (por ejemplo, "Calle Hidalgo cerrada por obra"), con `PUT /admin/recorridos/:id/desvio`.
- **FR-909**: MIENTRAS haya un desvío, la pantalla del chofer DEBE mostrar el aviso "Ruta cambiada por bloqueo" con el motivo, y el mapa DEBE dibujar el desvío en ámbar con línea punteada.
- **FR-910**: CUANDO el recorrido termine, el desvío DEBE quitarse y la ruta normal se queda como estaba.
- **FR-912**: CUANDO el administrador guarde un desvío, la API DEBE crear un aviso `tipo: "desvio"` ("El camión cambió su ruta" con el motivo) para los ciudadanos de esas colonias, y la app del ciudadano DEBE mostrarlo como notificación y dibujar el desvío en ámbar en su mapa. (Petición del equipo, 2026-11-27: "deben enterarse que el camión cambió de dirección por cualquier percance".)

### General
- **FR-911**: Ningún cambio DEBE alterar lo que ya hace la v2.12.

## Criterios de éxito

- **SC-901**: `npm test` pasa con pruebas nuevas para el trazo, el avance y el desvío.
- **SC-902**: El administrador dibuja la ruta en el panel; con la simulación encendida, la app muestra cómo se va pintando de gris.
- **SC-903**: Compila en Android Studio y funciona en el emulador con las tres cuentas.

## Fuera de alcance

- Que la app calcule sola el desvío o siga calles reales (el administrador dibuja el camino a mano).
- Tiempo estimado de llegada (ETA).
- Varios camiones a la vez en el mapa del ciudadano.
