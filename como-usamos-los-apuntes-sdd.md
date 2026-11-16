# ¿Nos sirven los apuntes de SDD? Sí

Los apuntes del profe (`sdd apuntes.pdf`) enseñan el ciclo de **Spec Kit** con un ejemplo (`task-cli`). Truck on Rute 2.5 sigue exactamente el mismo ciclo; lo que cambia es el proyecto.

## Paso por paso: apuntes → Truck on Rute 2.5

| # | Paso en los apuntes | En task-cli (ejemplo del profe) | En Truck on Rute 2.5 | Estado |
|---|---|---|---|---|
| 1 | `specify init` y abrir en VS Code | carpeta `task-cli` | carpeta `TruckOnRute2.5` (ya trae la configuración de Spec Kit) | ✅ |
| 2 | Escribir `reglas.md` y correr `/speckit.constitution` | 10 reglas: TypeScript, SQLite, Commander… | `reglas.md`: 14 reglas: ESP32, Node + Express, app Android, sin secretos en git… | ✅ Ya está en `.specify/memory/constitution.md` |
| 3 | Escribir `spec-input.md` y correr `/speckit.specify` | Features + "Lo que NO se va a construir" | `spec-input.md` con el mismo formato | ✅ Resultado en `spec.md` |
| 3b | Revisar `spec.md`: historias, requisitos, criterios, `NEEDS CLARIFICATION`, `checklists/requirements.md` | 5 historias | 4 historias, 13 requisitos, 6 criterios; quedaban 2 `NEEDS CLARIFICATION`; el mapa se decidió en clarify y queda 1 (APN del SIM800L) | ✅ con 1 pendiente |
| 4 | `/speckit.clarify` (hasta 5 preguntas) | idioma, filtros… | 6 respondidas en `spec.md` (dónde va el mapa, qué hacer con el "23 min"…) | ✅ |
| 5 | `/speckit.plan` → `plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md` | | Todos creados | ✅ |
| 6 | `/speckit.tasks` → `tasks.md` agrupado por historia, con `[P]` | 33 tareas | 36 tareas, agrupadas por historia y con su etapa | ✅ |
| 7 | `/speckit.implement` | código + tests | API, app y simulación hechas; falta el hardware (GPS real y SIM800L). Después se hizo la spec 002 (versión 2.6) | ✅ en parte · **Aquí estamos** |
| 8 | (no está en los apuntes) Auditoría | | 2026-11-02: revisión SDD, constitución v2.0.0 y spec 002 completada. Ver `auditoria-2026-11-02.md` | ✅ |

## Lo que se aprovecha tal cual de los apuntes

- **El orden**: no escribir código antes de tener `tasks.md` revisado. El profe dice que es "el último punto para corregir el rumbo".
- **"Lo que NO se va a construir"** en `spec-input.md`: evita que el agente agregue login, ETA o base de datos.
- **Revisar los permisos en `/speckit.implement`**: aceptar crear archivos dentro de la carpeta, `npm install` de lo que diga el plan y correr pruebas. **No aceptar** borrar archivos, tocar carpetas fuera de TruckOnRute2.5 ni instalar algo que no esté en el plan.
- **Tests para la lógica**: en `task-cli` con Vitest; aquí con `node --test`, que no hay que instalar.

## Lo que se adapta

- Truck on Rute tiene **hardware**, y eso no se puede probar con tests automáticos. Por eso cada tarea dice su **etapa** del prompt maestro, y cada etapa tiene una prueba física (monitor serial, multímetro, Google Maps).
- Truck on Rute **no empieza de cero**: ya existe la app Android. El plan dice exactamente qué archivos de la app se tocan y cuáles no.
- `task-cli` es un solo programa; Truck on Rute son **tres** (firmware, API y app), cada uno en su carpeta.

## Lo que no aplica

- `constitution.md` y `constitution2.md` de `C:\Melendez` son plantillas de ejemplo (React, MySQL, JWT…). No se usan: Truck on Rute no tiene web ni base de datos en esta fase.
- `Comandos.docx` solo tiene el título "Comandos".
