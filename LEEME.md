# Truck on Rute 2.5

Versión de Truck on Rute que **une el GPS real con la app Android**, hecha con desarrollo guiado por especificación (SDD) siguiendo el mismo ciclo de los apuntes del profe (Spec Kit).

Estamos en el **paso 7 de 7 (implementar)**: la API y el mapa en la app ya funcionan con la simulación. Falta conectar el GPS real (hardware).

**Versión 2.7 (spec 003, 2026-11-02):** el login y el registro validan el formato del correo (ya no aceptan `hola.com` sin `@`). Constitución v2.1.0. Ver `specs/003-validar-correo/`. La 2.6 queda igual en la carpeta TruckOnRute2.5.

**Versión 2.6 (spec 002):** historial de la ruta, velocidad y distancia en la app; API con 6 rutas nuevas, guardado en MySQL y panel web del administrador en `http://localhost:3000/panel`. Ver `specs/002-mas-funciones/`.

**Para la demo:** doble clic en `iniciar-demo.bat` y sigue `DEMO.md`.

## Qué hay en cada archivo

| Archivo | Para qué sirve |
|---|---|
| `LEEME.md` | Este archivo. |
| `DEMO.md` | Guion para demostrar la app al profe. |
| `iniciar-demo.bat` | Arranca la API y la simulación con un doble clic. |
| `api/` | La API en Node.js: ubicación, historial, estadísticas, colonias, MySQL y panel web (`npm start`, `npm test`, `npm run simular`). |
| `db/` | Base de datos MySQL: `esquema.sql` y el diagrama. |
| `specs/002-mas-funciones/` | Spec, contrato y tareas de la versión 2.6. |
| `firmware/` | Plantilla de configuración del ESP32 (WiFi y dirección de la API). Aquí irán los programas del GPS. |
| `como-usamos-los-apuntes-sdd.md` | Cómo se aplica cada paso de los apuntes del profe a este proyecto. |
| `diagnostico.md` | Etapa 0: qué tiene la app original y qué le falta. |
| `reglas.md` | Las reglas del proyecto (entrada de `/speckit.constitution`). |
| `spec-input.md` | La descripción del sistema (entrada de `/speckit.specify`). |
| `CLAUDE.md` | Instrucciones cortas para el agente de IA. |
| `.specify/` y `.opencode/` | Configuración de Spec Kit (copiada del proyecto del profe) con la constitución de Truck on Rute. Las versiones anteriores de la constitución están en `.specify/memory/historial/`. |
| `auditoria-2026-11-02.md` | Bitácora de la auditoría SDD: qué se revisó, qué se encontró y qué se cambió. |
| `app-android/` | La app Android, ya con el mapa del camión y el buscador de colonias. Se abre en Android Studio. |
| `specs/001-ubicacion-tiempo-real/spec.md` | Qué debe hacer el sistema: historias de usuario, requisitos y criterios de éxito. |
| `.../plan.md` | Cómo se va a hacer: tecnologías, carpetas y qué archivos de la app se tocan. |
| `.../research.md` | Por qué se eligió cada tecnología y qué alternativas había. |
| `.../data-model.md` | Cómo es una "ubicación" y qué estados muestra la app. |
| `.../contracts/api-ubicacion.md` | El formato exacto del JSON entre el ESP32, la API y la app. |
| `.../quickstart.md` | Pasos para probar todo de principio a fin. |
| `.../tasks.md` | Las 36 tareas en orden, agrupadas por historia y con su etapa. |
| `.../hardware-conexiones.md` | Tabla de pines y precauciones de alimentación. |
| `.../checklists/requirements.md` | Revisión de calidad de la especificación. |

**Auditoría (2026-11-02):** se revisó el proyecto contra SDD y se actualizó la documentación (constitución v2.0.0, spec 002 completa con plan). Ver `auditoria-2026-11-02.md`.

## Siguiente paso

1. Probar la demo (`DEMO.md`).
2. Llenar la tabla de `hardware-conexiones.md` y armar el ESP32 con el GPS.
3. Enviar la ubicación real por WiFi (historia 1 de `tasks.md`).

## Versión 2.8: cuentas de ejemplo (spec 004)

La primera vez que arranca la API crea estas cuentas en `api/datos/tor-datos.json` (ese archivo no se sube a git):

| Rol | Correo | Contraseña | Dónde entra |
|---|---|---|---|
| Administrador | `admin@truckonroute.mx` | `Admin2026` | Panel web: http://localhost:3000/panel |
| Chofer | `chofer@truckonroute.mx` | `Chofer2026` | App Android |
| Ciudadano | `ana@gmail.com` | `Ana2026` | App Android |

Para usar otra cuenta de administrador, pon `ADMIN_CORREO` y `ADMIN_CONTRASENA` en `api/.env` **antes** del primer arranque. Si quieres empezar de cero, borra `api/datos/tor-datos.json` y vuelve a iniciar la API.

Demo completa: en el panel asigna un recorrido al chofer → en la app entra como chofer y toca **Iniciar ruta** → en otra sesión como ciudadano aparece "El camión ya está en ruta".

### Probar "Avisarme cuando esté cerca" en el emulador

1. En la app (como ciudadano) activa **Avisarme cuando esté cerca** y acepta los permisos.
2. En el emulador abre **⋮ (Extended controls) → Location**, escribe una coordenada cerca de la simulación, por ejemplo `20.1765, -98.0494`, y toca **Set location**.
3. Con un recorrido **En ruta** (el chofer tocó Iniciar ruta) y la simulación corriendo, en máximo 20 s con la app abierta (o 5 min en segundo plano) llega "El camión está cerca".

Nota: la simulación envía la ubicación como `CAMION-01`, así que el recorrido debe usar ese camión.
