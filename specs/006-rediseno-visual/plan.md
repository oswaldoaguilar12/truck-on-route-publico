# Plan: Rediseño visual moderno (versión 2.10)

**Spec**: `spec.md` · **Rama**: `version-2.10` (sale de `version-2.9`) · **Maqueta**: aprobada por el equipo el 2026-11-24

## Revisión con la constitución (v4.0.0)
- I. Alcance: no agrega funciones; solo cambia cómo se ven las pantallas que ya existen. ✔
- II. Tres partes y un contrato: solo se toca `app-android/`. La API, el panel web y el firmware no cambian. ✔
- III. Partir de lo que existe (v4.0.0): los colores y el logo no cambian (FR-607). Se cambia el acomodo, la tipografía (Poppins dentro de `res/font`) y se agregan animaciones cortas, como permite la enmienda. La maqueta se aprobó antes de programar. No se borra ningún archivo. ✔
- IV. Sencillo y en español: sin librerías nuevas (Poppins va como archivo `.ttf`, con su licencia OFL). Las animaciones usan lo que ya trae Compose. Funciones cortas y comentadas. ✔
- V. Una etapa con prueba: la prueba es compilar en Android Studio y recorrer las pantallas con las tres cuentas en el emulador. ✔

## Enfoque
1. **Tipografía**: copiar Poppins (Regular, Medium, SemiBold y Bold) a `res/font/` y crear `ui/Tipografia.kt` con la `Typography` de la app. `TruckOnRouteTheme` (en `MainActivity.kt`) la usa, así todos los textos cambian sin tocarlos uno por uno.
2. **Piezas nuevas en `ui/Estilo.kt`**: `Modifier.alPresionar()` (el botón se achica un poco al tocarlo), `EntradaSuave` (aparece subiendo y con desvanecido en 250 ms), `EtiquetaEstado` (chip de color), `EncabezadoVerde` (encabezado verde con círculos, usado en Chofer y Perfil) y `EstadoVacio` (ícono grande y texto amable).
3. **Inicio (`HomeScreen.kt` y `TruckMapSection.kt`)**:
   - `TruckMapSection.kt` se divide en dos: `rememberDatosCamion()` (pide la ubicación cada 10 s, igual que antes) y `MapaCamion()` (el mapa a pantalla completa).
   - El marcador se mueve con una animación de 1 s entre el punto anterior y el nuevo (FR-604).
   - `HomeScreen` usa `BottomSheetScaffold`: arriba el mapa con el buscador flotante, el botón de menú y el chip de estado; abajo la hoja que se desliza con la distancia, los tres datos, los avisos, el interruptor de cercanía y la nota del administrador (FR-601, FR-603).
4. **Login y registro**: fondo verde con círculos, título "Sabe cuándo pasa el camión" y una hoja blanca que sube con animación. Botón tipo píldora (FR-605). Misma lógica de validación y de la API.
5. **Chofer**: encabezado verde con saludo y "Hoy te toca", tarjeta encimada con colonias numeradas, nota amarilla, mensajes y botón grande "Iniciar ruta" / "Terminar ruta".
6. **Perfil**: encabezado verde, tarjeta encimada con avatar con la inicial (o la foto elegida), nombre, correo y chip del rol; filas de Colonia, Teléfono y Rol; botón "Editar perfil" con borde.

## Riesgos
- No se puede compilar en la nube (no hay SDK de Android). El equipo compila en Android Studio y reporta cualquier error; se corrige en la misma rama.
- `BottomSheetScaffold` es experimental en Material 3 1.3 (se marca con `@OptIn`).
- Si no queda probado antes del 2026-11-30, se entrega la v2.9.
