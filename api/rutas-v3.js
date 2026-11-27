// Rutas de la versión 2.8 (spec 004): sesión, administrador, chofer y avisos.
// Contrato: specs/004-roles-recorridos-avisos/contracts/api-v3.md

const express = require('express');
const { contrasenaCorrecta, nuevoToken } = require('./seguridad');
const { trazoValido, densificar, avanceEnTrazo } = require('./geo');

const DURACION_SESION_MS = 7 * 24 * 60 * 60 * 1000; // 7 días
// Misma regla que la app (data/ValidarCorreo.kt, spec 003)
const CORREO = /^[A-Za-z0-9_%+-]+(\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\.[A-Za-z]{2,}$/;

const texto = (v) => (typeof v === 'string' ? v.trim() : '');

function crearRutasV3(datos, { historialDe = () => [] } = {}) {
  const r = express.Router();
  const sesiones = new Map(); // token -> { usuarioId, vence }

  // ---------- Ayudantes ----------
  function revisarCuenta({ nombre, correo, contrasena }) {
    if (!texto(nombre)) return 'Falta el nombre';
    if (!CORREO.test(texto(correo)) || texto(correo).length > 254) return 'Escribe un correo válido, por ejemplo nombre@gmail.com';
    if (typeof contrasena !== 'string' || contrasena.length < 6) return 'La contraseña debe tener al menos 6 caracteres';
    if (datos.buscarPorCorreo(correo)) return 'Ese correo ya está registrado';
    return null;
  }

  // Revisa el token y, si se pide, el rol (FR-404)
  function exigir(...roles) {
    return (req, res, next) => {
      const token = (req.get('Authorization') || '').replace(/^Bearer\s+/i, '');
      const s = sesiones.get(token);
      if (!s || s.vence < Date.now()) {
        sesiones.delete(token);
        return res.status(401).json({ error: 'Inicia sesión otra vez' });
      }
      const usuario = datos.buscarUsuario(s.usuarioId);
      if (!usuario) return res.status(401).json({ error: 'Inicia sesión otra vez' });
      if (roles.length && !roles.includes(usuario.rol)) return res.status(403).json({ error: 'No tienes permiso' });
      req.usuario = usuario;
      req.token = token;
      next();
    };
  }

  // ---------- Sesión y registro ----------
  // Registro de ciudadanos desde la app (FR-403)
  r.post('/usuarios', (req, res) => {
    const c = req.body || {};
    const error = revisarCuenta(c);
    if (error) return res.status(400).json({ error });
    // v2.12 (spec 008, FR-806): sin aceptar el aviso de privacidad no se crea la cuenta
    if (c.aceptaAviso !== true) return res.status(400).json({ error: 'Debes aceptar el aviso de privacidad' });
    const u = datos.crearUsuario({ ...c, rol: 'ciudadano', avisoAceptado: new Date().toISOString() });
    res.status(201).json({ usuario: datos.publico(u) });
  });

  // Inicio de sesión (FR-401)
  r.post('/sesion', (req, res) => {
    const { correo, contrasena } = req.body || {};
    const u = datos.buscarPorCorreo(correo);
    if (!u || !contrasenaCorrecta(contrasena, u.contrasena)) {
      return res.status(401).json({ error: 'Correo o contraseña incorrectos' });
    }
    const token = nuevoToken();
    sesiones.set(token, { usuarioId: u.id, vence: Date.now() + DURACION_SESION_MS });
    console.log(`Inicio de sesión: ${u.correo} (${u.rol})`);
    res.json({ token, usuario: datos.publico(u) });
  });

  r.get('/sesion', exigir(), (req, res) => res.json({ usuario: datos.publico(req.usuario) }));

  r.delete('/sesion', exigir(), (req, res) => {
    sesiones.delete(req.token);
    res.json({ ok: true });
  });

  // ---------- Administrador (FR-405, FR-406) ----------
  const admin = exigir('administrador');
  const hoy = (iso) => new Date(iso).toDateString() === new Date().toDateString();

  r.get('/admin/resumen', admin, (req, res) => {
    const { usuarios, camiones, recorridos } = datos.d;
    res.json({
      camiones: camiones.length,
      choferes: usuarios.filter((u) => u.rol === 'conductor').length,
      ciudadanos: usuarios.filter((u) => u.rol === 'ciudadano').length,
      recorridosHoy: recorridos.filter((x) => hoy(x.fecha)).length,
      enRuta: recorridos.filter((x) => x.estado === 'en_curso').length,
    });
  });

  r.get('/admin/camiones', admin, (req, res) => res.json(datos.d.camiones));
  r.post('/admin/camiones', admin, (req, res) => {
    const clave = texto(req.body?.clave).toUpperCase();
    if (!clave || clave.length > 20) return res.status(400).json({ error: 'La clave del camión debe tener de 1 a 20 caracteres, por ejemplo CAMION-02' });
    if (datos.d.camiones.some((c) => c.clave === clave)) return res.status(400).json({ error: 'Ya existe un camión con esa clave' });
    res.status(201).json(datos.crearCamion({ ...req.body, clave }));
  });
  r.delete('/admin/camiones/:id', admin, (req, res) => {
    if (datos.d.recorridos.some((x) => x.camionId === Number(req.params.id) && x.estado === 'en_curso')) {
      return res.status(400).json({ error: 'Ese camión está en ruta; termina el recorrido primero' });
    }
    if (!datos.borrarCamion(req.params.id)) return res.status(404).json({ error: 'No existe ese camión' });
    res.json({ ok: true });
  });

  r.get('/admin/choferes', admin, (req, res) => res.json(datos.d.usuarios.filter((u) => u.rol === 'conductor').map(datos.publico)));
  r.post('/admin/choferes', admin, (req, res) => {
    const error = revisarCuenta(req.body || {});
    if (error) return res.status(400).json({ error });
    const u = datos.crearUsuario({ ...req.body, colonia: '', rol: 'conductor' });
    res.status(201).json(datos.publico(u));
  });

  r.get('/admin/usuarios', admin, (req, res) => res.json(datos.d.usuarios.map(datos.publico)));
  r.delete('/admin/usuarios/:id', admin, (req, res) => {
    if (Number(req.params.id) === req.usuario.id) return res.status(400).json({ error: 'No puedes borrar tu propia cuenta' });
    if (!datos.borrarUsuario(req.params.id)) return res.status(404).json({ error: 'No existe ese usuario' });
    res.json({ ok: true });
  });

  r.get('/admin/rutas', admin, (req, res) => res.json(datos.d.rutas));
  r.post('/admin/rutas', admin, (req, res) => {
    const nombre = texto(req.body?.nombre);
    const colonias = Array.isArray(req.body?.colonias) ? req.body.colonias : [];
    if (!nombre) return res.status(400).json({ error: 'Falta el nombre de la ruta' });
    if (colonias.length === 0) return res.status(400).json({ error: 'Agrega al menos una colonia a la ruta' });
    res.status(201).json(datos.crearRuta({ nombre, colonias, horario: req.body.horario }));
  });

  // v2.13 (spec 009, FR-903): el administrador guarda el camino dibujado de una ruta
  r.put('/admin/rutas/:id/trazo', admin, (req, res) => {
    const ruta = datos.buscarRuta(req.params.id);
    if (!ruta) return res.status(404).json({ error: 'No existe esa ruta' });
    const trazo = req.body?.trazo;
    if (!trazoValido(trazo)) return res.status(400).json({ error: 'Dibuja al menos 2 puntos en el mapa' });
    res.json(datos.guardarTrazo(ruta, trazo));
  });

  // v2.13 (FR-908): desvío por bloqueo en un recorrido en curso; avisa al chofer y a los ciudadanos
  r.put('/admin/recorridos/:id/desvio', admin, (req, res) => {
    const rec = datos.buscarRecorrido(req.params.id);
    if (!rec) return res.status(404).json({ error: 'No existe ese recorrido' });
    if (rec.estado !== 'en_curso') return res.status(400).json({ error: 'Solo se puede desviar un recorrido en curso' });
    const trazo = req.body?.trazo;
    const motivo = texto(req.body?.motivo);
    if (!trazoValido(trazo)) return res.status(400).json({ error: 'Dibuja al menos 2 puntos en el mapa' });
    if (!motivo) return res.status(400).json({ error: 'Escribe el motivo del desvío' });
    datos.guardarDesvio(rec, trazo, motivo);
    const det = datos.detalle(rec);
    datos.crearMensaje({ deId: req.usuario.id, paraId: rec.choferId, texto: `Ruta cambiada por bloqueo: ${motivo}. Sigue la línea amarilla del mapa.` });
    // FR-912: los ciudadanos se enteran del cambio
    datos.crearAviso({
      tipo: 'desvio', recorridoId: rec.id, camion: det.camion, ruta: det.ruta, colonias: det.colonias,
      titulo: 'El camión cambió su ruta',
      mensaje: `${det.ruta} (${det.camion}): ${motivo}`,
    });
    res.json(det);
  });

  r.delete('/admin/recorridos/:id/desvio', admin, (req, res) => {
    const rec = datos.buscarRecorrido(req.params.id);
    if (!rec) return res.status(404).json({ error: 'No existe ese recorrido' });
    datos.guardarDesvio(rec, null);
    res.json(datos.detalle(rec));
  });

  r.get('/admin/recorridos', admin, (req, res) => res.json([...datos.d.recorridos].reverse().map(datos.detalle)));
  r.post('/admin/recorridos', admin, (req, res) => {
    const { camionId, choferId, rutaId } = req.body || {};
    if (!datos.buscarCamion(camionId)) return res.status(400).json({ error: 'Elige un camión que exista' });
    const chofer = datos.buscarUsuario(choferId);
    if (!chofer || chofer.rol !== 'conductor') return res.status(400).json({ error: 'Elige un chofer que exista' });
    if (!datos.buscarRuta(rutaId)) return res.status(400).json({ error: 'Elige una ruta que exista' });
    const fecha = req.body.fecha && !Number.isNaN(Date.parse(req.body.fecha)) ? new Date(req.body.fecha).toISOString() : undefined;
    res.status(201).json(datos.detalle(datos.crearRecorrido({ ...req.body, fecha })));
  });

  r.get('/admin/mensajes', admin, (req, res) => {
    res.json([...datos.d.mensajes].reverse().map((m) => ({ ...m, para: datos.buscarUsuario(m.paraId)?.nombre || '—' })));
  });
  r.post('/admin/mensajes', admin, (req, res) => {
    const chofer = datos.buscarUsuario(req.body?.choferId);
    if (!chofer || chofer.rol !== 'conductor') return res.status(400).json({ error: 'Elige un chofer que exista' });
    if (!texto(req.body?.texto)) return res.status(400).json({ error: 'Escribe el mensaje' });
    res.status(201).json(datos.crearMensaje({ deId: req.usuario.id, paraId: chofer.id, texto: req.body.texto }));
  });

  // ---------- Chofer (FR-407) ----------
  const chofer = exigir('conductor');

  // Sus recorridos que no han terminado, más los de hoy aunque ya terminaran
  r.get('/chofer/recorridos', chofer, (req, res) => {
    const mios = datos.d.recorridos.filter((x) => x.choferId === req.usuario.id && (x.estado !== 'terminado' || hoy(x.fecha)));
    res.json(mios.map(datos.detalle));
  });

  function recorridoDelChofer(req, res) {
    const rec = datos.buscarRecorrido(req.params.id);
    if (!rec || rec.choferId !== req.usuario.id) {
      res.status(404).json({ error: 'No tienes ese recorrido' });
      return null;
    }
    return rec;
  }

  r.post('/chofer/recorridos/:id/iniciar', chofer, (req, res) => {
    const rec = recorridoDelChofer(req, res);
    if (!rec) return;
    if (rec.estado === 'terminado') return res.status(400).json({ error: 'El recorrido ya terminó' });
    if (rec.estado !== 'en_curso') {
      datos.cambiarEstado(rec, 'en_curso');
      const det = datos.detalle(rec);
      // Aviso para todos los ciudadanos (FR-407, FR-409)
      datos.crearAviso({
        tipo: 'en_ruta', recorridoId: rec.id, camion: det.camion, ruta: det.ruta, colonias: det.colonias,
        titulo: 'El camión ya está en ruta',
        mensaje: `${det.ruta} (${det.camion}) pasa por: ${det.colonias.join(', ')}`,
      });
      console.log(`Recorrido ${rec.id} en ruta: ${det.ruta} con ${det.camion}`);
    }
    res.json(datos.detalle(rec));
  });

  r.post('/chofer/recorridos/:id/terminar', chofer, (req, res) => {
    const rec = recorridoDelChofer(req, res);
    if (!rec) return;
    if (rec.estado !== 'terminado') datos.cambiarEstado(rec, 'terminado');
    res.json(datos.detalle(rec));
  });

  r.get('/chofer/mensajes', chofer, (req, res) => {
    res.json(datos.d.mensajes.filter((m) => m.paraId === req.usuario.id).reverse());
  });

  // ---------- Ciudadano (sin sesión) ----------
  // Avisos después de una fecha; sin fecha, los de las últimas 12 horas
  r.get('/avisos', (req, res) => {
    const desde = Date.parse(req.query.desde) || Date.now() - 12 * 60 * 60 * 1000;
    res.json(datos.d.avisos.filter((a) => Date.parse(a.fecha) > desde));
  });

  // Recorridos en ruta ahora, sin datos internos (ni chofer ni indicaciones)
  r.get('/recorridos/en-curso', (req, res) => {
    res.json(datos.d.recorridos.filter((x) => x.estado === 'en_curso').map((x) => {
      const det = datos.detalle(x);
      // v2.13 (FR-904): camino de la ruta (con puntos cada 20 m) y cuántos puntos ya recorrió
      const trazo = det.trazo.length >= 2 ? densificar(det.trazo) : [];
      const desde = Date.parse(det.inicio) || 0;
      const ubicaciones = historialDe(det.camion).filter((u) => Date.parse(u.fecha) >= desde);
      return {
        id: det.id, camion: det.camion, ruta: det.ruta, colonias: det.colonias, inicio: det.inicio,
        trazo, avance: trazo.length ? avanceEnTrazo(trazo, ubicaciones) : 0,
        desvio: det.desvio || null, motivoDesvio: det.motivoDesvio || null,
      };
    }));
  });

  return r;
}

module.exports = { crearRutasV3 };
