// Genera ProyectoFinal_TruckOnRoute_G2VSIS07.docx (documento del proyecto final, fases del PMI).
// Uso: node generar-docx.js
const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, Table, TableRow, TableCell, WidthType,
  ShadingType, AlignmentType, ImageRun, PageBreak, LevelFormat, PageOrientation, BorderStyle,
  TableOfContents, Footer, PageNumber, ExternalHyperlink,
} = require('docx');

const VERDE = '0A7D2C', VERDE_CLARO = 'E3F4E8', GRIS = 'F2F2F2';
const REPO = 'https://github.com/oswaldoaguilar12/truck-on-route-publico'; // repo público (copia limpia de version-2.9)
const ARBOL = REPO + '/tree/main/';
const ANCHO = 9360;         // carta vertical con márgenes de 1"
const ANCHO_H = 12960;      // carta horizontal con márgenes de 1"

// ---------- ayudas
const t = (texto, op = {}) => new TextRun({ text: texto, ...op });
const p = (contenido, op = {}) => new Paragraph({ children: Array.isArray(contenido) ? contenido : [t(contenido)], spacing: { after: 120 }, ...op });
const h1 = (x) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [t(x)] });
const h2 = (x) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [t(x)] });
const h3 = (x) => new Paragraph({ heading: HeadingLevel.HEADING_3, children: [t(x)] });
const viñeta = (x, nivel = 0) => new Paragraph({ numbering: { reference: 'vinetas', level: nivel }, spacing: { after: 60 }, children: Array.isArray(x) ? x : [t(x)] });
const negrita = (a, b) => [t(a, { bold: true }), t(b)];
const enlace = (texto, url) => new ExternalHyperlink({ link: url, children: [t(texto, { style: 'Hyperlink' })] });
const salto = () => new Paragraph({ children: [new PageBreak()] });

function celda(contenido, ancho, op = {}) {
  const parrafos = (Array.isArray(contenido) ? contenido : [contenido]).map((c) =>
    c instanceof Paragraph ? c : new Paragraph({ children: [typeof c === 'string' ? t(c, { bold: op.bold, color: op.color, size: op.size || 18 }) : c], alignment: op.align })
  );
  return new TableCell({
    width: { size: ancho, type: WidthType.DXA },
    shading: op.fondo ? { type: ShadingType.CLEAR, color: 'auto', fill: op.fondo } : undefined,
    margins: op.compacta ? { top: 10, bottom: 10, left: 40, right: 40 } : { top: 60, bottom: 60, left: 90, right: 90 },
    children: parrafos,
  });
}

function tabla(anchos, encabezado, filas, opciones = {}) {
  const total = anchos.reduce((a, b) => a + b, 0);
  const cab = new TableRow({ tableHeader: true, children: encabezado.map((e, i) => celda(e, anchos[i], { bold: true, color: 'FFFFFF', fondo: VERDE, size: opciones.tamCab || 18 })) });
  const cuerpo = filas.map((f, n) => new TableRow({
    children: f.map((c, i) => celda(c, anchos[i], { fondo: opciones.cebra && n % 2 ? GRIS : undefined, size: opciones.tam, align: opciones.centrar && opciones.centrar.includes(i) ? AlignmentType.CENTER : undefined, bold: opciones.negritaCol === i })),
  }));
  return new Table({ width: { size: total, type: WidthType.DXA }, columnWidths: anchos, rows: [cab, ...cuerpo] });
}

function imagen(archivo, anchoPx) {
  const datos = fs.readFileSync(archivo);
  const w = datos.readUInt32BE(16), h = datos.readUInt32BE(20);
  return new Paragraph({ alignment: AlignmentType.CENTER, children: [new ImageRun({ type: 'png', data: datos, transformation: { width: anchoPx, height: Math.round(anchoPx * h / w) } })] });
}
const pie = (x) => p([t(x, { italics: true, size: 18, color: '555555' })], { alignment: AlignmentType.CENTER });

// ---------- contenido
const DIAG = path.join(__dirname, '..', 'diagramas');

const portada = [
  new Paragraph({ spacing: { before: 1800 }, alignment: AlignmentType.CENTER, children: [t('Instituto Tecnológico Superior de Huauchinango', { size: 26, bold: true })] }),
  p([t('Ingeniería en Sistemas Computacionales · Gestión de Proyectos de Software', { size: 22 })], { alignment: AlignmentType.CENTER }),
  new Paragraph({ spacing: { before: 900, after: 200 }, alignment: AlignmentType.CENTER, children: [t('PROYECTO FINAL', { size: 30, bold: true, color: VERDE })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 }, children: [t('Truck on Route', { size: 56, bold: true, color: VERDE })] }),
  p([t('Ubicación en tiempo real del camión recolector de basura en Huauchinango, Puebla', { size: 24, italics: true })], { alignment: AlignmentType.CENTER }),
  p([t('Reporte del ciclo de vida del proyecto según el PMI: inicio, planeación, ejecución, control y cierre', { size: 20 })], { alignment: AlignmentType.CENTER }),
  new Paragraph({ spacing: { before: 900 }, alignment: AlignmentType.CENTER, children: [t('Docente: Ing. Manuel Hernández Melendez', { size: 22 })] }),
  p([t('Equipo G2VSIS07', { size: 22, bold: true })], { alignment: AlignmentType.CENTER }),
  ...['Sandra Sarahi Cruz Andres', 'Alonso Reyes Cruz', 'Dariana Luna Rivera', 'Oswaldo Aguilar Barrera'].map((n) => p([t(n, { size: 22 })], { alignment: AlignmentType.CENTER, spacing: { after: 40 } })),
  new Paragraph({ spacing: { before: 700 }, alignment: AlignmentType.CENTER, children: [t('Versión del sistema: 2.9 · Huauchinango, Puebla · 16 de noviembre de 2026', { size: 20, color: '555555' })] }),
  salto(),
  h1('Contenido'),
  new TableOfContents('Contenido', { hyperlink: true, headingStyleRange: '1-2' }),
  p([t('Si el índice aparece vacío, en Word da clic derecho sobre él y elige "Actualizar campo".', { italics: true, size: 18, color: '777777' })]),
  salto(),
];

// ---- 1. Inicio
const inicio = [
  h1('1. Inicio del proyecto'),
  p('Esta fase define qué es Truck on Route, para quién se hace y con qué tecnología. Se basa en el Acta de Constitución del 14 de septiembre de 2026 y en la constitución del proyecto (Spec Kit) versión 3.0.0.'),
  h2('1.1 Acta de constitución (Project Charter)'),
  p(negrita('Propósito. ', 'Los habitantes de Huauchinango no saben a qué hora pasa el camión recolector por su colonia. Truck on Route es una app móvil que muestra la ruta y la ubicación en tiempo real del camión, y avisa cuando ya salió o cuando está cerca.')),
  p(negrita('Objetivos medibles. ', 'Consultar el 100% de las rutas registradas; ver la ubicación del camión durante los recorridos activos (actualización cada 10 s); notificar cuando el camión está a menos de 500 m; llegar a la información en máximo 3 pasos desde la pantalla principal.')),
  tabla([2200, 7160], ['Parte', 'Funcionalidades'], [
    [t('MVP (entrega 1)', { bold: true, size: 18 }), 'El ESP32 con GPS NEO-6M (o el simulador) manda la ubicación a la API; la app Android la muestra en un mapa de Huauchinango con el estado "Ubicación real", "Simulación" o "Sin señal reciente".'],
    [t('Funcionalidades extendidas', { bold: true, size: 18 }), 'Historial del recorrido, velocidad y distancia; panel web del administrador; guardado en MySQL; validación de correo; cuentas con rol (ciudadano, chofer y administrador); altas de camiones, choferes y rutas; recorridos con indicaciones; aviso "el camión ya está en ruta"; aviso de cercanía; estilo visual con profundidad.'],
  ]),
  p(''),
  p([t('Evidencia: ', { bold: true }), enlace('ACTA DE CONSTITUCIÓN DEL PROYECTO.pdf', REPO + '/blob/main/ACTA%20DE%20CONSTITUCI%C3%93N%20DEL%20PROYECTO.pdf'), t(' en el repositorio.')]),

  h2('1.2 Registro de interesados (stakeholders)'),
  tabla([2100, 1700, 2560, 1000, 1000, 1000], ['Interesado', 'Rol', 'Interés en el proyecto', 'Interés', 'Influencia', 'Estrategia'], [
    ['Gobierno Municipal / Dirección de Servicios Públicos', 'Patrocinador', 'Ordenar la recolección y dar mejor servicio; facilita rutas, horarios y la instalación del GPS.', 'Alto', 'Alta', 'Gestionar de cerca'],
    ['Administrador del sistema (supervisor municipal)', 'Usuario: rol administrador', 'Dar de alta camiones, choferes y rutas, asignar recorridos y mandar indicaciones desde el panel.', 'Alto', 'Alta', 'Gestionar de cerca'],
    ['Choferes del camión recolector', 'Usuario: rol chofer', 'Ver su recorrido del día, las colonias en orden y las indicaciones; iniciar y terminar la ruta.', 'Medio', 'Media', 'Mantener informado'],
    ['Ciudadanía de Huauchinango', 'Usuario: rol ciudadano', 'Saber dónde va el camión y recibir avisos para sacar la basura a tiempo.', 'Alto', 'Media', 'Mantener satisfecho'],
    ['Equipo de desarrollo G2VSIS07', 'Equipo del proyecto', 'Construir, probar y entregar el sistema; aprobar cada etapa.', 'Alto', 'Alta', 'Gestionar de cerca'],
    ['Ing. Manuel Hernández Melendez', 'Docente / evaluador', 'Revisar que el proyecto siga el PMI, SDD y Scrum y evaluar las entregas.', 'Medio', 'Alta', 'Mantener satisfecho'],
  ], { cebra: true, centrar: [3, 4] }),

  h2('1.3 Concepción arquitectónica inicial: stack tecnológico'),
  p('El pizarrón de clase propone opciones para cada capa (React JS, Express, MongoDB/MySQL/PostgreSQL y Claude/Codex/Cursor/OpenCode). La tabla muestra lo que eligió Truck on Route y el porqué o para qué de cada tecnología.'),
  tabla([1500, 2100, 1600, 4160], ['Capa', 'Tecnología elegida', 'Otras opciones', '¿Por qué o para qué?'], [
    ['Frontend móvil', 'Kotlin + Jetpack Compose (Android Studio)', 'React JS, React Native', 'La app original del equipo ya estaba en Kotlin; la constitución pide partir de lo que existe. Da acceso nativo al GPS del celular, a las notificaciones y al servicio en segundo plano que revisa la cercanía cada 5 minutos.'],
    ['Mapa', 'osmdroid + Esri World Street Map', 'Google Maps, OpenStreetMap, CARTO', 'No necesita clave ni pago. OpenStreetMap bloqueó las peticiones y CARTO pedía clave; Esri funcionó.'],
    ['Frontend web', 'Panel en HTML + JavaScript + Leaflet', 'React JS', 'Es una sola página que sirve la misma API: no hay que compilar ni desplegar otro proyecto. Sirve para monitorear el camión y hacer las altas.'],
    ['Backend', 'Node.js + Express 5', 'Laravel, Django, Firebase', 'JavaScript sencillo y ligero; el ESP32 manda JSON por HTTP y Express lo recibe en pocas líneas. Node trae su propio sistema de pruebas (node --test): 28 pruebas.'],
    ['Base de datos', 'MySQL 8 (opcional) + archivo JSON', 'MongoDB, PostgreSQL', 'Los datos son relacionales (rutas con colonias en orden, recorridos con camión y chofer). MySQL viene incluido en el servidor elegido (Hostinger) y el equipo lo conoce. Si MySQL falla, la API sigue en memoria y en JSON.'],
    ['Hardware', 'ESP32 + GPS NEO-6M + SIM800L (C++, Arduino IDE)', 'Celular del chofer como GPS', 'Dispositivo barato fijo en el camión que no depende del celular del chofer. WiFi para pruebas y SIM800L para la red celular.'],
    ['Seguridad', 'scrypt (crypto de Node) + tokens', 'JWT, Firebase Auth', 'Viene en Node sin librerías extra. Contraseñas con hash y sal; token de 7 días; cada ruta revisa el rol (401/403).'],
    ['IA generativa', 'Claude (plan Pro)', 'Codex, Cursor, OpenCode, Copilot', 'Trabaja en español, sigue la constitución y el CLAUDE.md, y lee y escribe en el repositorio. Se usó para SDD, código, pruebas, auditoría y documentación; todo lo revisó el equipo.'],
    ['Metodología', 'SDD con GitHub Spec Kit', 'Programar sin especificación', 'Constitución → spec → clarify → plan → tasks → código. Evitó que la IA inventara funciones como el ETA.'],
    ['Gestión', 'Jira (Scrum), Notion, GitHub, Google Drive', 'Trello, Excel', 'Jira para épicas, historias y sprints; Notion para el reporte y la bitácora; GitHub para versiones (2.5 a 2.9); Drive para el archivo compartido con permisos.'],
  ], { cebra: true, tam: 17 }),
  salto(),
];

// ---- 2. Planeación
const planeacion = [
  h1('2. Planeación'),
  h2('2.1 Especificación del sistema (SDD con Spec Kit)'),
  p('Cada versión se especificó antes de programarla. Todos los archivos están en el repositorio público truck-on-route-publico:'),
  tabla([2900, 3700, 2760], ['Artefacto', 'Archivo', 'Qué contiene'], [
    ['Constitución v3.0.0', '.specify/memory/constitution.md', 'Reglas, alcance por fases y lo que queda fuera.'],
    ['Instrucciones para la IA', 'CLAUDE.md', 'Español, leer la constitución y seguir tasks en orden.'],
    ['Spec 001 (MVP)', 'specs/001-ubicacion-tiempo-real/', 'spec, plan, research, data-model, contracts, quickstart, tasks.'],
    ['Spec 002', 'specs/002-mas-funciones/', 'Historial, velocidad, panel y MySQL.'],
    ['Spec 003', 'specs/003-validar-correo/', 'Validación del correo.'],
    ['Spec 004', 'specs/004-roles-recorridos-avisos/', 'Roles, recorridos y avisos; contrato api-v3.md.'],
    ['Spec 005', 'specs/005-estilo-visual/', 'Estilo visual con profundidad.'],
    ['Esquema de la base de datos', 'db/esquema.sql', '8 tablas MySQL con llaves y reglas.'],
    ['Arquitectura y diagramas', 'docs/diagramas/', 'arquitectura.html y base-de-datos.html.'],
  ], { cebra: true }),
  p(''),
  p([t('Repositorio: ', { bold: true }), enlace(REPO, ARBOL)]),

  h2('2.2 Diagramas'),
  h3('a) Estructura del proyecto'),
  imagen(path.join(DIAG, 'arquitectura.png'), 620),
  pie('Figura 1. Arquitectura de Truck on Route 2.9 (docs/diagramas/arquitectura.html).'),
  h3('b) Base de datos'),
  imagen(path.join(DIAG, 'base-de-datos.png'), 620),
  pie('Figura 2. Modelo entidad-relación de db/esquema.sql (docs/diagramas/base-de-datos.html).'),
  p([t('Nota: ', { bold: true }), t('en la versión 2.8/2.9 MySQL guarda camiones y ubicaciones; cuentas, rutas, recorridos, mensajes y avisos se guardan en api/datos/tor-datos.json. Pasarlos a estas tablas es trabajo futuro.')]),

  h2('2.3 Alcance del proyecto'),
  tabla([4680, 4680], ['Qué entra', 'Qué no entra (por ahora)'], [
    [
      ['Ubicación del camión en tiempo real (GPS o simulación) en un mapa.', 'Historial, velocidad y distancia del recorrido.', 'Panel web del administrador.', 'Cuentas con rol: ciudadano, chofer y administrador.', 'Altas de camiones, choferes y rutas; recorridos con indicaciones y mensajes.', 'Aviso "el camión ya está en ruta" y aviso de cercanía (500 m, cada 5 min).', 'Guardado en MySQL (opcional).'].map((x) => viñeta(x)),
      ['Tiempo estimado de llegada (ETA).', 'Colores de la ruta según por dónde ya pasó el camión (otra spec).', 'Notificaciones push con Firebase.', 'Varios camiones a la vez en el mapa del ciudadano.', 'Carrito con motores, panel solar y sensores extra (MPU6050, AHT20-BMP280).', 'Servicios en la nube distintos al VPS elegido.'].map((x) => viñeta(x)),
    ],
  ]),
  p(''),

  h2('2.4 Entregas (Scrum) y funcionalidad del sistema'),
  p('Cada entrega es un incremento que funciona por sí solo y sale de una spec. Equivale a la lista "1er MVP, 2º, 3º…" del pizarrón.'),
  tabla([700, 1200, 1700, 3860, 1900], ['No.', 'Versión', 'Entrega', 'Funcionalidad del sistema', 'Estado'], [
    ['1', 'v2.5 · spec 001', 'MVP: ubicación en tiempo real', 'ESP32/simulador → API → mapa en la app; estados "Ubicación real", "Simulación" y "Sin señal reciente".', 'Hecha en software; GPS real pendiente (T013, T015)'],
    ['2', 'v2.6 · spec 002', 'Historial y panel', 'Línea del recorrido, velocidad y distancia, panel web de monitoreo, MySQL y colonias desde la API.', 'Hecha; auditada el 02/11'],
    ['3', 'v2.7 · spec 003', 'Validación de datos', 'Login y registro rechazan correos mal escritos.', 'Hecha; falta prueba en celular (T308)'],
    ['4', 'v2.8 · spec 004', 'Cuentas, recorridos y avisos', 'Login real por rol; panel con altas, recorridos y mensajes; pantalla del chofer; aviso "en ruta"; aviso de cercanía.', 'Hecha; probada en emulador el 11/11'],
    ['5', 'v2.9 · spec 005', 'Experiencia visual', 'Tarjetas con sombra, títulos con ícono, encabezados y login con profundidad, sin cambiar colores.', 'Hecha; falta confirmar en emulador'],
    ['6', 'v3.0', 'Campo y despliegue', 'GPS real con SIM800L, API en Hostinger VPS, datos de la Fase 3 en MySQL.', 'Planeada'],
    ['7', 'futuro', 'Mejoras', 'Colores de la ruta recorrida, ETA, varios camiones en el mapa.', 'Fuera de alcance actual'],
  ], { cebra: true, centrar: [0] }),

  h2('2.5 Product Backlog: épicas e historias de usuario'),
  p('Estas son las épicas e historias que se capturan en Jira, una por historia de las specs:'),
  tabla([2200, 5560, 800, 800], ['Épica', 'Historia de usuario', 'Prioridad', 'Entrega'], [
    ['E1 Ubicación en tiempo real', 'US1 El dispositivo reporta su ubicación real', 'P1', '1'],
    ['E1 Ubicación en tiempo real', 'US2 El ciudadano ve el camión en el mapa de la app', 'P1', '1'],
    ['E1 Ubicación en tiempo real', 'US3 El envío funciona por red celular (SIM800L)', 'P2', '6'],
    ['E1 Ubicación en tiempo real', 'US4 Distinguir dato real de simulación', 'P3', '1'],
    ['E2 Historial y monitoreo', 'H7 Ver por dónde ya pasó el camión', 'P1', '2'],
    ['E2 Historial y monitoreo', 'H8 Ver velocidad y distancia', 'P2', '2'],
    ['E2 Historial y monitoreo', 'H9 Mover el mapa sin que regrese solo', 'P3', '2'],
    ['E2 Historial y monitoreo', 'H10 Panel web del administrador', 'P1', '2'],
    ['E2 Historial y monitoreo', 'H11 Guardar las ubicaciones en MySQL', 'P1', '2'],
    ['E2 Historial y monitoreo', 'H12 Consultar colonias desde la API', 'P3', '2'],
    ['E3 Cuentas y validación', 'Login y registro rechazan correos mal escritos', 'P1', '3'],
    ['E3 Cuentas y validación', 'H13 Iniciar sesión con mi cuenta', 'P1', '4'],
    ['E3 Cuentas y validación', 'H14 Crear mi cuenta (ciudadano)', 'P1', '4'],
    ['E4 Operación del servicio', 'H15 Dar de alta camiones y choferes', 'P1', '4'],
    ['E4 Operación del servicio', 'H16 Asignar el recorrido del día', 'P1', '4'],
    ['E4 Operación del servicio', 'H17 Mandar mensajes al chofer', 'P2', '4'],
    ['E4 Operación del servicio', 'H18 Ver mi recorrido e iniciar la ruta (chofer)', 'P1', '4'],
    ['E5 Avisos al ciudadano', 'H19 Saber que el camión ya salió', 'P1', '4'],
    ['E5 Avisos al ciudadano', 'H20 Que me avisen cuando está cerca', 'P2', '4'],
    ['E6 Experiencia de usuario', 'H21 Pantallas con profundidad y orden', 'P1', '5'],
  ], { cebra: true, centrar: [2, 3], tam: 17 }),
  salto(),
];

// ---- 2.6 Cronograma (sección horizontal)
const SEMANAS = ['7 sep', '14 sep', '21 sep', '28 sep', '5 oct', '12 oct', '19 oct', '26 oct', '2 nov', '9 nov', '16 nov', '23 nov', '30 nov'];
const ACTIVIDADES = [
  ['Acta de constitución, interesados y alta en Jira', [1, 2], [2]],
  ['Plan de calidad y control de cambios', [3, 4, 5], []],
  ['Diagnóstico de la app original y elección del stack', [5, 6], [6, 7]],
  ['SDD: constitución, spec, plan y tasks (spec 001)', [6, 7], [7]],
  ['Sprint 1: MVP ubicación en tiempo real (v2.5)', [7, 8], [7, 8]],
  ['Sprint 2: historial, panel y MySQL (v2.6)', [8, 9], [7]],
  ['Auditoría de calidad del MVP, costos y riesgos', [9], [9]],
  ['Sprint 3: validación de correo (v2.7)', [9], [9, 10]],
  ['Sprint 4: roles, recorridos y avisos (v2.8)', [10, 11], [10]],
  ['Sprint 5: estilo visual (v2.9)', [11], [10]],
  ['GPS real con ESP32 y SIM800L', [9, 10, 11], []],
  ['Despliegue en Hostinger y bitácora de calidad (20 nov)', [11, 12], []],
  ['Cierre: reporte final, Notion y lecciones (30 nov)', [12, 13], []],
];
function cronograma() {
  const anchos = [4660, 440, ...SEMANAS.map(() => 600)];
  const ajuste = ANCHO_H - anchos.reduce((a, b) => a + b, 0);
  anchos[0] += ajuste;
  const filas = [];
  ACTIVIDADES.forEach(([nombre, plan, real], i) => {
    ['P', 'R'].forEach((tipo, k) => {
      const marcadas = k === 0 ? plan : real;
      filas.push(new TableRow({
        children: [
          ...(k === 0 ? [new TableCell({ rowSpan: 2, width: { size: anchos[0], type: WidthType.DXA }, margins: { left: 80, right: 80, top: 10, bottom: 10 }, children: [new Paragraph({ children: [t(`${i + 1}. ${nombre}`, { size: 16 })] })] })] : []),
          celda(tipo, anchos[1], { bold: true, align: AlignmentType.CENTER, size: 15, compacta: true }),
          ...SEMANAS.map((_, s) => celda(marcadas.includes(s + 1) ? 'X' : '', anchos[s + 2], {
            align: AlignmentType.CENTER, bold: true, size: 15, compacta: true,
            fondo: marcadas.includes(s + 1) ? (k === 0 ? 'CFE8D6' : VERDE_CLARO) : undefined,
            color: k === 0 ? VERDE : '1F4E79',
          })),
        ],
      }));
    });
  });
  const cab1 = new TableRow({ tableHeader: true, children: [
    celda('ACTIVIDAD', anchos[0], { bold: true, color: 'FFFFFF', fondo: VERDE }),
    celda('', anchos[1], { fondo: VERDE }),
    ...SEMANAS.map((s, i) => celda([new Paragraph({ alignment: AlignmentType.CENTER, children: [t(String(i + 1), { bold: true, color: 'FFFFFF', size: 17 })] }), new Paragraph({ alignment: AlignmentType.CENTER, children: [t(s, { color: 'FFFFFF', size: 13 })] })], anchos[i + 2], { fondo: VERDE })),
  ] });
  const obs = new TableRow({ children: [
    celda('OBSERVACIONES', anchos[0] + anchos[1], { bold: true }),
    new TableCell({ columnSpan: SEMANAS.length, width: { size: anchos.slice(2).reduce((a, b) => a + b, 0), type: WidthType.DXA }, margins: { left: 90, right: 90, top: 60, bottom: 60 }, children: [
      new Paragraph({ children: [t('P = planeado · R = realizado. Las filas R salen de las fechas reales del repositorio, las specs y el acta. Las actividades 2, 11, 12 y 13 todavía no tienen fecha real: el equipo las marca al terminarlas. El trabajo se adelantó en la semana 7 (historial y panel) y en la semana 10 (v2.8 y v2.9) gracias al apoyo de la IA; el GPS real se atrasó porque falta el SIM800L.', { size: 16 })] }),
    ] }),
  ] });
  return new Table({ width: { size: ANCHO_H, type: WidthType.DXA }, columnWidths: anchos, rows: [cab1, ...filas, obs] });
}
const seccionCronograma = [
  h2('2.6 Cronograma preliminar de actividades'),
  p('Periodo de realización: 7 de septiembre al 4 de diciembre de 2026 (13 semanas). Cada semana empieza en lunes.'),
  cronograma(),
];

// ---- 2.7 y 2.8, 3, 4, 5
const resto = [
  h2('2.7 Presupuesto integral (costos + IA + alojamiento)'),
  tabla([2000, 3360, 2000, 2000], ['Categoría', 'Descripción', 'Cantidad × precio', 'Subtotal (MXN)'], [
    ['Mano de obra (gestión y programación)', 'Horas de especificación y programación (H_p)', '50 h × $150', '$7,500.00'],
    ['Mano de obra (pruebas)', 'Horas de depuración y correcciones (H_d)', '20 h × $150', '$3,000.00'],
    ['Herramientas de IA', 'Claude plan Pro ($20 USD × $18.80)', '2 meses × $376.00', '$752.00'],
    ['Alojamiento', 'Hostinger VPS KVM 1: API, panel y MySQL en un servidor (primer mes)', '1 mes × $131.41', '$131.41'],
    [t('Subtotal', { bold: true, size: 18 }), 'Suma de las cuatro categorías', '', t('$11,383.41', { bold: true, size: 18 })],
    ['Reserva de riesgos', 'Contingencia por errores de la IA o retrasos (15%)', '$11,383.41 × 15%', '$1,707.51'],
    [t('COSTO TOTAL', { bold: true, size: 18 }), 'Presupuesto final del proyecto', '$11,383.41 + $1,707.51', t('$13,090.92', { bold: true, size: 18 })],
  ], { cebra: true }),
  p([t('Fuente: hoja "Costos_OAB.xlsx" (70 horas registradas del 14/10 al 06/11). Ahí la holgura F_r de 20% se aplicó solo a la mano de obra ($12,600 MXN); aquí se usa la reserva de 15% sobre las cuatro categorías que pide la plantilla. Los servicios mensuales son $507.41 MXN (Claude Pro + Hostinger).', { size: 18, italics: true })]),

  h2('2.8 Plan de gestión de riesgos'),
  tabla([400, 2160, 600, 2000, 2800, 1400], ['No.', 'Riesgo identificado', 'Prob.', 'Impacto', '¿Cómo se resuelve?', 'Tiempo'], [
    ['1', 'Escasez de recursos: se acaba la cuota de Claude Pro; solo hay 1 GPS de prueba y falta el SIM800L.', 'M', 'Se frena el avance; sin GPS real no se cierra la prueba física.', 'Avance escrito en specs y tasks para seguir sin IA; usar la simulación; no comprar sin comprobar que hace falta.', '1 a 3 días'],
    ['2', 'Contratiempos operativos: falla el internet, el celular no conecta con la API, Gradle pide otra versión de Java o el emulador se congela.', 'A', 'No hay demostración o la app no compila el día de la entrega.', 'La API funciona sin MySQL; respaldos por versión en GitHub; arranque limpio del emulador; adb reverse.', '1 a 2 horas'],
    ['3', 'Bajo desempeño: código de la IA con errores, GPS con mala señal, muchas consultas con 6 camiones.', 'M', 'El camión aparece tarde o brincando; la app se siente lenta.', 'Revisar el código y correr las 28 pruebas; índice por camión y fecha; historial limitado a 500 puntos.', '1 día'],
    ['4', 'Falta de claridad: prompts ambiguos que hacen que la IA invente requisitos (ETA).', 'M', 'Retrabajo: se programa lo que nadie pidió.', 'SDD: spec y clarify antes del código, requisitos EARS y lo que queda fuera escrito en la constitución.', '1 hora'],
    ['5', 'Corrupción del alcance: agregar funciones que la IA hace fácil sin terminar el MVP.', 'A', 'El GPS real se retrasa y la constitución deja de coincidir con el código.', 'Toda función nueva necesita enmienda y su propia spec (así se hizo la v3.0.0).', '1 semana'],
    ['6', 'Costos y tiempo: ignorar las horas de depuración y la holgura.', 'M', 'El presupuesto sale bajo (sin H_d bajaría de $12,600 a $8,400).', 'Separar H_p y H_d y sumar holgura; actualizar las horas cada semana.', '30 min por semana'],
  ], { cebra: true, centrar: [0, 2], tam: 16 }),
  salto(),

  h1('3. Ejecución'),
  h2('3.1 Sprints de desarrollo'),
  p('El código se construyó por versiones; cada una tiene su rama o commit en GitHub y su spec:'),
  ...[
    ['v2.5 (MVP, 22–23 oct): ', 'API Express con validación y 19 pruebas, simulador, firmware de prueba del GPS y mapa en la app con estados.'],
    ['v2.6 (23 oct): ', 'historial, velocidad, distancia, panel web y MySQL. Auditoría de calidad del MVP el 2 de noviembre.'],
    ['v2.7 (2–11 nov): ', 'validación de correo en login y registro; rama main.'],
    ['v2.8 (11 nov): ', 'constitución 3.0.0, API con cuentas y roles (28 pruebas), panel del administrador, pantalla del chofer, avisos y cercanía; rama version-2.8, probada en el emulador.'],
    ['v2.9 (11 nov): ', 'estilo visual con sombras, tarjetas e íconos sin cambiar colores; rama version-2.9.'],
  ].map(([a, b]) => viñeta(negrita(a, b))),
  h2('3.2 Integración de IA'),
  p('Se usó Claude (plan Pro) como asistente dentro del flujo SDD:'),
  ...[
    'Redactar la constitución, las specs con requisitos EARS, el plan y las tareas, siempre en español.',
    'Programar la API, las pantallas de la app y el panel, y escribir las pruebas automáticas.',
    'Hacer la auditoría de calidad, la hoja de costos y riesgos, los diagramas y este documento.',
    'Diagnosticar problemas: el emulador sin ubicación, la conexión con adb reverse y la ubicación de prueba en Huauchinango.',
    'Regla del equipo: nada se acepta sin revisarlo y sin que pase su prueba; la IA no borra archivos sin permiso.',
  ].map((x) => viñeta(x)),

  h1('4. Monitoreo y control de calidad'),
  p('Bitácora con los mismos rubros para cada funcionalidad: 4.1 MVP, 4.2 fase 2 (avance al 20 de noviembre) y 4.3 fase 3 (cierre del 30 de noviembre).'),
  tabla([700, 1500, 2160, 1900, 1000, 2100], ['ID', 'Requisito', 'Criterio de aceptación', 'Prueba', 'Resultado', 'Evidencia'], [
    ['TOR-01', 'Validar la ubicación (FR-004)', 'Datos inválidos → 400 con {error}; válidos → 200', '5 POST inválidos con curl y npm test', 'Aprobado', 'evidencia/auditoria-2026-11-02'],
    ['TOR-02', 'Última ubicación (FR-005/006)', '404 sin datos; 200 con la fecha UTC de la API', 'GET, POST con fecha falsa, GET', 'Aprobado', 'api-curl.txt pasos 1, 8, 9'],
    ['TOR-03', 'Camión en el mapa (FR-007/008)', 'Mapa con el camión y "Simulación · hora"; se mueve en ≤ 20 s', 'App en el celular con la simulación', 'Aprobado', 'reporte/inicio-celular.jpg'],
    ['TOR-04', 'Historial y estadísticas', 'Últimos N puntos en orden; distancia y velocidad', 'Simular 8 puntos', 'Aprobado', 'api-curl.txt pasos 12, 13'],
    ['TOR-05', 'Panel web (FR-106)', '/panel con estado, mapa y ruta; se actualiza cada 5 s', 'Abrir /panel', 'Aprobado', 'reporte/panel-web.png'],
    ['TOR-06', 'Validar correo (spec 003)', '"hola.com" muestra mensaje y no entra', 'Prueba unitaria ValidarCorreoTest', 'Pendiente', 'T308, T309'],
    ['TOR-07', 'Login con rol (FR-401/404)', 'Cada rol entra a su pantalla; sin token 401, otro rol 403', 'npm test (28/28) y emulador', 'Aprobado', 'api/test/v3.test.js'],
    ['TOR-08', 'Aviso "camión en ruta" (H19)', 'Al iniciar la ruta, el ciudadano recibe la notificación', 'Chofer inicia ruta en el emulador', 'Aprobado', 'Demo 11/11 (T419)'],
    ['TOR-09', 'Aviso de cercanía (H20)', 'Notifica a menos de 500 m', 'Emulador en 20.1765, -98.0494 (329 m)', 'Aprobado', 'Demo 11/11 (T419)'],
    ['TOR-10', 'Estilo visual (spec 005)', 'Compila y se ve con profundidad sin cambiar colores', 'Compilar y recorrer pantallas', 'Pendiente', 'T508, T509'],
    ['TOR-11', 'GPS real (US1)', 'Coordenada a menos de ~20 m de Google Maps', 'Prueba en cielo abierto', 'Pendiente', 'T013, T015'],
  ], { cebra: true, tam: 16 }),
  salto(),

  h1('5. Cierre del proyecto'),
  h2('5.1 Entrega final del software'),
  p('El código completo está en GitHub, en el repositorio público truck-on-route-publico (copia de la rama version-2.9). Hoy el sistema corre en una computadora local (API en el puerto 3000, panel en /panel y app en el emulador o el celular). El despliegue planeado es Hostinger VPS KVM 1 ($131.41 MXN al mes), que incluye la API, el panel y MySQL en un solo servidor.'),
  h2('5.2 Viabilidad y deuda técnica'),
  tabla([4200, 5160], ['Deuda técnica', 'Cómo se resuelve'], [
    ['La pantalla de Perfil tiene datos de ejemplo de una integrante del equipo (T034).', 'Mostrar los datos de la cuenta con sesión; antes de hacer público el repositorio.'],
    ['Archivos sin uso: LoginScreen.kt, GestionUsariosScreen.kt y TruckCard.kt vacío.', 'Borrarlos con permiso del equipo (T023).'],
    ['Datos de la Fase 3 en JSON y ubicaciones en MySQL: dos lugares.', 'Pasar usuarios, rutas y recorridos a las tablas de db/esquema.sql.'],
    ['Las sesiones viven en memoria: al reiniciar la API hay que volver a entrar.', 'Guardar los tokens o usar JWT firmados.'],
    ['La API usa HTTP sin cifrar (el SIM800L tiene problemas con HTTPS).', 'HTTPS para la app y el panel; HTTP solo para el dispositivo.'],
    ['GPS real y red celular sin probar.', 'Tareas T013, T015 y T027 a T032.'],
  ], { cebra: true }),
  p([t('Dictamen: ', { bold: true }), t('el sistema está listo para una demostración y un piloto local con un camión. Para producción falta el GPS real con SIM800L, el despliegue en el VPS, HTTPS y resolver la deuda técnica de la tabla.')]),
  h2('5.3 Lecciones aprendidas'),
  ...[
    'Escribir la spec antes del código evitó que la IA agregara funciones que nadie pidió, como el ETA.',
    'La IA acelera mucho, pero hay que revisar: una copia trajo de regreso colonias de Pachuca y hubo que corregirlas.',
    'Cada función nueva necesitó enmendar la constitución; así el documento y el código siempre coinciden.',
    'Guardar cada versión en su rama de GitHub y hacer respaldos nos dejó regresar sin miedo.',
    'Separar horas de programación y de depuración dio un presupuesto más real: 29% del tiempo fue depuración.',
    'El hardware es lo más lento: conviene pedir los módulos (SIM800L) desde el inicio.',
  ].map((x) => viñeta(x)),

  h1('Anexo: enlaces'),
  ...[
    ['Repositorio público: ', ARBOL],
    ['Diagrama de arquitectura: ', ARBOL + 'docs/diagramas/arquitectura.html'],
    ['Diagrama de la base de datos: ', ARBOL + 'docs/diagramas/base-de-datos.html'],
    ['Constitución: ', ARBOL + '.specify/memory/constitution.md'],
    ['Especificaciones: ', ARBOL + 'specs'],
  ].map(([a, u]) => viñeta([t(a, { bold: true }), enlace(u, u)])),
];

// ---------- documento
const estiloTitulo = (tam, color) => ({ run: { font: 'Arial', size: tam, bold: true, color }, paragraph: { spacing: { before: 240, after: 120 }, keepNext: true } });
const doc = new Document({
  creator: 'Equipo G2VSIS07',
  title: 'Proyecto final · Truck on Route',
  styles: {
    default: { document: { run: { font: 'Arial', size: 21 } } },
    paragraphStyles: [
      { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true, ...estiloTitulo(32, VERDE), paragraph: { spacing: { before: 240, after: 160 }, keepNext: true, outlineLevel: 0, border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: VERDE, space: 4 } } } },
      { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true, ...estiloTitulo(26, '1F1F1F'), paragraph: { spacing: { before: 240, after: 120 }, keepNext: true, outlineLevel: 1 } },
      { id: 'Heading3', name: 'Heading 3', basedOn: 'Normal', next: 'Normal', quickFormat: true, ...estiloTitulo(22, VERDE), paragraph: { spacing: { before: 160, after: 80 }, keepNext: true, outlineLevel: 2 } },
    ],
  },
  numbering: { config: [{ reference: 'vinetas', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 360, hanging: 240 } } } }] }] },
  features: { updateFields: true },
  sections: [
    { properties: { page: { size: { width: 12240, height: 15840 }, margin: { top: 1440, bottom: 1440, left: 1440, right: 1440 } } },
      footers: { default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [t('Truck on Route · Proyecto final · G2VSIS07 · página ', { size: 16, color: '777777' }), new TextRun({ children: [PageNumber.CURRENT], size: 16, color: '777777' })] })] }) },
      children: [...portada, ...inicio, ...planeacion] },
    { properties: { page: { size: { width: 12240, height: 15840, orientation: PageOrientation.LANDSCAPE }, margin: { top: 1080, bottom: 1080, left: 1440, right: 1440 } } },
      footers: { default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [t('Truck on Route · Proyecto final · G2VSIS07 · página ', { size: 16, color: '777777' }), new TextRun({ children: [PageNumber.CURRENT], size: 16, color: '777777' })] })] }) },
      children: seccionCronograma },
    { properties: { page: { size: { width: 12240, height: 15840 }, margin: { top: 1440, bottom: 1440, left: 1440, right: 1440 } } },
      footers: { default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [t('Truck on Route · Proyecto final · G2VSIS07 · página ', { size: 16, color: '777777' }), new TextRun({ children: [PageNumber.CURRENT], size: 16, color: '777777' })] })] }) },
      children: resto },
  ],
});

const salida = path.join(__dirname, 'ProyectoFinal_TruckOnRoute_G2VSIS07.docx');
Packer.toBuffer(doc).then((b) => { fs.writeFileSync(salida, b); console.log('Listo:', salida); });
