# Spec 002: Más funciones para la API y la app (versión 2.6)

**Fecha**: 2026-10-23 · **Equipo**: G2VSIS07 · **Base**: spec 001 (ubicación en tiempo real)
**Status**: Implementada (2026-10-23) · Completada en formato Spec Kit después de la auditoría (2026-11-02)
**Constitución**: v2.0.0 (la v1.0.0 dejaba esta fase fuera de alcance; ver "Historial de enmiendas")

> **Nota de trazabilidad.** Esta spec se escribió el 2026-10-23, junto con el código (tarea T118), y no antes como pide SDD. El 2026-11-02 una auditoría lo detectó. Se agregaron las secciones *Clarifications*, *Edge Cases*, *Success Criteria*, *Assumptions*, `plan.md` y `checklists/requirements.md`, marcadas como "registrado después". Las historias y requisitos originales no se cambiaron.

## Por qué

La versión 2.5 ya mostraba el camión en el mapa, pero hacía casi lo mismo que la app anterior de Truck on Route.
El profesor pidió más. Esta spec agrega funciones que usan lo que ya existe (API, base de datos, mapa) para dar información útil al ciudadano y al administrador.

## Clarifications

### Session 2026-10-23 (registrado después, el 2026-11-02)

Decisiones que el equipo tomó al construir la versión 2.6. No pasaron por `/speckit.clarify` en su momento; aquí quedan escritas con su fecha real.

- Q: ¿Por qué ampliar el alcance si la constitución v1.0.0 lo prohibía? → A: Lo pidió el profesor el 2026-10-23. Se formalizó con la enmienda v2.0.0 (2026-11-02).
- Q: ¿Base de datos obligatoria o opcional? → A: Opcional. MySQL 8 solo si `.env` tiene `DB_HOST`; si no está o falla, la API sigue en memoria. Así la demo funciona sin instalar MySQL.
- Q: ¿Cuántos puntos guarda el historial? → A: Hasta 500 por camión en memoria; la app y el panel piden los últimos 50 por defecto.
- Q: ¿Cómo se calcula la velocidad? → A: Con la distancia Haversine entre los 2 últimos puntos dividida entre el tiempo entre sus fechas. Con menos de 2 puntos se muestra `-- km/h`.
- Q: ¿El panel del administrador necesita login? → A: No en esta fase: solo lee datos y corre en la computadora del equipo (`localhost`). El login real sigue fuera de alcance.
- Q: ¿La velocidad sirve para dar el tiempo de llegada (ETA)? → A: No. Se muestra la velocidad y la distancia; el ETA (R4 del acta) sigue fuera de alcance.

### Pendientes

Ninguno.

## Historias de usuario

### H7: Ver por dónde ya pasó el camión (ciudadano) · Prioridad P1
Como ciudadano quiero ver en el mapa la ruta que ya recorrió el camión, para saber si ya pasó por mi calle.

- **Dado** que el camión envió varias ubicaciones, **cuando** abro Inicio, **entonces** veo una línea verde con los últimos 50 puntos.
- **Dado** que no hay ubicación, **entonces** no se dibuja ninguna línea.

### H8: Ver velocidad y distancia (ciudadano) · P2
Como ciudadano quiero ver la velocidad y los km recorridos, para saber qué tan rápido avanza el camión.

- Debajo del mapa aparece `Velocidad: 12.5 km/h · Recorrido: 1.32 km · 20 puntos`.
- Con menos de 2 puntos la velocidad se muestra como `-- km/h`.

> Cambio de redacción (2026-11-02): decía "para calcular cuánto tarda en llegar", lo que se confundía con el ETA, que está fuera de alcance.

### H9: Mover el mapa sin que regrese solo (ciudadano) · P3
- Si arrastro el mapa, deja de seguir al camión.
- El botón **Centrar** vuelve a ponerlo en el centro y lo sigue otra vez.

### H10: Panel web del administrador · P1
Como administrador quiero una página en la computadora con el mapa, el estado y los últimos puntos, para vigilar el servicio sin celular.

- `http://localhost:3000/panel` muestra el estado (Simulación, Ubicación real, Sin señal reciente o API apagada), 5 tarjetas (camión, última hora, velocidad, distancia, puntos), el mapa con la ruta, la tabla de los últimos 8 puntos y un buscador de colonias.
- Se actualiza solo cada 5 s.

### H11: Guardar las ubicaciones en MySQL (administrador) · P1
- Si el archivo `.env` tiene `DB_HOST`, cada ubicación se guarda en la tabla `ubicaciones` de `truck_on_route` (`db/esquema.sql`).
- Si MySQL no está o falla, la API sigue funcionando en memoria y lo avisa en la consola.

### H12: Consultar colonias desde la API · P3
- `GET /colonias?buscar=zaca` devuelve las colonias que coinciden, sin importar acentos ni mayúsculas.

## Edge Cases (registrado después)

- `limite` fuera de rango, negativo o texto en `/ubicacion/historial`: se ajusta a un valor entre 1 y 500 (por defecto 50); no da error.
- Historial vacío: `GET /ubicacion/historial` responde `[]` y la app no dibuja línea.
- Solo 1 punto: `velocidadKmh` es `null` y la app muestra `-- km/h`.
- MySQL apagado o con contraseña incorrecta: la API avisa en la consola y sigue en memoria.
- Ruta que no existe: 404 `{ "error": "Ruta no encontrada" }`.
- Sin internet en la computadora: el panel sigue cargando porque Leaflet se sirve desde `node_modules` (solo faltan las imágenes del mapa).

## Requisitos

Los requisitos de esta spec se llamaban R12–R18. Como el acta del proyecto también usa R (R3, R4, R7, R9), desde el 2026-11-02 se usan los nombres FR-101 a FR-107. La columna "Antes" conserva el nombre original para que las tareas sigan cuadrando.

Escritos en formato **EARS** el 2026-11-02 (mismos patrones que la spec 001).

| Id | Antes | Patrón | Requisito (EARS) |
|---|---|---|---|
| FR-101 | R12 | Por evento | Cuando se pida `GET /ubicacion/historial?limite=N`, la API deberá devolver los últimos N puntos (1 a 500, por defecto 50), del más viejo al más nuevo. |
| FR-102 | R13 | Por evento + No deseado | Cuando se pida `GET /estadisticas`, la API deberá devolver la distancia recorrida (Haversine) y la velocidad entre los 2 últimos puntos. Si hay menos de 2 puntos, entonces la API deberá devolver la velocidad como `null`. |
| FR-103 | R14 | Por evento | Cuando se pida `GET /camiones`, la API deberá listar cada camión con su última ubicación y si está en línea (menos de 60 s). |
| FR-104 | R15 | Por evento | Cuando se pida `GET /colonias?buscar=texto`, la API deberá devolver las colonias de las 83 que coincidan, sin importar acentos ni mayúsculas. |
| FR-105 | R16 | Opcional + No deseado | Donde `.env` tenga `DB_HOST`, la API deberá guardar cada ubicación en la tabla `ubicaciones` de MySQL 8. Si MySQL no responde, entonces la API deberá avisar en la consola y seguir funcionando en memoria. |
| FR-106 | R17 | Por estado | Mientras la API esté encendida, la API deberá servir el panel en `/panel`, con Leaflet desde `node_modules`, actualizándose cada 5 s. |
| FR-107 | R18 | Por estado + Por evento | Mientras Inicio esté visible, la app deberá dibujar la ruta y mostrar velocidad y distancia. Cuando el usuario arrastre el mapa, la app deberá dejar de seguir al camión hasta que toque **Centrar**. |

## Success Criteria (registrado después)

- **SC-101**: `npm test` pasa todas las pruebas de rutas y validación (hoy 19 de 19).
- **SC-102**: Con la simulación corriendo, la línea verde de la app crece con cada punto nuevo en máximo 20 s.
- **SC-103**: El panel muestra el mismo estado que la app y se actualiza en máximo 5 s.
- **SC-104**: Con MySQL configurado, cada `POST /ubicacion` válido aparece en la tabla `ubicaciones` (comprobado: 13 de 13 puntos, T108).
- **SC-105**: Con MySQL apagado, la API arranca y responde igual que sin base de datos.
- **SC-106**: Buscar `zaca` devuelve las 3 colonias de Zacamila, con o sin acento.

## Assumptions (registrado después)

- El panel corre en la computadora del equipo (`localhost`); no se publica en internet en esta fase.
- `db/esquema.sql` trae tablas para fases futuras (`usuarios`, `rutas`, `recorridos`, `notificaciones`), pero la API solo usa `ubicaciones` y `colonias`.
- La velocidad entre 2 puntos de la simulación es aproximada; con el GPS real puede variar por el error de posición.

## Fuera de alcance
Inicio de sesión real del administrador, varios camiones a la vez en la app, notificaciones push y tiempo estimado de llegada.
