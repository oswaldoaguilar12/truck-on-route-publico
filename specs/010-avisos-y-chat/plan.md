# Plan: Avisos con historial y chat chofer-administrador (versión 2.14)

**Spec**: `specs/010-avisos-y-chat/spec.md` · **Rama**: `version-2.14`

## Revisión de la constitución (v4.4.0)

- **I. Alcance**: la enmienda 4.4.0 agregó la Fase 6 (avisos con historial, tipos de aviso y chat). Firebase sigue fuera: el chofer ve avisos y mensajes con la app abierta. ✔
- **II. Guardado**: mensajes y avisos en `tor-datos.json`; fotos y audios en `api/datos/archivos/` (ya ignorado por git con `api/datos/`). ✔
- **III. Partir de lo que existe**: se reutilizan las piezas de la v2.10 (EncabezadoVerde, EtiquetaEstado, TarjetaTor) y los colores. ✔
- **IV. Seguro**: chat solo chofer ↔ administrador con token; archivos de 5 MB máximo; el formato se revisa por los primeros bytes del archivo, no por lo que diga el cliente. Sin librerías nuevas: base64 dentro del JSON (sin multer). ✔
- **V. Pruebas**: `node --test` para avisos, chat, archivos y permisos. ✔

## Cómo se hace

### API
- `app.js`: `express.json({ limit: '8mb' })` (5 MB en base64 ocupan ~6.7 MB).
- `datos.js`: `crearMensaje` con `tipo`, `archivo` y `leido`; `guardarArchivo(base64)` revisa el formato por sus primeros bytes (JPG, PNG, M4A, WebM, OGG) y el tamaño; `rutaArchivo(nombre)`.
- `rutas-v3.js`:
  - `POST /admin/avisos`, `GET /admin/avisos`; `GET /avisos` solo ciudadanos/todos y con `?dias=7`; `GET /chofer/avisos`.
  - Chofer: `GET /chofer/chat`, `POST /chofer/chat`, `GET /chofer/chat/no-leidos`.
  - Administrador: `GET /admin/chat` (choferes con no leídos), `GET /admin/chat/:choferId`, `POST /admin/chat/:choferId`.
  - `GET /archivos/:nombre` con token (en el header o `?token=`, porque `<img>` y `<audio>` del panel no mandan headers).
  - El mensaje del chofer al administrador lleva `paraId: 0` (la administración); así lo ve cualquier administrador.

### Panel
- Pestaña "Chat" con contador, lista de choferes y conversación; foto con `<input type=file>` y audio con `MediaRecorder` (funciona en `localhost`).
- Pestaña "Avisos": formulario y lista de enviados (reemplaza "Mensajes al chofer", que ahora vive en el chat).

### App
- `ApiTor`: avisos con `para` y 7 días, chat (listar, mandar con base64), no leídos, avisos del chofer.
- `AvisosScreen.kt`: historial con filtro "Sin leer"; lo leído se guarda en `Sesion` (fecha del último aviso visto).
- `ChatScreen.kt`: burbujas, mensajes rápidos, texto, foto (galería con `PickVisualMedia` o cámara con `TakePicturePreview`, se comprime a JPG) y audio (`MediaRecorder` a M4A, mantener presionado, máximo 60 s; se escucha con `MediaPlayer`).
- Permisos nuevos: `RECORD_AUDIO` (se pide al grabar). La foto de la cámara no necesita permiso con `TakePicturePreview`.
- Chofer: campanita con no leídos y botón "Chat con el administrador"; ciudadano: opción "Avisos" en el menú con contador.
