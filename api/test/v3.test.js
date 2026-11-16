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
  const r = await pedir('POST', '/usuarios', { nombre: 'Luis', correo: 'luis@gmail.com', contrasena: 'luis123', colonia: 'Centro' });
  assert.strictEqual(r.status, 201);
  assert.strictEqual(r.cuerpo.usuario.rol, 'ciudadano');
  assert.strictEqual((await pedir('POST', '/usuarios', { nombre: 'Luis', correo: 'luis@gmail.com', contrasena: 'luis123' })).status, 400);
  assert.strictEqual((await pedir('POST', '/usuarios', { nombre: 'X', correo: 'hola.com', contrasena: 'abcdef' })).status, 400);
  assert.strictEqual((await pedir('POST', '/usuarios', { nombre: 'X', correo: 'x@gmail.com', contrasena: '123' })).status, 400);
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
