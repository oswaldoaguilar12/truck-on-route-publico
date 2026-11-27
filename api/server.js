// Arranca la API de Truck on Rute. La lógica está en app.js.

try {
  process.loadEnvFile();
} catch {
  // Sin .env: se usan los valores por defecto
}

const { crearApp } = require('./app');
const db = require('./db');

const PORT = process.env.PORT || 3000;

// v2.15 (spec 011, FR-1105): en producción (Railway) no se arranca con una contraseña de administrador débil o de ejemplo
function revisarProduccion() {
  if (process.env.PRODUCCION !== '1') return;
  const c = process.env.ADMIN_CONTRASENA || '';
  const deEjemplo = ['Admin2026', 'Chofer2026', 'Ana2026'];
  if (c.length < 8 || deEjemplo.includes(c)) {
    console.error('PRODUCCION=1 pero ADMIN_CONTRASENA falta, tiene menos de 8 caracteres o es una de ejemplo.');
    console.error('Ponla en las variables de Railway (o en .env) y vuelve a desplegar. Ver docs/railway.md.');
    process.exit(1);
  }
  if (!process.env.CLAVE_GPS) console.warn('Aviso: no hay CLAVE_GPS; cualquiera podría mandar ubicaciones a POST /ubicacion.');
}

async function iniciar() {
  revisarProduccion();
  await db.conectar(); // opcional: solo si .env tiene DB_HOST
  const app = crearApp();

  // En Express 5, si el servidor no puede arrancar, el error llega a esta misma función
  app.listen(PORT, (err) => {
    if (err) {
      if (err.code === 'EADDRINUSE') {
        console.error(`El puerto ${PORT} ya está ocupado: probablemente la API ya está corriendo en otra terminal.`);
        console.error('Solución 1: cierra esa terminal o presiona Ctrl+C en ella.');
        console.error(`Solución 2 (Windows): netstat -ano | findstr :${PORT}  y luego  taskkill /PID <número> /F`);
        console.error('Solución 3: usa otro puerto: en PowerShell  $env:PORT=3001  (en CMD  set PORT=3001) y vuelve a ejecutar npm start');
      } else {
        console.error('No se pudo iniciar la API:', err.message);
      }
      process.exit(1);
    }
    console.log(`API de Truck on Rute escuchando en el puerto ${PORT}`);
    console.log(`Panel web:        http://localhost:${PORT}/panel`);
    console.log(`Última ubicación: http://localhost:${PORT}/ubicacion`);
  });
}

iniciar();
