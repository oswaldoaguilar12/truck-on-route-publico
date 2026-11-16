@echo off
rem Truck on Rute 2.5 - pone una contrasena nueva al usuario root de MySQL 8.0.
rem Uso: clic derecho > Ejecutar como administrador.
setlocal
net session >nul 2>&1
if errorlevel 1 (
  echo Este archivo necesita permisos de administrador.
  echo Cierra esta ventana, dale clic derecho al archivo y elige "Ejecutar como administrador".
  pause
  exit /b
)
set "BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin"
set "INI=C:\ProgramData\MySQL\MySQL Server 8.0\my.ini"
set "INIT=%TEMP%\mysql-init.txt"

rem Busca el nombre del servicio de MySQL (normalmente MySQL80)
set "SVC="
for /f "tokens=2" %%s in ('sc query state^= all ^| findstr /i /r "SERVICE_NAME:.*mysql"') do if not defined SVC set "SVC=%%s"
if not defined SVC set "SVC=MySQL80"
echo Servicio de MySQL: %SVC%
echo.
echo Escribe la nueva contrasena de root. Usa solo letras y numeros (por ejemplo Truck2025).
set /p "PASS=Nueva contrasena: "
if "%PASS%"=="" (
  echo No escribiste nada. No se cambio nada.
  pause
  exit /b
)
> "%INIT%" echo ALTER USER 'root'@'localhost' IDENTIFIED BY '%PASS%';

echo.
echo 1/4 Deteniendo MySQL...
net stop %SVC%
echo 2/4 Arrancando MySQL con la contrasena nueva (espera unos 20 segundos)...
start "MySQL temporal" /min "%BIN%\mysqld.exe" --defaults-file="%INI%" --init-file="%INIT%"
timeout /t 20 /nobreak >nul
echo 3/4 Apagando el MySQL temporal...
"%BIN%\mysqladmin.exe" -u root --password=%PASS% shutdown
if errorlevel 1 (
  echo No se pudo apagar con la contrasena nueva; se cierra a la fuerza.
  taskkill /im mysqld.exe /f >nul 2>&1
)
timeout /t 5 /nobreak >nul
del "%INIT%" >nul 2>&1
echo 4/4 Prendiendo MySQL normal...
net start %SVC%
echo.
echo Listo. Entra a MySQL Workbench con el usuario root y tu contrasena nueva.
echo Si Workbench tenia guardada la contrasena vieja: clic derecho en la conexion ^> Edit Connection ^> Clear.
pause
