// Datos de la Fase 3 (spec 004): usuarios, camiones, rutas, recorridos, mensajes y avisos.
// Viven en memoria y se copian a un archivo JSON en cada cambio (FR-411),
// así no se pierden al reiniciar la API. El archivo no se sube a git.

const fs = require('node:fs');
const path = require('node:path');
const { hashContrasena } = require('./seguridad');

const ARCHIVO_POR_DEFECTO = path.join(__dirname, 'datos', 'tor-datos.json');

function vacio() {
  return { siguienteId: 1, usuarios: [], camiones: [], rutas: [], recorridos: [], mensajes: [], avisos: [] };
}

function crearDatos(archivo = ARCHIVO_POR_DEFECTO) {
  let d = vacio();

  // Lee el archivo si existe; si no existe o está dañado, arranca con datos de ejemplo
  if (archivo && fs.existsSync(archivo)) {
    try {
      d = { ...vacio(), ...JSON.parse(fs.readFileSync(archivo, 'utf8')) };
    } catch (err) {
      console.error(`No se pudo leer ${archivo} (${err.message}). Se usan los datos de ejemplo.`);
      d = vacio();
    }
  }

  function guardar() {
    if (!archivo) return; // en pruebas se puede trabajar sin archivo
    fs.mkdirSync(path.dirname(archivo), { recursive: true });
    fs.writeFileSync(archivo, JSON.stringify(d, null, 2));
  }

  function nuevoId() {
    return d.siguienteId++;
  }

  // ---------- Usuarios ----------
  // Lo que se manda a la app o al panel: nunca la contraseña
  function publico(u) {
    if (!u) return null;
    const { contrasena, ...resto } = u;
    return resto;
  }

  function buscarPorCorreo(correo) {
    const c = String(correo || '').trim().toLowerCase();
    return d.usuarios.find((u) => u.correo === c) || null;
  }

  function buscarUsuario(id) {
    return d.usuarios.find((u) => u.id === Number(id)) || null;
  }

  // v2.12 (spec 008, FR-806): avisoAceptado = fecha en que el ciudadano aceptó el aviso de privacidad
  function crearUsuario({ nombre, correo, telefono = '', colonia = '', contrasena, rol = 'ciudadano', avisoAceptado = null }) {
    const u = {
      id: nuevoId(),
      nombre: String(nombre).trim(),
      correo: String(correo).trim().toLowerCase(),
      telefono: String(telefono || '').trim(),
      colonia: String(colonia || '').trim(),
      rol,
      contrasena: hashContrasena(contrasena),
      creado: new Date().toISOString(),
    };
    if (avisoAceptado) u.avisoAceptado = avisoAceptado;
    d.usuarios.push(u);
    guardar();
    return u;
  }

  function borrarUsuario(id) {
    const antes = d.usuarios.length;
    d.usuarios = d.usuarios.filter((u) => u.id !== Number(id));
    guardar();
    return d.usuarios.length < antes;
  }

  // ---------- Camiones ----------
  function buscarCamion(id) {
    return d.camiones.find((c) => c.id === Number(id)) || null;
  }

  function crearCamion({ clave, placas = '', modelo = '' }) {
    const c = { id: nuevoId(), clave: String(clave).trim().toUpperCase(), placas: String(placas || '').trim(), modelo: String(modelo || '').trim() };
    d.camiones.push(c);
    guardar();
    return c;
  }

  function borrarCamion(id) {
    const antes = d.camiones.length;
    d.camiones = d.camiones.filter((c) => c.id !== Number(id));
    guardar();
    return d.camiones.length < antes;
  }

  // ---------- Rutas ----------
  function buscarRuta(id) {
    return d.rutas.find((r) => r.id === Number(id)) || null;
  }

  function crearRuta({ nombre, colonias = [], horario = '' }) {
    const r = { id: nuevoId(), nombre: String(nombre).trim(), colonias: colonias.map((c) => String(c).trim()).filter(Boolean), horario: String(horario || '').trim() };
    d.rutas.push(r);
    guardar();
    return r;
  }

  // ---------- Recorridos ----------
  function buscarRecorrido(id) {
    return d.recorridos.find((r) => r.id === Number(id)) || null;
  }

  function crearRecorrido({ camionId, choferId, rutaId, fecha, indicaciones = '' }) {
    const r = {
      id: nuevoId(), camionId: Number(camionId), choferId: Number(choferId), rutaId: Number(rutaId),
      fecha: fecha || new Date().toISOString(), indicaciones: String(indicaciones || '').trim(),
      estado: 'programado', inicio: null, fin: null,
    };
    d.recorridos.push(r);
    guardar();
    return r;
  }

  function cambiarEstado(recorrido, estado) {
    recorrido.estado = estado;
    if (estado === 'en_curso') recorrido.inicio = new Date().toISOString();
    if (estado === 'terminado') recorrido.fin = new Date().toISOString();
    guardar();
    return recorrido;
  }

  // Recorrido con los nombres del camión, chofer y ruta, para mostrarlo sin hacer más consultas
  function detalle(r) {
    const camion = buscarCamion(r.camionId);
    const chofer = buscarUsuario(r.choferId);
    const ruta = buscarRuta(r.rutaId);
    return { ...r, camion: camion?.clave || '—', chofer: chofer?.nombre || '—', ruta: ruta?.nombre || '—', colonias: ruta?.colonias || [], horario: ruta?.horario || '' };
  }

  // ---------- Mensajes y avisos ----------
  function crearMensaje({ deId, paraId, texto }) {
    const m = { id: nuevoId(), deId: Number(deId), paraId: Number(paraId), texto: String(texto).trim(), fecha: new Date().toISOString() };
    d.mensajes.push(m);
    guardar();
    return m;
  }

  function crearAviso(aviso) {
    const a = { id: nuevoId(), fecha: new Date().toISOString(), ...aviso };
    d.avisos.push(a);
    if (d.avisos.length > 200) d.avisos.shift(); // solo los últimos 200
    guardar();
    return a;
  }

  // ---------- Datos de ejemplo ----------
  // Solo la primera vez (cuando no hay usuarios). La cuenta del administrador sale de .env.
  function sembrar() {
    if (d.usuarios.length > 0) return false;
    crearUsuario({ nombre: 'Administrador', correo: process.env.ADMIN_CORREO || 'admin@truckonroute.mx', contrasena: process.env.ADMIN_CONTRASENA || 'Admin2026', rol: 'administrador' });
    const chofer = crearUsuario({ nombre: 'Juan Pérez (chofer de ejemplo)', correo: 'chofer@truckonroute.mx', telefono: '7761234567', contrasena: 'Chofer2026', rol: 'conductor' });
    crearUsuario({ nombre: 'Ana López (ciudadana de ejemplo)', correo: 'ana@gmail.com', telefono: '7769876543', colonia: 'Centro', contrasena: 'Ana2026', rol: 'ciudadano' });
    const camion = crearCamion({ clave: 'CAMION-01', placas: 'PUE-123-A', modelo: 'Recolector 2018' });
    const ruta = crearRuta({ nombre: 'Ruta Centro', colonias: ['Centro', 'Zacamila', 'Cuauhtémoc', 'Los Ángeles'], horario: 'Lunes, miércoles y viernes 7:00 a 11:00' });
    crearRecorrido({ camionId: camion.id, choferId: chofer.id, rutaId: ruta.id, indicaciones: 'Empieza por el mercado. En Zacamila recoge también los contenedores de la escuela.' });
    return true;
  }

  return {
    d, guardar, publico, buscarPorCorreo, buscarUsuario, crearUsuario, borrarUsuario,
    buscarCamion, crearCamion, borrarCamion, buscarRuta, crearRuta,
    buscarRecorrido, crearRecorrido, cambiarEstado, detalle, crearMensaje, crearAviso, sembrar,
  };
}

module.exports = { crearDatos };
