<!--
Informe de sincronización (Sync Impact Report)
- Versión: 2.0.0 → 2.1.0 (MINOR: se agrega al principio III una excepción acotada: validar el formato del correo)
- Versión anterior: 1.0.0 → 2.0.0 (MAJOR: se redefinen los principios I y II)
- Principios modificados:
  - I. Alcance acotado → I. Alcance por fases (ahora incluye historial, panel de administrador y MySQL opcional)
  - II. Tres partes separadas y un contrato → se agrega el panel web dentro de api/ y la persistencia opcional
  - IV. Se agrega: MySQL con usuario propio, nunca root
- Principios sin cambio: III, V
- Secciones agregadas: "Historial de enmiendas"
- Specs revisadas: specs/002-mas-funciones (se le agregó plan.md con Constitution Check)
- Versiones anteriores guardadas en: .specify/memory/historial/ (v1.0.0 y v2.0.0)
-->

# Truck on Rute Constitution

## Core Principles

### I. Alcance por fases

**Fase 1 (spec 001):** la cadena GPS NEO-6M → ESP32 → (WiFi, luego SIM800L) → API Node.js → mapa en la app Android.

**Fase 2 (spec 002):** sobre lo anterior se permite historial de ubicaciones, velocidad y distancia calculadas, panel web del administrador (solo lectura) y guardado opcional en MySQL.

Siguen fuera de alcance: carrito con motores, panel solar, MPU6050, AHT20-BMP280, login real (de ciudadano o de administrador), tiempo estimado de llegada (ETA), notificaciones, varios camiones a la vez en la app y nube. Agregar cualquiera de estos requiere una nueva enmienda y preguntar al equipo antes de escribir código.

### II. Tres partes separadas y un contrato

`firmware/` (C++ con Arduino IDE, TinyGPSPlus por Serial2 a 9600), `api/` (Node.js LTS + Express, JavaScript) y `app-android/` (la app original en Kotlin + Jetpack Compose). El panel web del administrador vive dentro de `api/public/` y lo sirve la misma API.

La API funciona siempre en memoria. MySQL es opcional: solo se usa si `.env` tiene `DB_HOST`, y si falla la API sigue funcionando en memoria. Todas las partes usan el formato de `contracts/api-ubicacion.md` (y las rutas nuevas de `contracts/api-v2.6.md`); cada ubicación lleva `fuente` (`gps` o `simulacion`).

### III. Partir de lo que existe

La app original se cambia solo lo necesario para mostrar el mapa. Login, registro, perfil y gestión conservan su comportamiento.

Excepción (desde v2.1.0, spec 003): el login y el registro pueden **validar el formato del correo** (que tenga `@`, nombre y un dominio con extensión, como `nombre@gmail.com`) y mostrar un mensaje si no es válido. Esto no es un login real: no se consulta ninguna base de datos ni se verifica que la cuenta exista.

### IV. Sencillo, seguro y en español

Código corto, comentado en español, que el equipo pueda explicar en clase. Sin librerías extra para lo que se resuelve con pocas líneas. Sin secretos en git (`secrets.h`, `.env`, `local.properties`). La API no se conecta a MySQL como `root`, sino con un usuario que solo tiene permisos sobre `truck_on_route`. Errores claros: la API responde `{ "error": "..." }` con 400/404; la app nunca se cierra por falta de datos.

### V. Una etapa a la vez, con prueba

La validación y las rutas de la API tienen tests con `node --test`. Cada etapa del hardware tiene una prueba física. Una etapa termina solo cuando su prueba pasa y el equipo lo confirma; al terminar se escribe un resumen para Jira. El SIM800L nunca se alimenta del ESP32; no se compra nada sin comprobar que hace falta.

## Flujo de Desarrollo

- Especificaciones, aclaraciones, planes y tareas con Spec Kit (`/speckit.specify`, `/speckit.clarify`, `/speckit.plan`, `/speckit.tasks`, `/speckit.implement`), en ese orden. No se escribe código de una spec nueva sin su `plan.md` y su Constitution Check.
- Si algo se hizo antes de documentarlo, se registra después con la fecha real y se marca como "registrado después". No se cambian fechas para que parezca que fue antes.
- Documentos, comentarios y mensajes de commit en español; identificadores de código en inglés o español, consistentes.
- No borrar ni modificar archivos fuera de la carpeta TruckOnRute2.5.

## Governance

- Esta constitución prevalece sobre cualquier otra práctica del proyecto. Su origen es `reglas.md`.
- Las enmiendas se documentan con fecha y motivo (MAJOR: quitar o redefinir un principio; MINOR: agregar uno; PATCH: redacción). La versión anterior se guarda en `.specify/memory/historial/`.

## Historial de enmiendas

| Versión | Fecha | Tipo | Motivo |
|---|---|---|---|
| 1.0.0 | 2026-10-22 | Inicial | Creada con `/speckit.constitution` a partir de `reglas.md`. |
| 2.0.0 | 2026-11-02 | MAJOR | La versión 2.6 (spec 002, implementada el 2026-10-23 a petición del profesor) agregó historial, panel de administrador y MySQL, que la v1.0.0 dejaba fuera de alcance. La auditoría del 2026-11-02 lo detectó y esta enmienda lo formaliza (registrado después). Se redefinen los principios I y II, se agrega la regla del usuario de MySQL sin `root` (IV) y la regla de registrar con fecha real lo que se documenta después. |
| 2.1.0 | 2026-11-02 | MINOR | Al probar la app, el equipo vio que el login aceptaba textos como `hola.com` (sin `@`). Se permite validar el formato del correo en login y registro (spec 003). La enmienda se hizo antes de escribir el código. |

**Version**: 2.1.0 | **Ratified**: 2026-10-22 | **Last Amended**: 2026-11-02
