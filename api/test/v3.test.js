// Pruebas de la versión 2.8 (spec 004): sesión, permisos, altas, recorridos y avisos.
// Ejecutar con: npm test

const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { crearApp } = require('../app');
const { hashContrasena, contrasenaCorrecta } = require('../seguridad');

const archivo = path.join(fs.mkdtempSync(path.join(os.tmpdir(), 'tor-')), 'tor-datos.json');
let servidor;
let base;

function iniciar() {
  return new Promise((listo) => {
    servidor = crearApp({ archivoDatos: archivo }).listen(0, () => {
      base = `http://localhost:${servidor.address().port}`;
      listo();
    });
  });
}
test.before(iniciar);
test.after(() => servidor.close());

// Hace una petición y devuelve { status, cuerpo }
async function pedir(metodo, ruta, cuerpo, token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  const r = await fetch(`${base}${ruta}`, { method: metodo, headers, body: cuerpo ? JSON.stringify(cuerpo) : undefined });
  return { status: r.status, cuerpo: await r.json() };
}
const entrar = async (correo, contrasena) => (await pedir('POST', '/sesion', { correo, contrasena })).cuerpo.token;

test('la contraseña se guarda como hash y se puede comprobar', () => {
  const h = hashContrasena('secreta1');
  assert.ok(!h.includes('secreta1'));
  assert.ok(contrasenaCorrecta('secreta1', h));
  assert.ok(!contrasenaCorrecta('otra', h));
});

test('el archivo de datos no guarda contraseñas en texto', () => {
  const texto = fs.readFileSync(archivo, 'utf8');
  assert.ok(texto.includes('admin@truckonroute.mx'));
  assert.ok(!texto.includes('Admin2026'));
});

test('login correcto devuelve token y rol; incorrecto da 401', async () => {
  const ok = await pedir('POST', '/sesion', { correo: 'ADMIN@truckonroute.mx', contrasena: 'Admin2026' });
  assert.strictEqual(ok.status, 200);
  assert.strictEqual(ok.cuerpo.usuario.rol, 'administrador');
  assert.strictEqual(ok.cuerpo.usuario.contrasena, undefined);
  const mal = await pedir('POST', '/sesion', { correo: 'admin@truckonroute.mx', contrasena: 'nop' });
  assert.strictEqual(mal.status, 401);
});

test('registro crea un ciudadano y rechaza correos repetidos o inválidos', async () => {
  const r = await pedir('POST', '/usuarios', { nombre: 'Luis', correo: 'luis@gmail.com', contrasena: 'luis123', colonia: 'Centro', aceptaAviso: true });
  assert.strictEqual(r.status, 201);
  assert.strictEqual(r.cuerpo.usuario.rol, 'ciudadano');
  assert.strictEqual((await pedir('POST', '/usuarios', { nombre: 'Luis', correo: 'luis@gmail.com', contrasena: 'luis123', aceptaAviso: true })).status, 400);
  assert.strictEqual((await pedir('POST', '/usuarios', { nombre: 'X', correo: 'hola.com', contrasena: 'abcdef' })).status, 400);
  assert.strictEqual((await pedir('POST', '/usuarios', { nombre: 'X', correo: 'x@gmail.com', contrasena: '123' })).status, 400);
});

// v2.12 (spec 008): aviso de privacidad
test('el registro pide aceptar el aviso de privacidad y guarda la fecha', async () => {
  const sin = await pedir('POST', '/usuarios', { nombre: 'Rosa', correo: 'rosa@gmail.com', contrasena: 'rosa123' });
  assert.strictEqual(sin.status, 400);
  assert.strictEqual(sin.cuerpo.error, 'Debes aceptar el aviso de privacidad');
  const conTexto = await pedir('POST', '/usuarios', { nombre: 'Rosa', correo: 'rosa@gmail.com', contrasena: 'rosa123', aceptaAviso: 'si' });
  assert.strictEqual(conTexto.status, 400); // solo vale true
  const con = await pedir('POST', '/usuarios', { nombre: 'Rosa', correo: 'rosa@gmail.com', contrasena: 'rosa123', aceptaAviso: true });
  assert.strictEqual(con.status, 201);
  assert.ok(!Number.isNaN(Date.parse(con.cuerpo.usuario.avisoAceptado)));
});

test('GET /privacidad muestra el aviso sin pedir sesión', async () => {
  const r = await fetch(base + '/privacidad');
  assert.strictEqual(r.status, 200);
  const html = await r.text();
  assert.ok(html.includes('Aviso de privacidad'));
  assert.ok(html.includes('privacidad@truckonroute.mx'));
});

test('las rutas de administrador piden sesión y rol', async () => {
  assert.strictEqual((await pedir('GET', '/admin/camiones')).status, 401);
  const tCiudadano = await entrar('ana@gmail.com', 'Ana2026');
  assert.strictEqual((await pedir('GET', '/admin/camiones', null, tCiudadano)).status, 403);
  const tChofer = await entrar('chofer@truckonroute.mx', 'Chofer2026');
  assert.strictEqual((await pedir('GET', '/admin/camiones', null, tChofer)).status, 403);
});

test('el administrador da de alta camión, chofer, ruta, recorrido y mensaje', async () => {
  const t = await entrar('admin@truckonroute.mx', 'Admin2026');
  const camion = await pedir('POST', '/admin/camiones', { clave: 'camion-02', placas: 'PUE-456-B' }, t);
  assert.strictEqual(camion.status, 201);
  assert.strictEqual(camion.cuerpo.clave, 'CAMION-02');
  assert.strictEqual((await pedir('POST', '/admin/camiones', { clave: 'CAMION-02' }, t)).status, 400);

  const chofer = await pedir('POST', '/admin/choferes', { nombre: 'Pedro', correo: 'pedro@truckonroute.mx', contrasena: 'pedro123' }, t);
  assert.strictEqual(chofer.status, 201);
  assert.strictEqual(chofer.cuerpo.rol, 'conductor');

  const ruta = await pedir('POST', '/admin/rutas', { nombre: 'Ruta Norte', colonias: ['Bellavista', 'Agua Blanca'] }, t);
  assert.strictEqual(ruta.status, 201);

  const rec = await pedir('POST', '/admin/recorridos', { camionId: camion.cuerpo.id, choferId: chofer.cuerpo.id, rutaId: ruta.cuerpo.id, indicaciones: 'Calle 5 de Mayo cerrada' }, t);
  assert.strictEqual(rec.status, 201);
  assert.strictEqual(rec.cuerpo.estado, 'programado');
  assert.strictEqual(rec.cuerpo.camion, 'CAMION-02');
  assert.strictEqual((await pedir('POST', '/admin/recorridos', { camionId: 999, choferId: chofer.cuerpo.id, rutaId: ruta.cuerpo.id }, t)).status, 400);

  const m = await pedir('POST', '/admin/mensajes', { choferId: chofer.cuerpo.id, texto: 'Hoy empieza a las 7' }, t);
  assert.strictEqual(m.status, 201);

  const resumen = (await pedir('GET', '/admin/resumen', null, t)).cuerpo;
  assert.strictEqual(resumen.camiones, 2);
  assert.strictEqual(resumen.choferes, 2);
});

test('el chofer ve su recorrido y mensajes, inicia la ruta y se crea el aviso', async () => {
  const antes = new Date().toISOString();
  const t = await entrar('pedro@truckonroute.mx', 'pedro123');
  const recs = (await pedir('GET', '/chofer/recorridos', null, t)).cuerpo;
  assert.strictEqual(recs.length, 1);
  assert.strictEqual(recs[0].indicaciones, 'Calle 5 de Mayo cerrada');
  assert.deepStrictEqual(recs[0].colonias, ['Bellavista', 'Agua Blanca']);

  const mensajes = (await pedir('GET', '/chofer/mensajes', null, t)).cuerpo;
  assert.strictEqual(mensajes[0].texto, 'Hoy empieza a las 7');

  const iniciado = await pedir('POST', `/chofer/recorridos/${recs[0].id}/iniciar`, null, t);
  assert.strictEqual(iniciado.cuerpo.estado, 'en_curso');

  const avisos = (await pedir('GET', `/avisos?desde=${encodeURIComponent(antes)}`)).cuerpo;
  assert.strictEqual(avisos.length, 1);
  assert.strictEqual(avisos[0].tipo, 'en_ruta');
  assert.match(avisos[0].mensaje, /Ruta Norte \(CAMION-02\)/);

  const enCurso = (await pedir('GET', '/recorridos/en-curso')).cuerpo;
  assert.strictEqual(enCurso.length, 1);
  assert.strictEqual(enCurso[0].indicaciones, undefined); // el ciudadano no ve datos internos

  // Otro chofer no puede tocar ese recorrido
  const otro = await entrar('chofer@truckonroute.mx', 'Chofer2026');
  assert.strictEqual((await pedir('POST', `/chofer/recorridos/${recs[0].id}/terminar`, null, otro)).status, 404);

  const fin = await pedir('POST', `/chofer/recorridos/${recs[0].id}/terminar`, null, t);
  assert.strictEqual(fin.cuerpo.estado, 'terminado');
  assert.strictEqual((await pedir('POST', `/chofer/recorridos/${recs[0].id}/iniciar`, null, t)).status, 400);
});

test('cerrar sesión invalida el token', async () => {
  const t = await entrar('ana@gmail.com', 'Ana2026');
  assert.strictEqual((await pedir('GET', '/sesion', null, t)).status, 200);
  await pedir('DELETE', '/sesion', null, t);
  assert.strictEqual((await pedir('GET', '/sesion', null, t)).status, 401);
});

test('los datos siguen después de reiniciar la API', async () => {
  servidor.close();
  await iniciar();
  const t = await entrar('pedro@truckonroute.mx', 'pedro123');
  assert.ok(t, 'el chofer creado antes del reinicio puede entrar');
  const ta = await entrar('admin@truckonroute.mx', 'Admin2026');
  assert.strictEqual((await pedir('GET', '/admin/camiones', null, ta)).cuerpo.length, 2);
});

// ---------------------------------------------------------------------------
// Versión 2.13 (spec 009): rutas dibujadas, gris de lo recorrido y desvío
// ---------------------------------------------------------------------------
const { densificar, avanceEnTrazo } = require('../geo');

test('el avance solo cuenta puntos cercanos y nunca regresa', () => {
  const trazo = densificar([[20.17, -98.05], [20.17, -98.048]]); // ~210 m
  assert.ok(trazo.length >= 11);
  const mitad = trazo[Math.floor(trazo.length / 2)];
  const a = avanceEnTrazo(trazo, [{ lat: trazo[0][0], lng: trazo[0][1] }, { lat: mitad[0], lng: mitad[1] }]);
  assert.ok(a > 1 && a < trazo.length);
  // si el GPS brinca hacia el inicio, el avance se queda igual
  const b = avanceEnTrazo(trazo, [{ lat: trazo[0][0], lng: trazo[0][1] }, { lat: mitad[0], lng: mitad[1] }, { lat: trazo[0][0], lng: trazo[0][1] }]);
  assert.strictEqual(b, a);
  // una ubicación lejos no cuenta
  assert.strictEqual(avanceEnTrazo(trazo, [{ lat: 20.2, lng: -98.1 }]), 0);
});

test('el administrador dibuja el camino, se pinta el avance y pone un desvío que avisa a todos', async () => {
  const t = await entrar('admin@truckonroute.mx', 'Admin2026');
  const ruta = (await pedir('POST', '/admin/rutas', { nombre: 'Ruta Trazo', colonias: ['Centro'] }, t)).cuerpo;
  const camino = [[20.17, -98.05], [20.17, -98.048]];

  // FR-903: solo administrador y con al menos 2 puntos válidos
  assert.strictEqual((await pedir('PUT', `/admin/rutas/${ruta.id}/trazo`, { trazo: camino })).status, 401);
  assert.strictEqual((await pedir('PUT', `/admin/rutas/${ruta.id}/trazo`, { trazo: [[20.17, -98.05]] }, t)).status, 400);
  assert.strictEqual((await pedir('PUT', `/admin/rutas/${ruta.id}/trazo`, { trazo: [[200, 0], [0, 0]] }, t)).status, 400);
  assert.strictEqual((await pedir('PUT', `/admin/rutas/999/trazo`, { trazo: camino }, t)).status, 404);
  const ok = await pedir('PUT', `/admin/rutas/${ruta.id}/trazo`, { trazo: camino }, t);
  assert.strictEqual(ok.status, 200);
  assert.deepStrictEqual(ok.cuerpo.trazo, camino);

  // Recorrido con un camión nuevo para que su historial esté limpio
  const camion = (await pedir('POST', '/admin/camiones', { clave: 'TRAZO-01' }, t)).cuerpo;
  const chofer = (await pedir('POST', '/admin/choferes', { nombre: 'Rosa', correo: 'rosa@truckonroute.mx', contrasena: 'rosa123' }, t)).cuerpo;
  const rec = (await pedir('POST', '/admin/recorridos', { camionId: camion.id, choferId: chofer.id, rutaId: ruta.id }, t)).cuerpo;

  // No se puede desviar un recorrido que no ha empezado
  assert.strictEqual((await pedir('PUT', `/admin/recorridos/${rec.id}/desvio`, { trazo: camino, motivo: 'Obra' }, t)).status, 400);

  const tc = await entrar('rosa@truckonroute.mx', 'rosa123');
  await pedir('POST', `/chofer/recorridos/${rec.id}/iniciar`, null, tc);

  // FR-904: sin ubicaciones el avance es 0
  let mio = (await pedir('GET', '/recorridos/en-curso')).cuerpo.find((x) => x.id === rec.id);
  assert.ok(mio.trazo.length >= 11);
  assert.strictEqual(mio.avance, 0);
  assert.strictEqual(mio.desvio, null);

  // El camión pasa por la mitad del camino
  await pedir('POST', '/ubicacion', { camion: 'TRAZO-01', lat: 20.17, lng: -98.05, fuente: 'simulacion' });
  await pedir('POST', '/ubicacion', { camion: 'TRAZO-01', lat: 20.17, lng: -98.049, fuente: 'simulacion' });
  mio = (await pedir('GET', '/recorridos/en-curso')).cuerpo.find((x) => x.id === rec.id);
  assert.ok(mio.avance > 1 && mio.avance < mio.trazo.length);

  // FR-908 y FR-912: desvío con motivo; mensaje al chofer y aviso a los ciudadanos
  const antes = new Date().toISOString();
  assert.strictEqual((await pedir('PUT', `/admin/recorridos/${rec.id}/desvio`, { trazo: camino }, t)).status, 400);
  const desvio = [[20.17, -98.049], [20.171, -98.049], [20.171, -98.048]];
  const d = await pedir('PUT', `/admin/recorridos/${rec.id}/desvio`, { trazo: desvio, motivo: 'Calle Hidalgo cerrada por obra' }, t);
  assert.strictEqual(d.status, 200);
  mio = (await pedir('GET', '/recorridos/en-curso')).cuerpo.find((x) => x.id === rec.id);
  assert.deepStrictEqual(mio.desvio, desvio);
  assert.strictEqual(mio.motivoDesvio, 'Calle Hidalgo cerrada por obra');

  const avisos = (await pedir('GET', `/avisos?desde=${encodeURIComponent(antes)}`)).cuerpo;
  const aviso = avisos.find((a) => a.tipo === 'desvio');
  assert.ok(aviso);
  assert.strictEqual(aviso.titulo, 'El camión cambió su ruta');
  assert.match(aviso.mensaje, /Calle Hidalgo cerrada por obra/);
  const mensajes = (await pedir('GET', '/chofer/mensajes', null, tc)).cuerpo;
  assert.ok(mensajes.some((m) => /Ruta cambiada por bloqueo/.test(m.texto)));

  // FR-910: al terminar se quita el desvío
  await pedir('POST', `/chofer/recorridos/${rec.id}/terminar`, null, tc);
  const lista = (await pedir('GET', '/admin/recorridos', null, t)).cuerpo.find((x) => x.id === rec.id);
  assert.strictEqual(lista.desvio, undefined);
});

// ---------------------------------------------------------------------------
// Versión 2.14 (spec 010): avisos por destinatario y chat chofer-administrador
// ---------------------------------------------------------------------------
// Archivos mínimos con los primeros bytes de cada formato
const JPG = Buffer.from([0xff, 0xd8, 0xff, 0xe0, 1, 2, 3, 4]).toString('base64');
const M4A = Buffer.concat([Buffer.from([0, 0, 0, 0x18]), Buffer.from('ftypM4A '), Buffer.alloc(8)]).toString('base64');

test('el administrador manda avisos a ciudadanos o choferes y cada quien ve solo los suyos', async () => {
  const t = await entrar('admin@truckonroute.mx', 'Admin2026');
  const tc = await entrar('chofer@truckonroute.mx', 'Chofer2026');
  const antes = new Date().toISOString();
  assert.strictEqual((await pedir('POST', '/admin/avisos', { tipo: 'retraso', para: 'ciudadanos', mensaje: 'x' })).status, 401);
  assert.strictEqual((await pedir('POST', '/admin/avisos', { tipo: 'otro', para: 'todos', mensaje: 'x' }, t)).status, 400);
  assert.strictEqual((await pedir('POST', '/admin/avisos', { tipo: 'general', para: 'vecinos', mensaje: 'x' }, t)).status, 400);
  assert.strictEqual((await pedir('POST', '/admin/avisos', { tipo: 'general', para: 'todos', mensaje: '  ' }, t)).status, 400);

  const ruta = (await pedir('GET', '/admin/rutas', null, t)).cuerpo[0];
  const a1 = await pedir('POST', '/admin/avisos', { tipo: 'retraso', para: 'ciudadanos', rutaId: ruta.id, mensaje: 'Pasa 30 min tarde' }, t);
  assert.strictEqual(a1.status, 201);
  assert.strictEqual(a1.cuerpo.titulo, 'El camión va retrasado');
  assert.strictEqual(a1.cuerpo.mensaje, `${ruta.nombre}: Pasa 30 min tarde`);
  await pedir('POST', '/admin/avisos', { tipo: 'general', para: 'choferes', mensaje: 'Junta a las 6' }, t);
  await pedir('POST', '/admin/avisos', { tipo: 'no_pasa', para: 'todos', mensaje: 'Día festivo' }, t);

  const q = `?desde=${encodeURIComponent(antes)}`;
  const ciudadano = (await pedir('GET', '/avisos' + q)).cuerpo.map((a) => a.tipo);
  assert.deepStrictEqual(ciudadano.sort(), ['no_pasa', 'retraso']);
  const delChofer = (await pedir('GET', '/chofer/avisos' + q, null, tc)).cuerpo.map((a) => a.mensaje);
  assert.deepStrictEqual(delChofer.sort(), ['Día festivo', 'Junta a las 6']);
  // Historial de 7 días
  assert.ok((await pedir('GET', '/avisos?dias=7')).cuerpo.length >= 2);
});

test('chat: el chofer manda rápido, texto, foto y audio; el admin ve no leídos y contesta', async () => {
  const t = await entrar('admin@truckonroute.mx', 'Admin2026');
  const tc = await entrar('chofer@truckonroute.mx', 'Chofer2026');
  const yo = (await pedir('GET', '/sesion', null, tc)).cuerpo.usuario;

  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'rapido', texto: 'Falla mecánica' }, tc)).status, 201);
  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'rapido', texto: 'Vámonos' }, tc)).status, 400);
  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'texto', texto: '' }, tc)).status, 400);
  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'texto', texto: 'Bajo por unos tacos' }, tc)).status, 201);
  const foto = await pedir('POST', '/chofer/chat', { tipo: 'foto', archivo: JPG }, tc);
  assert.strictEqual(foto.status, 201);
  assert.match(foto.cuerpo.archivo, /\.jpg$/);
  // Un audio que dice ser foto se rechaza (se revisan los primeros bytes)
  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'foto', archivo: M4A }, tc)).status, 400);
  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'audio', archivo: M4A }, tc)).status, 201);
  // Más de 5 MB
  const grande = Buffer.concat([Buffer.from([0xff, 0xd8, 0xff]), Buffer.alloc(5 * 1024 * 1024)]).toString('base64');
  assert.strictEqual((await pedir('POST', '/chofer/chat', { tipo: 'foto', archivo: grande }, tc)).status, 400);

  // El ciudadano no entra al chat
  const tci = await entrar('ana@gmail.com', 'Ana2026');
  assert.strictEqual((await pedir('GET', '/chofer/chat', null, tci)).status, 403);
  assert.strictEqual((await pedir('GET', '/admin/chat', null, tci)).status, 403);

  // FR-1011: el admin ve 4 sin leer de ese chofer; al abrir quedan leídos
  let lista = (await pedir('GET', '/admin/chat', null, t)).cuerpo;
  assert.strictEqual(lista.choferes.find((c) => c.id === yo.id).noLeidos, 4);
  assert.ok(lista.total >= 4);
  const conv = (await pedir('GET', `/admin/chat/${yo.id}`, null, t)).cuerpo;
  assert.ok(conv.some((m) => m.tipo === 'audio'));
  lista = (await pedir('GET', '/admin/chat', null, t)).cuerpo;
  assert.strictEqual(lista.choferes.find((c) => c.id === yo.id).noLeidos, 0);

  // El admin contesta con texto y foto; el chofer los ve como no leídos
  assert.strictEqual((await pedir('POST', `/admin/chat/${yo.id}`, { tipo: 'texto', texto: 'Va, ya mandé al mecánico' }, t)).status, 201);
  assert.strictEqual((await pedir('POST', `/admin/chat/${yo.id}`, { tipo: 'foto', archivo: JPG }, t)).status, 201);
  assert.strictEqual((await pedir('POST', `/admin/chat/${yo.id}`, { tipo: 'rapido', texto: 'Tráfico' }, t)).status, 400);
  assert.strictEqual((await pedir('GET', '/chofer/chat/no-leidos', null, tc)).cuerpo.total >= 2, true);
  const delChofer = (await pedir('GET', '/chofer/chat', null, tc)).cuerpo;
  assert.ok(delChofer.some((m) => m.texto === 'Hoy empieza a las 7' || m.texto === 'Va, ya mandé al mecánico'));
  assert.strictEqual((await pedir('GET', '/chofer/chat/no-leidos', null, tc)).cuerpo.total, 0);

  // FR-1014: el archivo lo bajan el chofer y el admin (también con ?token=), nadie más
  const url = `${base}/archivos/${foto.cuerpo.archivo}`;
  assert.strictEqual((await fetch(url)).status, 401);
  assert.strictEqual((await fetch(url, { headers: { Authorization: `Bearer ${tc}` } })).status, 200);
  assert.strictEqual((await fetch(`${url}?token=${t}`)).status, 200);
  const otro = await entrar('pedro@truckonroute.mx', 'pedro123');
  assert.strictEqual((await fetch(url, { headers: { Authorization: `Bearer ${otro}` } })).status, 403);
  assert.strictEqual((await fetch(`${base}/archivos/..%2Ftor-datos.json?token=${t}`)).status, 404);
});
