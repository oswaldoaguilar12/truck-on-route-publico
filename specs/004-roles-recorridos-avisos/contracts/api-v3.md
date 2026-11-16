# Contrato API v3 (spec 004)

Todas las respuestas son JSON. Errores: `{ "error": "mensaje" }` con 400, 401, 403 o 404.
Las rutas marcadas con 🔒 piden el encabezado `Authorization: Bearer <token>`.

## Sesión y cuentas

| Método y ruta | Quién | Cuerpo | Respuesta |
|---|---|---|---|
| `POST /usuarios` | cualquiera | `{ nombre, correo, telefono, colonia, contrasena }` | 201 `{ usuario }` (rol `ciudadano`) |
| `POST /sesion` | cualquiera | `{ correo, contrasena }` | 200 `{ token, usuario }` |
| `GET /sesion` 🔒 | cualquiera con sesión | | `{ usuario }` |
| `DELETE /sesion` 🔒 | cualquiera con sesión | | `{ ok: true }` |

`usuario` = `{ id, nombre, correo, telefono, colonia, rol }` (nunca incluye la contraseña). `rol`: `ciudadano`, `conductor` o `administrador`.

## Administrador 🔒 (rol `administrador`)

| Método y ruta | Cuerpo | Respuesta |
|---|---|---|
| `GET /admin/resumen` | | `{ camiones, choferes, ciudadanos, recorridosHoy, enRuta }` |
| `GET /admin/camiones` · `POST /admin/camiones` | `{ clave, placas, modelo }` | lista · 201 camión |
| `DELETE /admin/camiones/:id` | | `{ ok: true }` |
| `GET /admin/choferes` · `POST /admin/choferes` | `{ nombre, correo, telefono, contrasena }` | lista · 201 usuario (rol `conductor`) |
| `GET /admin/usuarios` | | todos los usuarios |
| `DELETE /admin/usuarios/:id` | | `{ ok: true }` (no se puede borrar a sí mismo) |
| `GET /admin/rutas` · `POST /admin/rutas` | `{ nombre, colonias: [..], horario }` | lista · 201 ruta |
| `GET /admin/recorridos` · `POST /admin/recorridos` | `{ camionId, choferId, rutaId, fecha, indicaciones }` | lista · 201 recorrido |
| `GET /admin/mensajes` · `POST /admin/mensajes` | `{ choferId, texto }` | lista · 201 mensaje |

## Chofer 🔒 (rol `conductor`)

| Método y ruta | Respuesta |
|---|---|
| `GET /chofer/recorridos` | sus recorridos de hoy y los siguientes, con `camion`, `ruta` e `indicaciones` |
| `POST /chofer/recorridos/:id/iniciar` | recorrido con `estado: "en_curso"`; crea un aviso |
| `POST /chofer/recorridos/:id/terminar` | recorrido con `estado: "terminado"` |
| `GET /chofer/mensajes` | mensajes del administrador, del más nuevo al más viejo |

## Ciudadano (sin sesión)

| Método y ruta | Respuesta |
|---|---|
| `GET /avisos?desde=<ISO>` | avisos después de esa fecha: `{ id, tipo: "en_ruta", titulo, mensaje, recorridoId, camion, ruta, colonias, fecha }` |
| `GET /recorridos/en-curso` | recorridos en ruta ahora: `{ id, camion, ruta, colonias, inicio }` (sin indicaciones ni chofer) |

Las rutas de antes (`/ubicacion`, `/ubicacion/historial`, `/estadisticas`, `/camiones`, `/colonias`, `/panel`) no cambian.
