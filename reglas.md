/speckit.constitution Este proyecto es Truck on Rute: muestra en una app Android la ubicación en tiempo real de un camión recolector de basura en Huauchinango, Puebla. Un ESP32 con GPS NEO-6M envía la ubicación a una API en Node.js y la app la muestra en un mapa.

Reglas del proyecto:

1. Alcance: GPS NEO-6M → ESP32 → (WiFi para pruebas, luego SIM800L) → API Node.js → mapa en la app Android. Nada más. Fuera de alcance: carrito con motores, panel solar, MPU6050, AHT20-BMP280, login real, ETA, notificaciones, historial y panel de administración real.

2. Estructura: tres carpetas, una por parte del sistema: firmware/ (ESP32), api/ (Node.js) y app-android/ (la app original). No se mezclan.

3. Firmware: C++ con Arduino IDE. GPS con TinyGPSPlus por Serial2 a 9600 baudios. Un sketch pequeño por etapa.

4. API: Node.js LTS con JavaScript y Express. La ubicación se guarda en memoria; sin base de datos en esta fase. Elegir base de datos o nube requiere preguntar al equipo.

5. App Android: se parte de la app original (Kotlin + Jetpack Compose). Se cambia solo lo necesario para mostrar el mapa; las pantallas de login, registro, perfil y gestión se quedan como están.

6. Contrato de datos: todo el sistema usa el formato de contracts/api-ubicacion.md. Si algo usa otro formato, se avisa antes de cambiarlo.

7. Simulación separada de lo real: cada ubicación lleva fuente "gps" o "simulacion", y la app muestra cuál es.

8. Sin secretos en el código: contraseña del WiFi, APN, URL de la API y clave del mapa van en secrets.h, .env o local.properties, que no se suben a git.

9. Código sencillo y comentado en español, que el equipo pueda explicar en clase. Sin librerías extra para cosas que se resuelven con pocas líneas.

10. Errores claros: la API responde { "error": "mensaje" } con 400 o 404; el ESP32 imprime en el monitor serial qué pasó; la app muestra un mensaje en vez de cerrarse.

11. Pruebas: la lógica de validación de la API tiene tests con node --test (incluido en Node). El resto se prueba con la prueba de cada etapa.

12. Una etapa a la vez: termina solo cuando su prueba pasa y el equipo lo confirma. Al terminar se escribe un resumen para Jira.

13. Seguridad eléctrica: el SIM800L nunca se alimenta del ESP32. No se cambian conexiones sin revisar hardware-conexiones.md.

14. No comprar componentes sin comprobar antes que hacen falta.


---

## Enmienda 2026-11-02 (constitución v2.0.0)

Las reglas de arriba son las originales (2026-10-22) y se dejan tal cual como referencia. La versión 2.6 (spec 002) cambió estas reglas; la constitución v2.0.0 lo formaliza:

- Regla 1: además de la cadena GPS → mapa, se permite historial, velocidad y distancia, panel web del administrador (solo lectura) y MySQL opcional. Siguen fuera: login real, ETA, notificaciones, varios camiones en la app, nube, motores, panel solar y sensores extra.
- Regla 4: la API funciona en memoria; MySQL es opcional (solo con `DB_HOST` en `.env`) y con un usuario propio, nunca `root`.
- Nueva: si algo se documenta después de hacerlo, se escribe con la fecha real y se marca como "registrado después".
