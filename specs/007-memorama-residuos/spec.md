# Feature Specification: Memorama de residuos "¿Dónde va?" (versión 2.11)

**Feature Branch**: `version-2.11` (sale de `version-2.10`) · **Created**: 2026-11-25 · **Status**: Aprobada (maqueta aprobada por el equipo el 2026-11-25)

**Input**: "Hagamos algo más pro, agreguemos un mini juego tipo memorama para los usuarios en donde te ponga un ejemplo sobre el tipo de residuos y su clasificación." (Oswaldo, 2026-11-25)

## Contexto

La app ya le dice al ciudadano cuándo pasa el camión. Falta enseñarle a separar bien su basura antes de sacarla. Un memorama corto, que se juega sin internet, sirve para eso y hace la app más atractiva. Como un juego no estaba en el alcance, se hizo la enmienda 4.1.0 de la constitución (Fase 4) antes de escribir código.

## Historia de usuario

**H23 (P1)**: Como ciudadano, quiero jugar un memorama con ejemplos de residuos, para aprender en qué categoría va cada uno y separar mejor mi basura.

**Prueba independiente**: entrar con la cuenta de ciudadano, abrir "Juego: ¿Dónde va?" desde el menú, terminar una partida y ver que cada par encontrado explica su categoría y que al final sale el resumen con estrellas.

## Categorías (con su color dentro del juego)

| Categoría | Color | Ejemplos del juego |
|---|---|---|
| Orgánicos | Verde | cáscara de plátano, restos de comida, cáscara de huevo, hojas secas, café molido usado |
| Inorgánicos reciclables | Gris | botella de plástico (PET), periódico, caja de cartón, lata de aluminio, envase de jugo (tetrapak) |
| Inorgánicos no reciclables | Naranja | pañal usado, servilleta usada, charola de unicel, envoltura de dulce, colilla de cigarro |
| Manejo especial | Café | pila, celular viejo, foco ahorrador, aceite de cocina usado, medicinas caducadas |

Los colores son los que usa la norma de separación de residuos en México (NADF-024-AMBT-2013). Se usan solo dentro del juego; el resto de la app conserva sus verdes (principio III).

## Requisitos (EARS)

- **FR-701**: El menú lateral (cuentas de ciudadano y administrador) DEBE tener la opción "Cuida tu ambiente". Abre una sección con tarjetas de actividades: la primera es el juego "¿Dónde va?" y la segunda, "Composta en casa", aparece como "Próximamente" (sin contenido todavía). Cambio pedido por el equipo el 2026-11-25 para que la sección pueda crecer con nuevas dinámicas.
- **FR-702**: La pantalla de inicio del juego DEBE explicar en una frase cómo se juega, mostrar las 4 categorías con su color y tener el botón "Jugar".
- **FR-703**: Cada partida DEBE tener 12 cartas boca abajo (6 pares) en una cuadrícula de 3 × 4. Los 6 residuos se eligen al azar de la lista de 20 y las cartas se revuelven en cada partida.
- **FR-704**: CUANDO el usuario toque una carta, la carta DEBE voltearse con una animación corta (menos de 300 ms) y mostrar el dibujo y el nombre del residuo.
- **FR-705**: CUANDO las dos cartas volteadas sean iguales, DEBEN quedarse boca arriba con el borde del color de su categoría y DEBE aparecer una tarjeta con la categoría y un consejo corto (por ejemplo: "Pila → Manejo especial. Llévala a un punto de acopio, nunca a la basura.").
- **FR-706**: CUANDO las dos cartas no sean iguales, DEBEN regresar boca abajo después de 1 segundo.
- **FR-707**: MIENTRAS haya dos cartas volteadas o la tarjeta del consejo esté abierta, el usuario NO DEBE poder voltear otra carta.
- **FR-708**: Arriba del tablero DEBEN verse los intentos, el tiempo y los pares encontrados ("3 de 6").
- **FR-709**: CUANDO se encuentren los 6 pares, DEBE aparecer un resumen con 1 a 3 estrellas según los intentos (3 estrellas con 9 intentos o menos, 2 con 10 a 14, 1 con 15 o más), el tiempo y los 6 residuos agrupados por categoría, con los botones "Jugar otra vez" y "Salir".
- **FR-710**: El juego DEBE funcionar sin internet, sin la API y sin guardar nada en el servidor. Las imágenes de los residuos son emojis del sistema, sin archivos nuevos.
- **FR-711**: El juego DEBE usar la letra Poppins, las piezas de la v2.10 (encabezado verde, botón píldora, animación al presionar) y los colores de la app fuera de las categorías.
- **FR-712**: Ningún cambio DEBE alterar lo que ya hace la v2.10 (mapa, avisos, cercanía, chofer, perfil).

## Criterios de éxito

- **SC-701**: Compila en Android Studio sin errores.
- **SC-702**: En el emulador se juega una partida completa con la cuenta de ciudadano y otra con la de administrador, sin que la app se cierre.
- **SC-703**: Cada uno de los 20 residuos tiene su categoría y su consejo correctos (revisado por el equipo con la tabla de arriba).

## Ideas para después (no entran en esta spec)

El equipo quiere que "Cuida tu ambiente" crezca con más dinámicas y tareas que animen a la población a cuidar el medio ambiente (2026-11-25). Ejemplo: una guía para hacer composta en casa. Cada una necesita su propia spec y, si sale del alcance de la Fase 4, una enmienda.

## Fuera de alcance

- Guardar récords o puntajes en la API o compartirlos.
- Más niveles, sonidos o música.
- El juego en el panel web.
