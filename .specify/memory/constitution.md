<!--
Informe de sincronización (Sync Impact Report)
- Versión: 4.2.0 → 4.3.0 (MINOR: rutas dibujadas en el mapa)
- Principio modificado:
  - I. Alcance por fases → se agrega la Fase 5 (spec 009): el administrador dibuja la ruta en el panel, el mapa pinta de gris lo que el camión ya recorrió y el administrador puede marcar un desvío por bloqueo. Sale de "fuera de alcance" el pintado de la ruta.
- Principios sin cambio: II, III, IV y V
- Specs revisadas: specs/009-rutas-dibujadas (nueva)
- Versiones anteriores guardadas en: .specify/memory/historial/ (v1.0.0 a v4.2.0)
-->

# Truck on Rute Constitution

## Core Principles

### I. Alcance por fases

**Fase 1 (spec 001):** la cadena GPS NEO-6M → ESP32 → (WiFi, luego SIM800L) → API Node.js → mapa en la app Android.

**Fase 2 (spec 002):** sobre lo anterior se permite historial de ubicaciones, velocidad y distancia calculadas, panel web del administrador (solo lectura) y guardado opcional en MySQL.

**Fase 3 (spec 004):** cuentas con rol (ciudadano, conductor y administrador) e inicio de sesión real contra la API; el administrador da de alta camiones, choferes y rutas, asigna recorridos y manda indicaciones al chofer desde el panel web; el chofer ve su recorrido e inicia o termina la ruta desde la app; el ciudadano recibe el aviso "el camión ya está en ruta" y, si lo activa, un aviso cuando el camión está a menos de 500 m de su celular.

**Fase 4 (spec 007):** un minijuego educativo tipo memorama dentro de la app Android, para que el ciudadano aprenda a separar sus residuos (orgánicos, inorgánicos reciclables, inorgánicos no reciclables y de manejo especial). Funciona sin internet, no usa la API y no guarda nada en el servidor. Los colores de las 4 categorías (verde, gris, naranja y café, como en la norma de separación de residuos) se usan solo dentro del juego.

También en la Fase 4 (spec 008): un aviso de privacidad que explica qué datos personales se piden, para qué y cómo pedir que se corrijan o borren. Se acepta al crear la cuenta, se puede leer desde el menú de la app y está en el panel web en `/privacidad`.

**Fase 5 (spec 009):** el administrador dibuja en el mapa del panel web el camino de cada ruta (una línea con puntos). La app y el panel muestran esa línea en verde y pintan de gris la parte que el camión ya recorrió en el recorrido en curso. Si una calle está bloqueada, el administrador dibuja un desvío para ese recorrido; el chofer lo ve en su pantalla con un aviso y la ruta normal regresa al terminar el recorrido. El gris y el ámbar del desvío se usan solo en el mapa.

Siguen fuera de alcance: carrito con motores, panel solar, MPU6050, AHT20-BMP280, tiempo estimado de llegada (ETA), notificaciones push con Firebase, varios camiones a la vez en el mapa del ciudadano y nube. Agregar cualquiera de estos requiere una nueva enmienda y preguntar al equipo antes de escribir código.

### II. Tres partes separadas y un contrato

`firmware/` (C++ con Arduino IDE, TinyGPSPlus por Serial2 a 9600), `api/` (Node.js LTS + Express, JavaScript) y `app-android/` (la app original en Kotlin + Jetpack Compose). El panel web del administrador vive dentro de `api/public/` y lo sirve la misma API.

La API funciona siempre en memoria. Los datos de la Fase 3 (usuarios, camiones, rutas, recorridos, mensajes y avisos) además se guardan en `api/datos/tor-datos.json` (no se sube a git) para que no se pierdan al reiniciar. MySQL es opcional: solo se usa si `.env` tiene `DB_HOST`, y si falla la API sigue funcionando en memoria. Todas las partes usan el formato de `contracts/api-ubicacion.md` (y las rutas nuevas de `contracts/api-v2.6.md` y `specs/004-roles-recorridos-avisos/contracts/api-v3.md`); cada ubicación lleva `fuente` (`gps` o `simulacion`).

### III. Partir de lo que existe

La app original se cambia solo lo necesario. Los **colores** (verde 0xFF019B43, verde oscuro 0xFF0A7D2C, fondos claros, blanco, grises, y el amarillo y rojo de los avisos) y el **logo** se conservan.

Desde v4.0.0 (spec 006): se puede cambiar el acomodo de las pantallas, la tipografía (una fuente incluida en la app, sin descargarla) y agregar animaciones cortas, siempre que la maqueta la apruebe el equipo antes de programar y que no cambie lo que hace la app (login, roles, avisos, cercanía y mapa).

Excepción (desde v2.1.0, spec 003): el login y el registro pueden **validar el formato del correo** (que tenga `@`, nombre y un dominio con extensión, como `nombre@gmail.com`) y mostrar un mensaje si no es válido. Desde v3.0.0 (spec 004) el login sí es real: se valida el formato y luego la API revisa el correo y la contraseña y devuelve el rol. El registro crea una cuenta de ciudadano. La pantalla "Gestión de usuarios" con la clave fija `admin123` se retira: el administrador trabaja en el panel web con su propia cuenta.

### IV. Sencillo, seguro y en español

Código corto, comentado en español, que el equipo pueda explicar en clase. Sin librerías extra para lo que se resuelve con pocas líneas. Los datos personales (nombre, correo, teléfono y colonia) solo se usan como dice el aviso de privacidad, y la ubicación del celular del ciudadano no sale del celular. Sin secretos en git (`secrets.h`, `.env`, `local.properties`). La API no se conecta a MySQL como `root`, sino con un usuario que solo tiene permisos sobre `truck_on_route`. Las contraseñas nunca se guardan en texto: se guarda un hash con `scrypt` (incluido en Node, sin librerías). Las rutas de administrador y de chofer piden un token de sesión y revisan el rol. Errores claros: la API responde `{ "error": "..." }` con 400/401/403/404; la app nunca se cierra por falta de datos.

### V. Una etapa a la vez, con prueba

La validación y las rutas de la API tienen tests con `node --test`. Cada etapa del hardware tiene una prueba física. Una etapa termina solo cuando su prueba pasa y el equipo lo confirma; al terminar se escribe un resumen para Jira. El SIM800L nunca se alimenta del ESP32; no se compra nada sin comprobar que hace falta.

## Flujo de Desarrollo

- Especificaciones, aclaraciones, planes y tareas con Spec Kit (`/speckit.specify`, `/speckit.clarify`, `/speckit.plan`, `/speckit.tasks`, `/speckit.implement`), en ese orden. No se escribe código de una spec nueva sin su `plan.md` y su Constitution Check.
- Si algo se hizo antes de documentarlo, se registra después con la fecha real y se marca como "registrado después". No se cambian fechas para que parezca que fue antes.
- Documentos, comentarios y mensajes de commit en español; identificadores de código en inglés o español, consistentes.
- No borrar ni modificar archivos fuera de la carpeta del proyecto (hoy `C:\Melendez\TruckOR.v7`, repositorio `oswaldoaguilar12/truck-on-route`).

## Governance

- Esta constitución prevalece sobre cualquier otra práctica del proyecto. Su origen es `reglas.md`.
- Las enmiendas se documentan con fecha y motivo (MAJOR: quitar o redefinir un principio; MINOR: agregar uno; PATCH: redacción). La versión anterior se guarda en `.specify/memory/historial/`.

## Historial de enmiendas

| Versión | Fecha | Tipo | Motivo |
|---|---|---|---|
| 1.0.0 | 2026-10-22 | Inicial | Creada con `/speckit.constitution` a partir de `reglas.md`. |
| 2.0.0 | 2026-11-02 | MAJOR | La versión 2.6 (spec 002, implementada el 2026-10-23 a petición del profesor) agregó historial, panel de administrador y MySQL, que la v1.0.0 dejaba fuera de alcance. La auditoría del 2026-11-02 lo detectó y esta enmienda lo formaliza (registrado después). Se redefinen los principios I y II, se agrega la regla del usuario de MySQL sin `root` (IV) y la regla de registrar con fecha real lo que se documenta después. |
| 2.1.0 | 2026-11-02 | MINOR | Al probar la app, el equipo vio que el login aceptaba textos como `hola.com` (sin `@`). Se permite validar el formato del correo en login y registro (spec 003). La enmienda se hizo antes de escribir el código. |
| 3.0.0 | 2026-11-11 | MAJOR | El equipo pidió que el sistema funcione completo para administrador, chofer y ciudadano: login real con rol, alta de camiones y choferes, recorridos, indicaciones al chofer y avisos de "camión en ruta" y "camión cerca". Se redefinen los principios I y III, y se agregan reglas de guardado (II) y de contraseñas y sesiones (IV). La enmienda se hizo antes de escribir el código (spec 004). |
| 4.0.0 | 2026-11-25 | MAJOR | El equipo, al recorrer la v2.9 el 2026-11-24, dijo que la app "se ve muy acartonada y aburrida" y pidió un diseño más llamativo. Aprobó la maqueta del rediseño (Login, Inicio, Chofer y Perfil) y pidió mantener los verdes. Se redefine el principio III: se conservan colores y logo, y se permite cambiar acomodo, tipografía y animaciones (spec 006). La enmienda se hizo antes de escribir el código. |
| 4.1.0 | 2026-11-25 | MINOR | El equipo pidió el 2026-11-25 "un mini juego tipo memorama para los usuarios" que enseñe los tipos de residuos y su clasificación. Se agrega la Fase 4 al principio I (spec 007). No cambia la API ni el panel web. La enmienda se hizo antes de escribir el código. |
| 4.2.0 | 2026-11-27 | MINOR | El equipo pidió el 2026-11-26 "meterle las políticas de privacidad". La app pide datos personales al registrarse, así que se agrega el aviso de privacidad (Ley Federal de Protección de Datos Personales en Posesión de los Particulares) a la Fase 4 y una regla en el principio IV (spec 008). La enmienda se hizo antes de escribir el código. |
| 4.3.0 | 2026-11-27 | MINOR | El equipo pidió el 2026-11-27 que el mapa pinte de gris por donde ya pasó el camión, que el administrador dibuje las rutas y que pueda marcar un desvío cuando haya un bloqueo. Se agrega la Fase 5 al principio I y se quita de "fuera de alcance" el pintado de la ruta (spec 009). La enmienda se hizo antes de escribir el código. |

**Version**: 4.3.0 | **Ratified**: 2026-10-22 | **Last Amended**: 2026-11-27
