// Arranca la API de Truck on Rute. La lógica está en app.js.

try {
  process.loadEnvFile();
} catch {
  // Sin .env: se usan los valores por defecto
}

const { crearApp } = require('./app');
const db = require('./db');

const PORT = process.env.PORT || 3000;

async function iniciar() {
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
