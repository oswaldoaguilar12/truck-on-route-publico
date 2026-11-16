# Plan: Estilo visual con profundidad (versión 2.9)

**Spec**: `spec.md` · **Rama**: `version-2.9` (sale de `version-2.8`)

## Revisión con la constitución (v3.0.0)
- I. Alcance: no agrega funciones; solo cambia cómo se ven las pantallas que ya existen. ✔
- III. Partir de lo que existe: el principio pide conservar el diseño (colores, logo, tarjetas): los colores y el logo no cambian y las tarjetas siguen siendo tarjetas, solo con sombra y esquinas más redondas. No se borra ningún archivo. ✔
- IV. Sencillo y en español: una sola pieza nueva (`ui/Estilo.kt`) con funciones cortas y comentadas. ✔
- V. Una etapa con prueba: la prueba es compilar y recorrer las pantallas en el emulador. ✔

## Enfoque
1. `ui/Estilo.kt` (nuevo): `sombraSuave()`, `TarjetaTor`, `TituloSeccion`, `IconoCirculo`, `FondoDecorado`, `BurbujasEncabezado` y `DegradadoVerde`.
2. Aplicarlo en: `HomeScreen.kt` (encabezado, menú lateral, barra inferior, buscador, avisos), `TruckMapSection.kt` (tarjeta, estado en píldora, datos en cuadritos), `ChoferScreen.kt` (encabezado con inicial, colonias unidas por una línea, botones con ícono), `MainActivity.kt` (login), `RegistroScreen.kt` y `PerfilScreen.kt`.

## Riesgo
- No se puede compilar en la nube (no hay SDK de Android). El equipo compila en Android Studio y reporta cualquier error.
