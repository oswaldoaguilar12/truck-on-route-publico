@echo off
rem Truck on Rute 2.5 - arranca la API y la simulacion para la demo.
rem Doble clic en este archivo. Abre dos ventanas: no las cierres durante la demo.
cd /d "%~dp0api"
if not exist node_modules (
  echo Instalando dependencias por primera vez...
  call npm install
)
start "API Truck on Rute" cmd /k npm start
timeout /t 3 /nobreak >nul
start "Simulacion del camion" cmd /k npm run simular
rem Si hay un celular conectado por cable (o el emulador), le pasa el puerto 3000 de la computadora.
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if exist "%ADB%" (
  "%ADB%" -d reverse tcp:3000 tcp:3000 && echo Celular conectado: ve la API en localhost:3000
)
echo.
echo Listo. Ahora ejecuta la app en Android Studio (boton verde de Play).
echo Para probar la API en el navegador: http://localhost:3000/ubicacion
pause
