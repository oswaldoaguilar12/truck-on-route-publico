# Genera los diagramas HTML de Truck on Route (estilo Archify).
# Uso: python3 generar.py  -> crea arquitectura.html y base-de-datos.html en esta carpeta.
import os

AQUI = os.path.dirname(os.path.abspath(__file__))

CABEZA = r"""<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>__TITULO__</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&display=swap" rel="stylesheet">
<style>
:root, [data-theme="dark"] {
  --bg:#0b1120; --card:#0f172a; --border:#1e293b; --text:#e2e8f0; --muted:#94a3b8; --dim:#64748b; --grid:#1e293b; --mask:#0f172a;
  --fe-f:rgba(8,51,68,.55); --fe-s:#22d3ee; --fe-t:#67e8f9;
  --be-f:rgba(6,78,59,.5); --be-s:#34d399; --be-t:#6ee7b7;
  --db-f:rgba(46,16,101,.5); --db-s:#a78bfa; --db-t:#c4b5fd;
  --cl-f:rgba(69,26,3,.5); --cl-s:#fbbf24; --cl-t:#fcd34d;
  --se-f:rgba(76,5,25,.5); --se-s:#fb7185; --se-t:#fda4af;
  --mb-f:rgba(67,20,7,.55); --mb-s:#fb923c; --mb-t:#fdba74;
  --ex-f:rgba(30,41,59,.6); --ex-s:#94a3b8; --ex-t:#cbd5e1;
  --arrow:#64748b; --emph:#34d399;
}
[data-theme="light"] {
  --bg:#f8fafc; --card:#ffffff; --border:#e2e8f0; --text:#0f172a; --muted:#475569; --dim:#94a3b8; --grid:#eef2f7; --mask:#ffffff;
  --fe-f:rgba(207,250,254,.7); --fe-s:#0891b2; --fe-t:#0e7490;
  --be-f:rgba(209,250,229,.7); --be-s:#059669; --be-t:#047857;
  --db-f:rgba(237,233,254,.75); --db-s:#7c3aed; --db-t:#6d28d9;
  --cl-f:rgba(254,243,199,.75); --cl-s:#d97706; --cl-t:#b45309;
  --se-f:rgba(255,228,230,.75); --se-s:#e11d48; --se-t:#be123c;
  --mb-f:rgba(255,237,213,.8); --mb-s:#ea580c; --mb-t:#c2410c;
  --ex-f:rgba(241,245,249,.9); --ex-s:#64748b; --ex-t:#334155;
  --arrow:#94a3b8; --emph:#059669;
}
* { box-sizing:border-box; }
body { margin:0; background:var(--bg); color:var(--text); font-family:'JetBrains Mono',ui-monospace,monospace; padding:32px 16px 24px; }
.wrap { max-width:1180px; margin:0 auto; }
header h1 { font-size:22px; margin:0 0 6px; display:flex; align-items:center; gap:10px; }
header p { margin:0 0 20px; color:var(--muted); font-size:13px; }
.dot { width:10px; height:10px; border-radius:50%; background:var(--emph); box-shadow:0 0 0 0 var(--emph); animation:pulso 2s infinite; }
@keyframes pulso { 0%{box-shadow:0 0 0 0 rgba(52,211,153,.6)} 70%{box-shadow:0 0 0 10px rgba(52,211,153,0)} 100%{box-shadow:0 0 0 0 rgba(52,211,153,0)} }
.diagrama { background:var(--card); border:1px solid var(--border); border-radius:14px; padding:12px; overflow-x:auto; }
.diagrama svg { width:100%; min-width:760px; height:auto; display:block; font-family:'JetBrains Mono',ui-monospace,monospace; }
.cards { display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:14px; margin-top:18px; }
.card { background:var(--card); border:1px solid var(--border); border-radius:12px; padding:14px 16px; }
.card-header { display:flex; align-items:center; gap:8px; }
.card-header h3 { margin:0; font-size:13px; }
.card ul { list-style:none; padding:0; margin:10px 0 0; color:var(--muted); font-size:12px; line-height:1.7; }
.card-dot { width:9px; height:9px; border-radius:50%; }
.card-dot.cyan{background:#22d3ee}.card-dot.emerald{background:#34d399}.card-dot.violet{background:#a78bfa}.card-dot.amber{background:#fbbf24}.card-dot.rose{background:#fb7185}.card-dot.orange{background:#fb923c}.card-dot.slate{background:#94a3b8}
footer { margin-top:18px; color:var(--dim); font-size:11px; text-align:center; }
.toolbar { position:fixed; top:12px; right:12px; display:flex; gap:8px; z-index:10; }
.toolbar button { background:var(--card); color:var(--text); border:1px solid var(--border); border-radius:8px; padding:6px 10px; font:inherit; font-size:12px; cursor:pointer; }
.menu { position:relative; }
.menu-lista { display:none; position:absolute; right:0; top:34px; background:var(--card); border:1px solid var(--border); border-radius:8px; min-width:200px; overflow:hidden; }
.menu-lista.abierto { display:block; }
.menu-lista button { display:block; width:100%; text-align:left; border:0; border-radius:0; padding:8px 12px; }
.menu-lista button:hover { background:var(--border); }
/* clases del SVG */
.c-grid { stroke:var(--grid); fill:none; }
.c-mask { fill:var(--mask); }
.c-frontend{fill:var(--fe-f);stroke:var(--fe-s)} .t-frontend{fill:var(--fe-t)}
.c-backend{fill:var(--be-f);stroke:var(--be-s)} .t-backend{fill:var(--be-t)}
.c-database{fill:var(--db-f);stroke:var(--db-s)} .t-database{fill:var(--db-t)}
.c-cloud{fill:var(--cl-f);stroke:var(--cl-s)} .t-cloud{fill:var(--cl-t)}
.c-security{fill:var(--se-f);stroke:var(--se-s)} .t-security{fill:var(--se-t)}
.c-messagebus{fill:var(--mb-f);stroke:var(--mb-s)} .t-messagebus{fill:var(--mb-t)}
.c-external{fill:var(--ex-f);stroke:var(--ex-s)} .t-external{fill:var(--ex-t)}
.c-security-group{fill:none;stroke:var(--se-s);stroke-dasharray:5 4}
.c-region{fill:none;stroke:var(--cl-s);stroke-dasharray:8 5}
.t-primary{fill:var(--text)} .t-muted{fill:var(--muted)} .t-dim{fill:var(--dim)}
.a-default{stroke:var(--arrow);fill:none} .a-emphasis{stroke:var(--emph);fill:none}
.a-security{stroke:var(--se-s);fill:none;stroke-dasharray:5 4} .a-dashed{stroke:var(--arrow);fill:none;stroke-dasharray:4 4}
.m-default{fill:var(--arrow)} .m-emphasis{fill:var(--emph)} .m-security{fill:var(--se-s)}
</style>
</head>
<body>
<div class="toolbar">
  <button id="tema" type="button">☀︎ Claro</button>
  <div class="menu">
    <button id="exportar" type="button">Exportar ▾</button>
    <div class="menu-lista" id="lista">
      <button type="button" data-f="copiar">Copiar PNG</button>
      <button type="button" data-f="png">Descargar PNG</button>
      <button type="button" data-f="jpeg">Descargar JPEG</button>
      <button type="button" data-f="webp">Descargar WebP</button>
      <button type="button" data-f="svg">Descargar SVG</button>
    </div>
  </div>
</div>
<div class="wrap">
<header><h1><span class="dot"></span>__TITULO__</h1><p>__SUBTITULO__</p></header>
<div class="diagrama">
"""

DEFS = """<defs>
<pattern id="grid" width="24" height="24" patternUnits="userSpaceOnUse"><path d="M24 0H0V24" class="c-grid" stroke-width="1"/></pattern>
<marker id="arrowhead" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" class="m-default"/></marker>
<marker id="arrowhead-emphasis" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" class="m-emphasis"/></marker>
<marker id="arrowhead-security" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" class="m-security"/></marker>
<marker id="arrowhead-dashed" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" class="m-default"/></marker>
</defs>"""

PIE = r"""
</div>
<div class="cards">__TARJETAS__</div>
<footer>__PIE__</footer>
</div>
<script>
(function () {
  var raiz = document.documentElement, boton = document.getElementById('tema');
  function poner(t) { raiz.setAttribute('data-theme', t); boton.textContent = t === 'dark' ? '☀︎ Claro' : '☾ Oscuro'; try { localStorage.setItem('tema-tor', t); } catch (e) {} }
  var guardado = null; try { guardado = localStorage.getItem('tema-tor'); } catch (e) {}
  poner(guardado || (window.matchMedia && matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark'));
  boton.onclick = function () { poner(raiz.getAttribute('data-theme') === 'dark' ? 'light' : 'dark'); };

  var lista = document.getElementById('lista');
  document.getElementById('exportar').onclick = function (e) { e.stopPropagation(); lista.classList.toggle('abierto'); };
  document.addEventListener('click', function () { lista.classList.remove('abierto'); });

  var PROPS = ['fill', 'stroke', 'stroke-width', 'stroke-dasharray', 'opacity', 'font-family', 'font-size', 'font-weight', 'text-anchor'];
  function svgConEstilos() {
    var orig = document.querySelector('.diagrama svg'), copia = orig.cloneNode(true);
    var a = orig.querySelectorAll('*'), b = copia.querySelectorAll('*');
    for (var i = 0; i < a.length; i++) {
      var cs = getComputedStyle(a[i]), s = '';
      PROPS.forEach(function (p) { var v = cs.getPropertyValue(p); if (v) s += p + ':' + v + ';'; });
      b[i].setAttribute('style', s); b[i].removeAttribute('class');
    }
    var vb = orig.viewBox.baseVal;
    copia.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
    copia.setAttribute('width', vb.width); copia.setAttribute('height', vb.height);
    copia.setAttribute('style', 'background:' + getComputedStyle(document.body).backgroundColor);
    return { texto: new XMLSerializer().serializeToString(copia), w: vb.width, h: vb.height };
  }
  function descargar(blob, nombre) { var u = URL.createObjectURL(blob), l = document.createElement('a'); l.href = u; l.download = nombre; l.click(); setTimeout(function () { URL.revokeObjectURL(u); }, 1000); }
  function rasterizar(tipo, listo) {
    var s = svgConEstilos(), img = new Image(), esc = 4;
    img.onload = function () {
      var c = document.createElement('canvas'); c.width = s.w * esc; c.height = s.h * esc;
      var g = c.getContext('2d'); g.fillStyle = getComputedStyle(document.body).backgroundColor; g.fillRect(0, 0, c.width, c.height);
      g.drawImage(img, 0, 0, c.width, c.height); c.toBlob(listo, tipo, 0.95);
    };
    img.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(s.texto);
  }
  var NOMBRE = '__ARCHIVO__';
  lista.addEventListener('click', function (e) {
    var f = e.target.getAttribute('data-f'); if (!f) return;
    if (f === 'svg') { descargar(new Blob([svgConEstilos().texto], { type: 'image/svg+xml' }), NOMBRE + '.svg'); return; }
    if (f === 'copiar') { rasterizar('image/png', function (b) { navigator.clipboard.write([new ClipboardItem({ 'image/png': b })]).catch(function () { descargar(b, NOMBRE + '.png'); }); }); return; }
    rasterizar('image/' + f, function (b) { descargar(b, NOMBRE + '.' + (f === 'jpeg' ? 'jpg' : f)); });
  });
})();
</script>
</body>
</html>
"""


def caja(x, y, w, h, tipo, titulo, *sub, tam=11):
    cx = x + w / 2
    s = f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" class="c-mask"/>'
    s += f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" class="c-{tipo}" stroke-width="1.5"/>'
    alto = 16 * len(sub)
    y0 = y + h / 2 - alto / 2 + 4
    s += f'<text x="{cx}" y="{y0}" class="t-primary" font-size="{tam}" font-weight="600" text-anchor="middle">{titulo}</text>'
    for i, t in enumerate(sub):
        s += f'<text x="{cx}" y="{y0 + 15 + i * 13}" class="t-muted" font-size="9" text-anchor="middle">{t}</text>'
    return s


def flecha(d, tipo="default", ancho=1.5):
    m = {"default": "arrowhead", "emphasis": "arrowhead-emphasis", "security": "arrowhead-security", "dashed": "arrowhead-dashed"}[tipo]
    return f'<path d="{d}" class="a-{tipo}" stroke-width="{ancho}" marker-end="url(#{m})"/>'


def texto(x, y, t, clase="t-muted", tam=8, ancla="middle", peso="400"):
    return f'<text x="{x}" y="{y}" class="{clase}" font-size="{tam}" font-weight="{peso}" text-anchor="{ancla}">{t}</text>'


def tarjeta(color, titulo, items):
    li = "".join(f"<li>&bull; {i}</li>" for i in items)
    return f'<div class="card"><div class="card-header"><div class="card-dot {color}"></div><h3>{titulo}</h3></div><ul>{li}</ul></div>'


def pagina(archivo, titulo, subtitulo, ancho, alto, cuerpo, tarjetas, pie):
    svg = f'<svg viewBox="0 0 {ancho} {alto}" role="img" aria-label="{titulo}">{DEFS}<rect width="{ancho}" height="{alto}" fill="url(#grid)"/>{cuerpo}</svg>'
    html = (CABEZA.replace("__TITULO__", titulo).replace("__SUBTITULO__", subtitulo) + svg +
            PIE.replace("__TARJETAS__", "".join(tarjetas)).replace("__PIE__", pie).replace("__ARCHIVO__", archivo))
    with open(os.path.join(AQUI, archivo + ".html"), "w", encoding="utf-8") as f:
        f.write(html)


# ---------------------------------------------------------------- a) Arquitectura
def arquitectura():
    c = []
    # Región del servidor (se dibuja primero, queda al fondo)
    c.append('<rect x="320" y="56" width="450" height="474" rx="10" class="c-region" stroke-width="1"/>')
    c.append(texto(332, 74, "Servidor · hoy: PC local (localhost:3000) · plan: Hostinger VPS KVM 1", "t-cloud", 8, "start"))
    c.append('<rect x="20" y="560" width="1060" height="110" rx="10" class="c-region" stroke-width="1"/>')
    c.append(texto(32, 578, "Cómo se construyó: gestión, IA y SDD", "t-cloud", 8, "start"))

    # Flechas (antes que las cajas)
    c.append(flecha("M220 150 H358", "emphasis"))
    c.append(texto(270, 120, "POST /ubicacion", "t-backend", 8))
    c.append(texto(270, 132, "cada 10 s", "t-backend", 8))
    c.append(flecha("M220 260 H290 V170 H358", "dashed", 1))
    c.append(texto(255, 252, "pruebas sin GPS", "t-dim", 7))
    for x in (410, 545):
        c.append(flecha(f"M{x} 190 V228"))
    c.append(flecha("M605 259 H618", "security"))
    c.append(flecha("M410 288 V338"))
    c.append(flecha("M545 288 V338"))
    c.append(flecha("M440 288 V316 H680 V338"))
    c.append(texto(612, 312, "si hay .env", "t-dim", 7))
    c.append(flecha("M840 140 H772"))
    c.append(texto(806, 132, "GET /ubicacion", "t-dim", 7))
    c.append(texto(806, 158, "GET /avisos", "t-dim", 7))
    c.append(flecha("M840 238 H805 V172 H772", "security"))
    c.append(flecha("M1060 358 H1082 V142 H1062", "dashed", 1))
    c.append(flecha("M840 472 H742"))
    c.append(texto(790, 464, "navegador", "t-dim", 7))
    c.append(flecha("M350 472 H334 V172 H348", "dashed", 1))
    c.append(texto(330, 520, "fetch + token", "t-dim", 7, "start"))

    # Dispositivos
    c.append(caja(40, 110, 180, 80, "cloud", "ESP32 + GPS NEO-6M", "firmware C++ (Arduino IDE)", "TinyGPSPlus · Serial2 9600"))
    c.append('<rect x="244" y="141" width="92" height="18" rx="4" class="c-messagebus" stroke-width="1"/>')
    c.append(texto(290, 154, "WiFi / SIM800L", "t-messagebus", 7))
    c.append(caja(40, 230, 180, 60, "external", "Simulador", "npm run simular", "fuente: simulacion"))

    # API y módulos
    c.append(caja(350, 110, 400, 80, "backend", "API Node.js + Express 5", "server.js · app.js · puerto 3000", "28 pruebas con node --test", tam=12))
    c.append(caja(350, 230, 120, 58, "backend", "Ubicación", "validar.js · geo.js", "historial y velocidad"))
    c.append(caja(485, 230, 120, 58, "backend", "rutas-v3.js", "sesión · admin", "chofer · avisos"))
    c.append(caja(620, 230, 120, 58, "security", "seguridad.js", "contraseña scrypt", "token de 7 días"))

    # Datos
    c.append(caja(350, 340, 120, 70, "database", "Memoria", "última ubicación", "e historial (500)"))
    c.append(caja(485, 340, 120, 70, "database", "tor-datos.json", "usuarios · camiones", "rutas · recorridos", "mensajes · avisos"))
    c.append(caja(620, 340, 120, 70, "database", "MySQL 8", "camiones", "ubicaciones", "(opcional, .env)"))

    # Panel servido por la API
    c.append(caja(350, 445, 390, 55, "frontend", "Panel del administrador  /panel", "HTML + JavaScript + Leaflet · lo sirve la misma API"))

    # Clientes
    c.append(caja(840, 100, 220, 84, "frontend", "App Android · Ciudadano", "Kotlin + Jetpack Compose", "mapa osmdroid · avisos", "aviso a 500 m (cada 5 min)"))
    c.append(caja(840, 208, 220, 62, "frontend", "App Android · Chofer", "su recorrido e indicaciones", "Iniciar / Terminar ruta"))
    c.append(caja(840, 330, 220, 56, "external", "Mapa Esri World Street Map", "imágenes del mapa (sin clave)"))
    c.append(caja(840, 445, 220, 55, "external", "Administrador", "altas, recorridos y mensajes"))

    # Herramientas
    herramientas = [("Spec Kit (SDD)", "spec → plan → tasks"), ("Claude", "IA: spec, código, pruebas"),
                    ("GitHub", "versiones 2.5 → 2.9"), ("Jira", "épicas e historias"),
                    ("Notion", "reporte y bitácora"), ("Android Studio", "compilar y emulador")]
    for i, (t, s) in enumerate(herramientas):
        c.append(caja(40 + i * 172, 594, 160, 56, "external", t, s, tam=10))

    tarjetas = [
        tarjeta("emerald", "Flujo principal", ["El ESP32 lee el GPS y manda la ubicación a la API cada 10 s.",
                                               "La API valida, pone la hora UTC y la guarda.",
                                               "La app pide la última ubicación cada 10 s y la dibuja en el mapa."]),
        tarjeta("rose", "Seguridad", ["Contraseñas con scrypt (nunca en texto plano).",
                                      "Token Bearer de 7 días; cada ruta revisa el rol.",
                                      "Secretos en .env, secrets.h y local.properties, fuera de git."]),
        tarjeta("amber", "Pendiente", ["GPS real con SIM800L (tareas T013 y T015).",
                                       "Subir la API a Hostinger VPS KVM 1 ($131.41 MXN al mes).",
                                       "Colores de la ruta recorrida y ETA: fuera de alcance por ahora."]),
    ]
    pagina("arquitectura", "Truck on Route · Arquitectura del sistema",
           "a) Estructura del proyecto en la versión 2.9: dispositivo GPS, API, datos, app Android y panel web",
           1100, 690, "".join(c), tarjetas,
           "Equipo G2VSIS07 · ITSH · Gestión de Proyectos de Software · versión 2.9 · noviembre 2026")


# ---------------------------------------------------------------- b) Base de datos
TABLAS = {
    "colonias": (40, 80, [("PK", "id_colonia", "INT"), ("UQ", "nombre", "VARCHAR(100)"), ("", "codigo_postal", "CHAR(5)")]),
    "ruta_colonias": (410, 80, [("PK FK", "id_ruta", "INT"), ("PK FK", "id_colonia", "INT"), ("PK", "dia_semana", "TINYINT 1-7"),
                                ("", "orden", "SMALLINT"), ("", "hora_estimada", "TIME")]),
    "rutas": (780, 80, [("PK", "id_ruta", "INT"), ("UQ", "nombre", "VARCHAR(80)"), ("", "descripcion", "VARCHAR(255)"), ("", "activa", "BOOLEAN")]),
    "usuarios": (40, 300, [("PK", "id_usuario", "INT"), ("", "nombre", "VARCHAR(100)"), ("UQ", "correo", "VARCHAR(150)"),
                           ("", "contrasena_hash", "VARCHAR(255)"), ("", "rol", "ENUM ciudadano|admin|conductor"),
                           ("FK", "id_colonia", "INT"), ("", "avisos_activos", "BOOLEAN"), ("", "creado_en", "DATETIME")]),
    "recorridos": (410, 300, [("PK", "id_recorrido", "INT"), ("FK", "id_ruta", "INT"), ("FK", "id_camion", "INT"),
                              ("FK", "id_conductor", "INT"), ("", "inicio", "DATETIME"), ("", "fin", "DATETIME"),
                              ("", "estado", "ENUM programado|en_curso|...")]),
    "camiones": (780, 300, [("PK", "id_camion", "INT"), ("UQ", "clave", "VARCHAR(20)"), ("UQ", "placas", "VARCHAR(15)"),
                            ("", "modelo", "VARCHAR(60)"), ("", "capacidad_kg", "INT"), ("", "activo", "BOOLEAN")]),
    "notificaciones": (40, 560, [("PK", "id_notificacion", "INT"), ("FK", "id_usuario", "INT"), ("FK", "id_recorrido", "INT"),
                                 ("", "titulo", "VARCHAR(80)"), ("", "mensaje", "VARCHAR(255)"), ("", "leida", "BOOLEAN"), ("", "creada_en", "DATETIME")]),
    "ubicaciones": (410, 560, [("PK", "id_ubicacion", "BIGINT"), ("FK", "id_camion", "INT"), ("FK", "id_recorrido", "INT"),
                               ("", "lat", "DECIMAL(9,6)"), ("", "lng", "DECIMAL(9,6)"), ("", "fuente", "ENUM gps|simulacion"), ("", "fecha", "DATETIME(3) UTC")]),
}
ANCHO_T, CAB, FILA = 280, 28, 18


def tabla(nombre, x, y, cols):
    h = CAB + FILA * len(cols) + 8
    s = f'<rect x="{x}" y="{y}" width="{ANCHO_T}" height="{h}" rx="6" class="c-mask"/>'
    s += f'<rect x="{x}" y="{y}" width="{ANCHO_T}" height="{h}" rx="6" class="c-database" stroke-width="1.5"/>'
    s += f'<line x1="{x}" y1="{y + CAB}" x2="{x + ANCHO_T}" y2="{y + CAB}" class="a-default" stroke-width="1"/>'
    s += texto(x + 12, y + 19, nombre, "t-primary", 12, "start", "700")
    for i, (llave, col, tipo) in enumerate(cols):
        yy = y + CAB + 15 + i * FILA
        clase = "t-security" if "PK" in llave else ("t-cloud" if "FK" in llave else ("t-frontend" if llave else "t-dim"))
        s += texto(x + 12, yy, llave, clase, 8, "start", "700")
        s += texto(x + 58, yy, col, "t-primary", 10, "start")
        s += texto(x + ANCHO_T - 12, yy, tipo, "t-muted", 8, "end")
    return s


def relacion(d, x1, y1, x2, y2):
    s = f'<path d="{d}" class="a-default" stroke-width="1.5"/>'
    s += texto(x1, y1, "1", "t-backend", 10, "middle", "700") + texto(x2, y2, "N", "t-backend", 10, "middle", "700")
    return s


def base_de_datos():
    c = []
    # Relaciones primero
    c.append(relacion("M180 170 V300", 190, 186, 190, 292))                 # colonias → usuarios
    c.append(relacion("M320 120 H410", 330, 114, 400, 114))                 # colonias → ruta_colonias
    c.append(relacion("M780 120 H690", 770, 114, 700, 114))                 # rutas → ruta_colonias
    c.append(relacion("M850 188 V250 H620 V300", 860, 204, 630, 292))       # rutas → recorridos
    c.append(relacion("M780 360 H690", 770, 354, 700, 354))                 # camiones → recorridos
    c.append(relacion("M320 375 H410", 330, 369, 400, 369))                 # usuarios (conductor) → recorridos
    c.append(texto(365, 390, "conductor", "t-dim", 7))
    c.append(relacion("M920 444 V620 H690", 930, 460, 700, 614))            # camiones → ubicaciones
    c.append(relacion("M550 462 V560", 560, 478, 560, 552))                 # recorridos → ubicaciones
    c.append(relacion("M180 480 V560", 190, 496, 190, 552))                 # usuarios → notificaciones
    c.append(relacion("M410 440 H365 V640 H320", 400, 434, 330, 634))       # recorridos → notificaciones

    for n, (x, y, cols) in TABLAS.items():
        c.append(tabla(n, x, y, cols))

    # Nota sobre la v2.8
    c.append('<rect x="780" y="660" width="280" height="96" rx="8" class="c-security-group" stroke-width="1"/>')
    for i, t in enumerate(["Nota de la versión 2.8 / 2.9:", "MySQL guarda hoy camiones y ubicaciones.",
                           "Cuentas, rutas, recorridos, mensajes y", "avisos se guardan en tor-datos.json.",
                           "Pasarlos a estas tablas: trabajo futuro."]):
        c.append(texto(792, 680 + i * 15, t, "t-security" if i == 0 else "t-muted", 9 if i == 0 else 8, "start", "700" if i == 0 else "400"))

    # Leyenda
    c.append(texto(40, 770, "PK = llave primaria", "t-security", 9, "start", "700"))
    c.append(texto(200, 770, "FK = llave foránea", "t-cloud", 9, "start", "700"))
    c.append(texto(360, 770, "UQ = valor único", "t-frontend", 9, "start", "700"))
    c.append(texto(510, 770, "1 — N = uno a muchos", "t-backend", 9, "start", "700"))

    tarjetas = [
        tarjeta("violet", "8 tablas (MySQL 8)", ["Catálogos: colonias (83 de Huauchinango), rutas y camiones.",
                                                 "Operación: recorridos, ubicaciones y notificaciones.",
                                                 "ruta_colonias resuelve la relación N a N con orden y día."]),
        tarjeta("emerald", "Reglas de integridad", ["Correo, clave y placas únicos.",
                                                    "CHECK: latitud ±90, longitud ±180, día 1 a 7, fin ≥ inicio.",
                                                    "Índice (id_camion, fecha) para pedir rápido la última ubicación."]),
        tarjeta("amber", "Requisitos del acta que cubre", ["R1 usuarios · R2 rutas · R3/R9 ubicaciones.",
                                                          "R5 ruta y camión asignado · R6 notificaciones.",
                                                          "R7 historial de recorridos · R8 administración."]),
    ]
    pagina("base-de-datos", "Truck on Route · Base de datos",
           "b) Modelo entidad-relación de db/esquema.sql (MySQL 8 / MariaDB 10.6)",
           1100, 790, "".join(c), tarjetas,
           "Equipo G2VSIS07 · ITSH · Gestión de Proyectos de Software · esquema en db/esquema.sql · noviembre 2026")


if __name__ == "__main__":
    arquitectura()
    base_de_datos()
    print("Listo: arquitectura.html y base-de-datos.html")
