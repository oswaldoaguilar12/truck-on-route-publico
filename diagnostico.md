# Diagnóstico — Etapa 0

**Fecha:** 2026-10-22
**Estado:** casi completo (faltan la tabla de conexiones y las versiones instaladas)

## 1. Material revisado

| Fuente | Qué es |
|---|---|
| `C:\Melendez` | Material del profe: apuntes de SDD, constituciones de ejemplo y la práctica `task-cli` terminada con Spec Kit. Solo se leyó, no se cambió nada. |
| `TruckOnRoute.zip` | El Truck on Rute original: **una app Android** (Kotlin + Jetpack Compose), hecha en Android Studio. |
| `prompt-maestro-v2.md` | La descripción del proyecto, el alcance y las etapas. |

## 2. Qué hay en el Truck on Rute original

Proyecto Android `com.example.truckonroute` (minSdk 24, targetSdk 36, Material 3, Coil para imágenes). Una copia sin archivos de compilación está en `app-android/`.

| Archivo | Qué hace | ¿Funciona de verdad? |
|---|---|---|
| `MainActivity.kt` | Arranca la app. Navega entre Login, Registro y Home. También tiene su propia `LoginScreen`, una `RegisterScreen` y componentes reutilizables. | Sí, la navegación funciona. |
| `ui/LoginScreen.kt` | Otra versión de la pantalla de login. | **No se usa:** `MainActivity` usa la suya. |
| `ui/RegistroScreen.kt` | Formulario de registro. | Solo visual, no guarda nada. |
| `ui/HomeScreen.kt` | Pantalla principal: menú lateral (Inicio, Gestión de usuarios, Ayuda, Cerrar sesión), barra inferior (Inicio, Perfil), buscador de colonias, tarjeta del camión, notificaciones y mensajes. | Solo visual. El camión es un ícono animado y dice "llega en 23 min" fijo. |
| `ui/GestionUsariosScreen.kt` | Lista de usuarios para el administrador. | Solo visual. |
| `ui/PerfilScreen.kt` | Perfil con foto (elegida de la galería) y datos editables. | Visual, los datos no se guardan. |
| `ui/TruckCard.kt` | Vacío (solo la línea `package`). | — |
| `ui/theme/` | Colores, tema y tipografía. | Sí. |
| `res/drawable/camion.png` | Imagen del camión. | Se puede usar como ícono en el mapa. |

### Lo más importante para esta fase

- **No hay mapa.** La app no muestra ninguna ubicación real todavía.
- **No hay conexión a internet:** el `AndroidManifest.xml` no tiene el permiso `INTERNET`.
- **No hay API ni firmware en el zip.** El prompt maestro habla de `app.js` / `server.js` y de sketches del ESP32, pero no vienen aquí. Si existen en otra carpeta, conviene revisarlos; si no, se crean nuevos en esta fase.
- **No hay simulación en navegador** como dice el prompt maestro. La "simulación" actual es el ícono animado de la app.

### Cosas a corregir (anotadas, no se cambian todavía)

- La lista de colonias del buscador tiene "Villas de Pachuca" y "Tulancingo", que no son de Huauchinango.
- La contraseña de administrador `admin123` está escrita en el código (`HomeScreen.kt`). El login real (R1) está fuera de esta fase, pero no hay que presentarlo como seguridad real.
- `PerfilScreen.kt` tiene datos personales reales de una integrante del equipo como valores de ejemplo. Conviene cambiarlos por datos ficticios antes de subir el código a un repositorio público.
- Hay código duplicado (dos `LoginScreen`, una `RegisterScreen` sin usar) y un archivo vacío (`TruckCard.kt`).

## 3. Cómo cambia el plan

El prompt maestro suponía que el mapa estaba en una página web. En realidad, **la "app de Truck" es esta app Android**, así que el mapa con la ubicación real va **dentro de la app**, en la pantalla de Inicio, en lugar de la tarjeta con el camión animado.

Cadena completa de esta fase:

```
GPS NEO-6M → ESP32 → (WiFi / SIM800L) → API Node.js → App Android (mapa)
```

## 4. Lo que falta para cerrar la Etapa 0

- [x] Revisar el material del profe.
- [x] Revisar el código original de Truck on Rute.
- [x] Decidir dónde va el mapa (en la app Android).
- [ ] Confirmar si existe una API anterior (`app.js` / `server.js`) fuera del zip.
- [ ] Llenar la tabla de `specs/001-ubicacion-tiempo-real/hardware-conexiones.md`.
- [ ] Anotar versiones: `node -v`, Android Studio, Arduino IDE y librerías Arduino instaladas.
- [ ] El equipo aprueba este diagnóstico.

## Resumen para Jira

> Etapa 0: se revisó el material del profe y el código original (app Android en Kotlin/Compose, solo pantallas visuales, sin mapa ni conexión a internet). Se decidió poner el mapa con la ubicación real dentro de la app. Pendiente: tabla de conexiones y confirmar si hay una API anterior.
