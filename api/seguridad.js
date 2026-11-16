// Contraseñas y sesiones (spec 004, FR-402).
// Se usa crypto, que ya viene con Node: no hace falta instalar nada.

const crypto = require('node:crypto');

// Convierte una contraseña en "sal:hash". La contraseña original nunca se guarda.
function hashContrasena(contrasena) {
  const sal = crypto.randomBytes(16).toString('hex');
  const hash = crypto.scryptSync(contrasena, sal, 64).toString('hex');
  return `${sal}:${hash}`;
}

// Revisa si una contraseña coincide con el "sal:hash" guardado
function contrasenaCorrecta(contrasena, guardado) {
  if (typeof contrasena !== 'string' || typeof guardado !== 'string' || !guardado.includes(':')) return false;
  const [sal, hash] = guardado.split(':');
  const calculado = crypto.scryptSync(contrasena, sal, 64);
  const esperado = Buffer.from(hash, 'hex');
  // timingSafeEqual compara sin dar pistas por el tiempo que tarda
  return esperado.length === calculado.length && crypto.timingSafeEqual(esperado, calculado);
}

// Token de sesión: 32 bytes al azar en hexadecimal (64 letras y números)
function nuevoToken() {
  return crypto.randomBytes(32).toString('hex');
}

module.exports = { hashContrasena, contrasenaCorrecta, nuevoToken };
