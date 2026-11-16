# Spec 004: Roles, recorridos y avisos (versión 2.8)

**Fecha**: 2026-11-11 · **Equipo**: G2VSIS07 · **Base**: specs 001, 002 y 003
**Status**: Implementada y probada en el emulador (2026-11-11) · **Constitución**: v3.0.0 (enmienda hecha antes del código)

## Por qué

Hasta la versión 2.7 el sistema muestra dónde va el camión, pero nadie lo administra: el login no revisa nada, la pantalla de gestión usa una clave fija (`admin123`), no se sabe qué chofer maneja ni qué ruta toca, y los avisos de la pantalla de Inicio son textos fijos.
El equipo pidió (2026-11-11) que el sistema funcione completo para los tres tipos de usuario: **administrador**, **chofer** y **ciudadano**.

## Clarifications

### Session 2026-11-11

- Q: ¿Dónde trabaja el administrador? → A: En el panel web (`/panel`), con su cuenta. La pantalla "Gestión de usuarios" de la app con `admin123` se retira.
- Q: ¿Cómo sabe la app si soy ciudadano o chofer? → A: Al iniciar sesión, la API revisa correo y contraseña y devuelve el rol. Ciudadano → Inicio con el mapa. Conductor → pantalla del chofer. Un administrador que entra en la app ve la pantalla del ciudadano y un aviso de que su panel está en la computadora.
- Q: ¿Quién inicia la ruta? → A: El chofer, con el botón **Iniciar ruta** de su recorrido. En ese momento se crea el aviso "El camión ya está en ruta" para los ciudadanos.
- Q: ¿Qué ve el chofer que no ve el ciudadano? → A: Su camión, la ruta asignada con sus colonias en orden, el horario, las indicaciones que escribió el administrador y los mensajes que le manda. El ciudadano nunca ve indicaciones ni mensajes internos.
- Q: ¿"Cerca de mí" con qué ubicación? → A: Con el GPS del celular del ciudadano. Cerca = menos de 500 m del camión.
- Q: ¿Cada cuánto se revisa? → A: Cada 5 minutos. Android solo deja revisar tan seguido en segundo plano con una notificación fija ("Truck on Route está vigilando el camión"), así que el ciudadano la activa con el interruptor **Avisarme cuando esté cerca** y la puede apagar.
- Q: ¿Push con Firebase? → A: No. La app pregunta a la API (`GET /avisos`) y muestra notificaciones del propio celular. Sin cuentas de Google ni librerías extra.
- Q: ¿Dónde se guardan usuarios, camiones y recorridos? → A: En memoria y en `api/datos/tor-datos.json`, que se escribe en cada cambio y se lee al arrancar. Las tablas de MySQL ya existen en `esquema.sql`; pasarlas a MySQL queda para después.
- Q: ¿Colores de la ruta (ya pasó en amarillo/rojo, por pasar en verde)? → A: Pendiente. El equipo pidió saltarlo por ahora.

## Historias de usuario

### H13: Iniciar sesión con mi cuenta (todos) · P1
- **Dado** que tengo cuenta, **cuando** escribo mi correo y contraseña correctos, **entonces** entro a la pantalla de mi rol.
- **Dado** una contraseña incorrecta, **entonces** veo "Correo o contraseña incorrectos" y no entro.
- **Dado** que la API está apagada, **entonces** veo "No se pudo conectar con el servidor" y la app no se cierra.

### H14: Crear mi cuenta (ciudadano) · P1
- El registro pide nombre, correo, teléfono, colonia y contraseña (mínimo 6 caracteres) y crea una cuenta de **ciudadano**.
- Si el correo ya existe, veo "Ese correo ya está registrado".

### H15: Dar de alta camiones y choferes (administrador) · P1
- En el panel, el administrador entra con su cuenta y da de alta un camión (clave, placas, modelo) y un chofer (nombre, correo, teléfono, contraseña inicial).
- Ve las listas de camiones, choferes y ciudadanos registrados.

### H16: Asignar el recorrido del día (administrador) · P1
- El administrador crea rutas (nombre y colonias en orden) y asigna un recorrido: camión + chofer + ruta + fecha y hora + indicaciones.
- Ve el estado de cada recorrido: Programado, En ruta o Terminado.

### H17: Mandar mensajes al chofer (administrador) · P2
- Desde el panel escribe un mensaje a un chofer ("Hoy no pases por la calle 5 de Mayo, está cerrada"). El chofer lo ve en su app en menos de 30 s.

### H18: Ver mi recorrido e iniciar la ruta (chofer) · P1
- El chofer ve su recorrido de hoy: camión, ruta, colonias en orden, hora e indicaciones; y debajo, los mensajes del administrador.
- Toca **Iniciar ruta** → el estado cambia a En ruta. Toca **Terminar ruta** → Terminado.

### H19: Saber que el camión ya salió (ciudadano) · P1
- Cuando un chofer inicia la ruta, el ciudadano ve en Inicio la tarjeta "El camión ya está en ruta: Ruta Centro (CAMION-01)" y recibe una notificación en el celular.

### H20: Que me avisen cuando está cerca (ciudadano) · P2
- Con **Avisarme cuando esté cerca** activado, cada 5 minutos la app compara la ubicación del celular con la del camión. Si están a menos de 500 m, llega la notificación "El camión está a 320 m de ti". Solo una vez por recorrido.

## Edge Cases

- Token vencido o falso: la API responde 401 y la app regresa al login.
- Un chofer intenta una ruta de administrador: 403 `{ "error": "No tienes permiso" }`.
- Asignar un camión o chofer que no existe: 400.
- Iniciar un recorrido que ya terminó: 400 "El recorrido ya terminó".
- Sin permiso de ubicación: el interruptor se apaga y se explica por qué.
- `tor-datos.json` dañado o borrado: la API arranca con los datos de ejemplo y lo avisa en la consola.
- El administrador no se puede borrar a sí mismo.

## Requisitos (EARS)

| Id | Patrón | Requisito |
|---|---|---|
| FR-401 | Por evento + No deseado | Cuando se pida `POST /sesion` con correo y contraseña, la API deberá devolver un token y el usuario con su rol. Si no coinciden, entonces la API deberá responder 401 "Correo o contraseña incorrectos". |
| FR-402 | Ubicuo | La API deberá guardar las contraseñas solo como hash `scrypt` con sal. |
| FR-403 | Por evento | Cuando se pida `POST /usuarios`, la API deberá crear una cuenta de ciudadano si el correo es válido y no existe. |
| FR-404 | No deseado | Si una ruta `/admin/*` o `/chofer/*` llega sin token válido, entonces la API deberá responder 401; si el rol no corresponde, 403. |
| FR-405 | Por evento | Cuando el administrador lo pida, la API deberá dar de alta y listar camiones, choferes, rutas y recorridos. |
| FR-406 | Por evento | Cuando el administrador mande un mensaje a un chofer, la API deberá guardarlo y entregarlo en `GET /chofer/mensajes`. |
| FR-407 | Por evento | Cuando el chofer inicie su recorrido, la API deberá cambiarlo a `en_curso` y crear un aviso "en ruta" visible en `GET /avisos`. |
| FR-408 | Por estado | Mientras la sesión sea de chofer, la app deberá mostrar la pantalla del chofer en lugar del mapa del ciudadano. |
| FR-409 | Por evento | Cuando haya un aviso nuevo, la app del ciudadano deberá mostrarlo en Inicio y como notificación del celular. |
| FR-410 | Opcional | Donde el ciudadano active "Avisarme cuando esté cerca", la app deberá revisar cada 5 minutos y notificar una vez por recorrido si el camión está a menos de 500 m. |
| FR-411 | Ubicuo | La API deberá guardar los datos de la Fase 3 en `api/datos/tor-datos.json` y leerlos al arrancar. |

## Success Criteria

- **SC-401**: `npm test` pasa todas las pruebas, las de antes y las nuevas de sesión, permisos, altas, recorridos y avisos.
- **SC-402**: Con la demo, el administrador da de alta un chofer, le asigna un recorrido y le manda un mensaje; el chofer lo ve en la app en menos de 30 s.
- **SC-403**: El chofer toca Iniciar ruta y el ciudadano ve el aviso en menos de 30 s con la app abierta.
- **SC-404**: Con la simulación y el emulador colocado a menos de 500 m de la ruta, llega la notificación "camión cerca" en máximo 5 minutos.
- **SC-405**: Reiniciar la API no borra usuarios, camiones ni recorridos.

## Assumptions

- La demo corre en la computadora del equipo (`localhost`). En Hostinger se usaría HTTPS para cuidar las contraseñas.
- Un camión por recorrido y un recorrido en curso por camión.
- La ruta del chofer es una lista de colonias, no un trazo en el mapa (el trazo y sus colores son otra spec).

## Fuera de alcance

Colores de la ruta según avance, ETA, Firebase, recuperar contraseña, varios camiones en el mapa del ciudadano.
