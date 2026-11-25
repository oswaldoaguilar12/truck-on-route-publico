# Plan: Memorama de residuos "¿Dónde va?" (versión 2.11)

**Spec**: `spec.md` · **Rama**: `version-2.11` (sale de `version-2.10`) · **Maqueta**: aprobada por el equipo el 2026-11-25 (`evidencia/v2.11/maqueta_memorama_v2.11.png`)

## Revisión con la constitución (v4.1.0)
- I. Alcance: el memorama es la Fase 4 que agregó la enmienda 4.1.0. "Composta en casa" solo aparece como "Próximamente", sin contenido. ✔
- II. Tres partes separadas y un contrato: solo se toca `app-android/`. La API, el panel web y el firmware no cambian; el juego no hace peticiones. ✔
- III. Partir de lo que existe: se reusan las piezas de la v2.10 (`EncabezadoVerde`, `BotonPildora`, `BotonBorde`, `EtiquetaEstado`, `entradaSuave`, `rebote`). Los colores de las categorías se usan solo dentro del juego. No se borra ningún archivo. ✔
- IV. Sencillo y en español: sin librerías nuevas ni imágenes nuevas (los residuos son emojis). Funciones cortas y comentadas. ✔
- V. Una etapa con prueba: compilar en Android Studio y jugar en el emulador con ciudadano y administrador. ✔

## Enfoque
1. **Datos** (`juego/Residuos.kt`): `enum class Categoria` (nombre, color, emoji y ejemplos), `data class Residuo` (nombre, emoji, categoría y consejo), la lista de 20 residuos y la función `estrellas(intentos)`.
2. **Pantalla** (`ui/CuidaAmbienteScreen.kt`), con 4 etapas en una sola variable:
   - `SECCION`: encabezado verde "Cuida tu ambiente" y tarjetas de actividades (juego y "Composta en casa: Próximamente").
   - `INICIO`: explicación y las 4 categorías con su color; botón "Jugar".
   - `TABLERO`: intentos, tiempo y pares arriba; cuadrícula de 3 × 4 con cartas que se voltean (`rotationY`, 280 ms). Al encontrar un par aparece la tarjeta del consejo; si no son iguales, regresan después de 1 s.
   - `RESUMEN`: estrellas, intentos, tiempo y los residuos agrupados por categoría; "Jugar otra vez" y "Salir".
   - El botón atrás del celular regresa a la etapa anterior (`BackHandler`).
3. **Menú** (`HomeScreen.kt`): nueva opción "Cuida tu ambiente" con ícono de hoja. Mientras está abierta, la sección ocupa la pantalla en lugar de Inicio/Perfil.

## Riesgos
- No se puede compilar en la nube; el equipo compila en Android Studio y se corrige en la misma rama.
- Algunos emojis podrían verse distinto según la versión de Android; se eligieron emojis comunes (Android 8 o más nuevo).
