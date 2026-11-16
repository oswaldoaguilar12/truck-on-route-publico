/speckit.specify Agregar a la app Android de Truck on Rute la ubicación en tiempo real del camión recolector, usando un GPS real.

## Features principales

### El dispositivo del camión reporta su ubicación
- Un ESP32 con GPS NEO-6M lee la posición del camión
- Cada 10 segundos envía camión, latitud, longitud y fuente ("gps") a la API
- Solo envía si el GPS tiene un dato válido
- Primero envía por WiFi (pruebas) y después por red celular con SIM800L

### La API guarda y entrega la última ubicación
- Recibe ubicaciones con POST /ubicacion
- Rechaza datos incompletos o fuera de rango (400)
- Agrega la fecha y hora de recepción
- Entrega la última ubicación con GET /ubicacion (404 si todavía no hay)

### El ciudadano ve el camión en el mapa de la app
- La pantalla de Inicio muestra un mapa de Huauchinango con el ícono del camión
- El mapa consulta la API cada 10 segundos y mueve el ícono
- Muestra si la ubicación es real o simulada y la hora de la última actualización
- Si la ubicación tiene más de 60 segundos, muestra "Sin señal reciente"
- Si no hay ubicación, muestra "Sin ubicación disponible"

### Simulación para probar sin hardware
- Un script envía a la API las coordenadas de ejemplo con fuente "simulacion"

## Lo que NO se va a construir
- No habrá login real, registro real ni gestión de usuarios real (las pantallas actuales se quedan como están)
- No habrá tiempo estimado de llegada, notificaciones ni historial
- No habrá base de datos ni nube
- No habrá varios camiones (solo CAMION-01)
- No se usarán el MPU6050, el AHT20-BMP280, motores ni panel solar
