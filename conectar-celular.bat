@echo off
rem Truck on Rute 2.5 - vuelve a conectar el celular a la API (usalo si desconectaste el cable).
rem La API ya debe estar abierta con iniciar-demo.bat.
rem Pasa el puerto 3000 de la computadora al celular conectado por cable.
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB%" (
  echo No encontre adb en %ADB%
  goto fin
)
echo Buscando el celular por cable... si se queda aqui, conecta el celular y acepta "Permitir depuracion USB".
"%ADB%" -d wait-for-device
"%ADB%" -d reverse tcp:3000 tcp:3000
if errorlevel 1 (
  echo ERROR: no se pudo pasar la API al celular. Revisa el cable y la depuracion USB.
) else (
  echo Celular conectado: ve la API en localhost:3000
)
:fin
pause
