# Feature Specification: Rediseño visual moderno (versión 2.10)

**Feature Branch**: `version-2.10` (sale de `version-2.9`) · **Created**: 2026-11-24 · **Status**: Aprobada (maqueta aprobada por el equipo el 2026-11-24, constitución v4.0.0 del 2026-11-25)

**Input**: "Ahora vamos a actualizar lo visual de la app, se ve muy acartonada y la verdad se ve aburrida." (Oswaldo, 2026-11-24)

## Contexto

La v2.9 (spec 005) agregó sombras y tarjetas sin cambiar el acomodo de las pantallas. El equipo, al recorrerla en el emulador el 2026-11-24, siente que sigue viéndose rígida. Este rediseño sí cambia el acomodo, la tipografía y agrega animaciones, por eso choca con el principio III de la constitución ("el diseño se conserva") y necesita una enmienda antes de escribir código.

## Historia de usuario

**H22 (P1)**: Como ciudadano, chofer o administrador, quiero una app con un diseño moderno, con el mapa como protagonista y movimiento suave, para que se sienta actual y agradable de usar.

**Prueba independiente**: abrir la app en el emulador y recorrer login, registro, inicio, perfil y pantalla del chofer con las tres cuentas; todo funciona igual que en la v2.9 y se ve como la maqueta aprobada.

## Requisitos (EARS)

- **FR-601**: La pantalla de inicio DEBE mostrar el mapa a casi toda la pantalla, con la información del camión (estado, velocidad, recorrido, puntos y avisos) en una hoja inferior que se puede deslizar.
- **FR-602**: La app DEBE usar una tipografía con más personalidad para títulos y cifras (propuesta: Poppins, incluida en el proyecto, sin descargarla en cada inicio).
- **FR-603**: El estado del camión DEBE verse como una etiqueta de color (En ruta, Programado, Sin señal, Simulación).
- **FR-604**: CUANDO el camión cambie de posición, el marcador DEBE moverse con una animación suave en lugar de saltar.
- **FR-605**: CUANDO una pantalla o tarjeta aparezca, DEBE entrar con una animación corta (menos de 300 ms); los botones DEBEN responder al tocarlos.
- **FR-606**: Los estados vacíos ("Sin avisos", "Sin mensajes") DEBEN tener un ícono grande y un texto amable.
- **FR-607**: La app DEBE conservar los colores de la v2.9 (verde 0xFF019B43, verde oscuro 0xFF0A7D2C, fondos claros, blanco, grises, amarillo y rojo de avisos). Decisión del equipo, 2026-11-24.
- **FR-608**: Ningún cambio DEBE alterar el comportamiento de la v2.9 (login, roles, avisos, cercanía, mapa, Perfil con la cuenta de la sesión).
- **FR-609** (agregado 2026-11-25 a petición del equipo): CUANDO se abra la app, DEBE mostrarse el logo con el nombre por unos 1.6 s; CUANDO el usuario inicie sesión, DEBE verse un camioncito animado estilo caricatura (avanza, parpadea, echa humo, levanta la tolva y tira bolsas) con un saludo por unos 2.4 s antes de entrar a su pantalla.
- **FR-610** (agregado 2026-11-25 a petición del equipo): La app DEBE tener su propio ícono en el celular (pin de ubicación blanco con el camión, sobre el degradado de los verdes de la app; el mismo logo se ve al abrir la app) en lugar del ícono genérico de Android.
- **FR-611** (agregado 2026-11-25, mejoras pedidas por el equipo): El camión en el mapa DEBE verse como el camión del logo dentro de un círculo verde, con un pulso que crece y se desvanece.
- **FR-612**: SI el usuario dio permiso de ubicación, el mapa DEBE mostrar un punto azul en su ubicación (la última que conoce el celular).
- **FR-613**: En las pantallas con fondo verde (login, registro, bienvenida, chofer y perfil), la hora y la batería de arriba DEBEN verse en blanco.
- **FR-614**: MIENTRAS llegan los datos, DEBEN verse rectángulos grises que brillan (esqueletos) en lugar de un círculo girando.
- **FR-615**: CUANDO se cambie entre Inicio y Perfil, la pantalla DEBE deslizarse suavemente.
- **FR-616**: CUANDO llegue un aviso nuevo con la app abierta, DEBE aparecer con un rebote y el celular DEBE vibrar un poco.
- **FR-617**: CUANDO la ruta esté en curso, el chofer DEBE poder tocar cada colonia al terminarla y ver una barra de avance ("2 de 4 colonias"). Es solo en su pantalla; no se guarda en la API.
- **FR-618**: El menú lateral DEBE mostrar el logo nuevo y el nombre, la inicial y el correo de quien inició sesión.

## Fuera de alcance
- Modo oscuro.
- El panel web del administrador (solo la app Android).
- Funciones nuevas (ETA, varios camiones, etc.).

## Criterios de éxito
- **SC-601**: El proyecto compila en Android Studio sin errores.
- **SC-602**: El equipo aprueba la maqueta antes de programar y confirma en el emulador que la app ya no se ve "acartonada".
- **SC-603**: Las tres cuentas siguen funcionando igual que en la v2.9.

## Riesgo
- La entrega final es el 2026-11-30. Si el rediseño no queda probado a tiempo, se entrega la v2.9 (ya probada) y la v2.10 queda como mejora.
