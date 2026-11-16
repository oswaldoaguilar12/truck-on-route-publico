# Implementation Plan: Roles, recorridos y avisos

**Spec**: [spec.md](spec.md) · **Fecha**: 2026-11-11 · **Constitución**: v3.0.0

## Summary

La API agrega cuentas con rol, sesiones con token y las altas del administrador, guardadas en `api/datos/tor-datos.json`. El panel web agrega un inicio de sesión y pestañas de administración. La app consulta la API para iniciar sesión, muestra la pantalla del chofer o la del ciudadano según el rol, y avisa con notificaciones del celular.

## Technical Context

| Parte | Decisión | Por qué |
|---|---|---|
| Contraseñas | `crypto.scryptSync` con sal aleatoria de 16 bytes | Viene en Node; sin librerías (IV) |
| Sesión | Token aleatorio de 32 bytes (`crypto.randomBytes`), en memoria, dura 7 días | Simple de explicar; no hace falta JWT |
| Guardado | `datos.js`: objeto en memoria + `fs.writeFileSync` a `api/datos/tor-datos.json` en cada cambio | Sobrevive reinicios sin instalar MySQL (II) |
| Datos de ejemplo | Al primer arranque: 1 administrador, 1 chofer, 1 ciudadano, `CAMION-01` y la "Ruta Centro" | La demo funciona desde el inicio |
| Cuenta admin | Correo y contraseña de `.env` (`ADMIN_CORREO`, `ADMIN_CONTRASENA`); si no hay, los de ejemplo de `LEEME.md` | Sin secretos en git (IV) |
| Panel | Mismo `index.html` con pestañas: Monitoreo, Camiones, Choferes, Rutas, Recorridos, Mensajes, Usuarios. Token en `sessionStorage` | Sin frameworks |
| App: red | `ApiTor.kt` con `HttpURLConnection` y `org.json`, igual que `UbicacionApi.kt` | Sin Retrofit (IV) |
| App: sesión | `SharedPreferences` guarda token y usuario | Viene en Android |
| App: notificaciones | `NotificationManager` + canal "Avisos del camión"; permiso `POST_NOTIFICATIONS` (Android 13+) | Sin Firebase |
| App: "camión cerca" | `ServicioCercania`: servicio en primer plano tipo `location` que cada 5 min lee el GPS con `LocationManager`, pide `/recorridos/en-curso` y `/ubicacion?camion=` y calcula la distancia con `Location.distanceBetween` | Android limita las tareas en segundo plano a 15 min; con servicio visible sí se puede cada 5 min |
| Distancia | Haversine ya está en `api/geo.js`; en la app se usa `Location.distanceBetween` | No se repite código |

## Constitution Check (v3.0.0)

- I Alcance: ✅ todo está en la Fase 3; los colores de la ruta siguen fuera.
- II Partes y guardado: ✅ API + panel en `api/`; app en `app-android/`; el firmware no cambia. Datos en memoria + JSON.
- III Partir de lo que existe: ✅ mismo diseño; se reemplazan textos fijos (notificación y mensaje de ejemplo) por datos reales y se retira `admin123`.
- IV Seguro: ✅ hash scrypt, token, roles, `.env` para la cuenta admin, `tor-datos.json` en `.gitignore`.
- V Con prueba: ✅ pruebas `node --test` para sesión, permisos, altas, recorridos y avisos; prueba manual en emulador.

## Archivos

```text
api/
├── datos.js            # nuevo: usuarios, camiones, rutas, recorridos, mensajes, avisos + guardado JSON
├── seguridad.js        # nuevo: hash de contraseña y tokens
├── rutas-v3.js         # nuevo: rutas de sesión, admin, chofer y avisos
├── app.js              # monta rutas-v3 y permite el encabezado Authorization
├── public/index.html   # panel con login y pestañas de administración
└── test/v3.test.js     # nuevo
app-android/app/src/main/java/com/example/truckonroute/
├── data/ApiTor.kt              # nuevo: sesión, registro, chofer, avisos
├── data/Sesion.kt              # nuevo: guarda token y usuario
├── avisos/Notificaciones.kt    # nuevo: canal y notificaciones
├── avisos/ServicioCercania.kt  # nuevo: revisión cada 5 min
├── ui/ChoferScreen.kt          # nuevo
├── MainActivity.kt             # login real y navegación por rol
├── ui/RegistroScreen.kt        # registro real
└── ui/HomeScreen.kt            # avisos reales, interruptor de cercanía, sin admin123
```
