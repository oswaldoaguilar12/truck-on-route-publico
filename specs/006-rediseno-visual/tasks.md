# Tasks: Rediseño visual moderno (versión 2.10)

**Input**: `specs/006-rediseno-visual/`

- [X] T601 Spec 006 y maqueta aprobada por el equipo (2026-11-24)
- [X] T602 Enmienda de la constitución a v4.0.0 (principio III) y plan con su revisión (2026-11-25)
- [X] T603 Poppins en `res/font`, `ui/Tipografia.kt` y el tema de la app (FR-602) — programada 2026-11-25, falta compilar (T610)
- [X] T604 Piezas nuevas en `ui/Estilo.kt`: al presionar, entrada suave, etiqueta de estado, encabezado verde y estado vacío (FR-603, FR-605, FR-606) — programada 2026-11-25, falta compilar (T610)
- [X] T605 `TruckMapSection.kt`: datos separados del mapa y marcador con movimiento suave (FR-604) — programada 2026-11-25, falta compilar (T610)
- [X] T606 `HomeScreen.kt`: mapa a pantalla completa con hoja inferior deslizable (FR-601, FR-603) — programada 2026-11-25, falta compilar (T610)
- [X] T607 [P] Login (`MainActivity.kt`) y `RegistroScreen.kt` con fondo verde y hoja blanca (FR-605) — programada 2026-11-25, falta compilar (T610)
- [X] T608 [P] `ChoferScreen.kt`: encabezado, tarjeta encimada y botón grande (FR-605, FR-606) — programada 2026-11-25, falta compilar (T610)
- [X] T609 [P] `PerfilScreen.kt`: encabezado, avatar con inicial y chip del rol — programada 2026-11-25, falta compilar (T610)
- [X] T612 `ui/Bienvenida.kt`: logo al abrir y camioncito animado al iniciar sesión (FR-609) — programada 2026-11-25, falta compilar (T610). Versión de caricatura (2026-11-25, a petición del equipo): `evidencia/v2.10/camion_caricatura_v2.10.gif` (la primera versión, `camion_animado_v2.10.gif`, quedó como antecedente)
- [X] T613 Ícono propio de la app: `ic_launcher_foreground.xml`, `ic_launcher_background.xml` y las imágenes `mipmap-*` (FR-610) — 2026-11-25. Logo final: pin de ubicación con el camión (`evidencia/v2.10/icono_app_v2.10.png` y `logo_profesional_v2.10.png`)
- [X] T614 [P] `TruckMapSection.kt`: marcador con el camión del logo, pulso y punto azul de tu ubicación (FR-611, FR-612) — programada 2026-11-25, falta compilar (T610)
- [X] T615 [P] `Estilo.kt`: íconos de la barra de estado en blanco, esqueletos y rebote; aplicado en pantallas verdes, Inicio y Chofer (FR-613, FR-614, FR-616) — programada 2026-11-25, falta compilar (T610)
- [X] T616 [P] `HomeScreen.kt`: cambio suave entre pestañas, aviso nuevo con vibración y menú con logo y usuario (FR-615, FR-616, FR-618) — programada 2026-11-25, falta compilar (T610)
- [X] T617 [P] `ChoferScreen.kt`: barra de avance por colonia (FR-617) — programada 2026-11-25, falta compilar (T610)
- [X] T610 Compilar en Android Studio sin errores (SC-601) — equipo, 2026-11-24: BUILD SUCCESSFUL, solo advertencias (evidencia/v2.10/T610_build_successful.png)
- [X] T611 Recorrer las pantallas con las tres cuentas en el emulador y confirmar que todo funciona igual y se ve como la maqueta (SC-602, SC-603, FR-608) — equipo, 2026-11-24: admin (camión animado, Inicio, mapa, Perfil, menú), chofer (recorrido, iniciar ruta, barra de avance) y ciudadano (notificación, aviso destacado, cercanía a 129 m con punto azul) funcionan (evidencia/v2.10/T611_*.png). Ajustes pequeños hechos durante la prueba (estado repetido en la hoja, menú morado, logo del login y pantalla del sistema en verde) quedan por ver en el siguiente Pull
