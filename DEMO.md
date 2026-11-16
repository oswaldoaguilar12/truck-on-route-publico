# Guion de la demo para el profe

Lo que se demuestra: la app Truck on Rute muestra en un mapa dónde va el camión de la basura, con datos que manda una API hecha por el equipo. Todavía sin hardware: la ubicación viene de una **simulación** (6 puntos reales de Huauchinango).

**Nuevo en 2.6:** la app dibuja la ruta que ya recorrió el camión, muestra velocidad y km, tiene botón Centrar, y hay un **panel web del administrador** en `http://localhost:3000/panel`. Las ubicaciones se pueden guardar en MySQL.

## Antes de clase (5 min)

1. Conecta tu celular por cable USB (con la depuración USB activada) y cierra el emulador si está abierto.
2. Doble clic en `iniciar-demo.bat` (en `C:\truck 2.5`). Se abren dos ventanas negras:
   - **API Truck on Rute**: dice `escuchando en el puerto 3000`.
   - **Simulación del camión**: cada 10 s dice `Enviado punto 1/6 (…) → 200`.
   - En la ventana principal debe salir `Celular conectado: ve la API en localhost:3000`.
3. Abre Android Studio en `app-android`, elige tu celular arriba y dale Play a **app**.
4. El celular necesita internet (datos o WiFi) solo para las imágenes del mapa; la API le llega por el cable.

Si el celular dice "Sin ubicación disponible" o desconectaste el cable: con el celular conectado, doble clic en `conectar-celular.bat`. No abras otra vez `iniciar-demo.bat` (ya hay una API abierta).

## Durante la demo (en este orden)

| # | Qué haces | Qué dices |
|---|-----------|-----------|
| 1 | Abres la app | "Esta es la pantalla de inicio de sesión." |
| 2 | Escribes cualquier correo y contraseña, Iniciar sesión | "Por ahora acepta cualquier usuario; el registro real es otra fase." |
| 3 | En Inicio, señalas el mapa | "Aquí está el camión en el mapa de Huauchinango. La app le pregunta a nuestra API cada 10 segundos." |
| 4 | Esperas 10–20 s | "Se mueve solo: la simulación manda una nueva ubicación cada 10 segundos." |
| 5 | Señalas el texto bajo el mapa | "Dice *Simulación* y la hora, para no confundir datos de prueba con reales. Con el GPS dirá *Ubicación real*." |
| 5b | Señalas la línea verde y el texto de velocidad | "Nuevo en 2.6: la línea es la ruta que ya recorrió, y abajo la velocidad y los km que calcula la API." |
| 5c | Arrastras el mapa y luego tocas **Centrar** | "Puedo mover el mapa sin que regrese solo; Centrar vuelve al camión." |
| 6 | Abres `http://localhost:3000/panel` en la computadora | "Este es el panel del administrador: estado, velocidad, distancia, el mapa con la ruta y los últimos puntos. Se actualiza cada 5 segundos." |
| 6b | Si configuraste MySQL: en Workbench `SELECT * FROM ubicaciones ORDER BY id_ubicacion DESC;` | "Cada ubicación queda guardada en nuestra base de datos." |
| 7 | En la barra de búsqueda escribes `zaca` | "Salen Zacamila, Lomas de Zacamila e INFONAVIT Zacamila. El buscador tiene las 83 colonias reales de Huauchinango, y no importa si escribes con acento." |
| 8 | Cierras la ventana **Simulación** y esperas ~70 s | "Si el camión deja de mandar datos, la app avisa *Sin señal reciente*." |
| 9 | Cierras la ventana **API** | "Si la API se cae, la app dice *Sin ubicación disponible* y no se cierra." |
| 10 | Muestras Gestión de usuarios y Perfil | "Estas pantallas vienen del proyecto original y no cambiaron." |

## Si el profe pregunta

- **¿Cómo llega la ubicación real?** Un ESP32 con GPS NEO-6M la manda por WiFi a la API (después por SIM800L, red celular). Está en `specs/001-ubicacion-tiempo-real/hardware-conexiones.md`.
- **¿Cómo lo hicieron?** Con Spec Kit (SDD): reglas → especificación → plan → tareas → código. Todo está en la carpeta `specs/`.
- **¿Cómo saben que la API funciona?** `cd api` y `npm test`: 19 pruebas en verde. Si mandas una latitud de 200, la API responde 400 y no guarda nada.

## Si algo falla

- **El mapa sale en blanco**: el emulador no tiene internet; revisa el WiFi de la computadora. El camión y el texto igual funcionan.
- **"Sin ubicación disponible" desde el inicio**: el celular se conectó después de abrir `iniciar-demo.bat`, o la ventana de la API está cerrada. Con el celular conectado, vuelve a abrir `iniciar-demo.bat`.
- **La ventana de la API dice que el puerto 3000 está ocupado**: ya hay otra API abierta; ciérrala y repite.
