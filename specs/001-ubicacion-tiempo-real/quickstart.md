# Quickstart: Ubicación del camión en tiempo real

Validación de extremo a extremo. Cada paso dice qué ejecutar y qué debes ver.

## Prerrequisitos

- Node.js LTS (`node -v` ≥ v20)
- Android Studio con un emulador o un celular Android en modo desarrollador
- Arduino IDE con soporte para ESP32 y la librería TinyGPSPlus

## 1. API

```sh
cd api
npm install
npm test        # todos los tests en verde
npm start       # "API de Truck on Rute escuchando en el puerto 3000"
```

En otra terminal:

```sh
curl http://localhost:3000/ubicacion
```

Debes ver: `404` y `{"error":"Todavía no hay ubicación"}`.

## 2. Simulación

```sh
cd api
npm run simular
```

Debes ver cada 10 s: `Enviado punto 1/6 → 200`, etc. Y `curl http://localhost:3000/ubicacion` muestra `"fuente":"simulacion"`.

## 3. App con simulación

1. Emulador: no hay que configurar nada (la app usa `http://10.0.2.2:3000`).
   Celular en el mismo WiFi: en `app-android/local.properties` agrega `API_URL=http://<IP de tu computadora>:3000` y agrega esa IP en `app/src/main/res/xml/network_security_config.xml`.
2. Abre `app-android` en Android Studio y ejecuta la app.
3. Inicia sesión con cualquier correo y contraseña.

Debes ver en Inicio: el mapa de Huauchinango, el camión moviéndose entre los 6 puntos y el texto "Simulación · hh:mm".

4. Detén la API (Ctrl+C). En máximo 20 s la app dice "Sin ubicación disponible" y no se cierra.

## 4. GPS real por WiFi

1. Copia `firmware/secrets.h.example` como `firmware/envio_wifi/secrets.h` y llena el WiFi y la URL de la API.
2. Carga `firmware/envio_wifi/envio_wifi.ino` al ESP32 y abre el monitor serial a 115200.
3. Lleva el dispositivo a cielo abierto y espera el fix.

Debes ver en el monitor: `GPS valido · sats=7 · lat=20.17… lng=-98.05… → POST 200`.
Y en la app: "Ubicación real · hh:mm" y el camión donde estás.

## 5. GPS real por red celular

1. Publica la API en internet por HTTP y pon esa URL en `secrets.h` y en `API_URL`.
2. Carga `firmware/envio_celular/envio_celular.ino`.
3. Apaga el WiFi del celular (usa datos móviles).

Debes ver en la app la ubicación real actualizándose.
