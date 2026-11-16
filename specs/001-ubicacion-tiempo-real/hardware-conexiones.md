# Hardware y conexiones

No se cambian conexiones sin revisar esta tabla. Lo que diga "Pendiente" se confirma en la Etapa 0, mirando el circuito real.

## Componentes

| Componente | Estado | Notas |
|---|---|---|
| ESP32 + placa de expansión | Disponible | Microcontrolador principal |
| GPS NEO-6M | Conectado, sin coordenadas válidas confirmadas | En COM6 ya se vieron datos |
| SIM800L + antena | Disponible, sin probar | Ver precauciones |
| Baterías recargables | En evaluación | Pendiente: tipo y voltaje |
| MPU6050 | Probado (ejes X/Y) | **Fuera de alcance.** Sus X/Y no son coordenadas |
| AHT20-BMP280 | Sin verificar | **Fuera de alcance** |

## Tabla de conexiones

| Módulo | Pin del módulo | Pin del ESP32 | Confirmado |
|---|---|---|---|
| NEO-6M | VCC | 3.3 V o 5 V: Pendiente | No |
| NEO-6M | GND | GND | No |
| NEO-6M | TX | GPIO 16 (RX2) — sugerido | No |
| NEO-6M | RX | GPIO 17 (TX2) — sugerido | No |
| SIM800L | VCC | Fuente externa ~4 V (no del ESP32) | No |
| SIM800L | GND | GND común con el ESP32 | No |
| SIM800L | TXD | Pendiente (sugerido GPIO 26) | No |
| SIM800L | RXD | Pendiente (sugerido GPIO 27) | No |

## Precauciones

- **TX con RX:** el TX de un módulo va al RX del ESP32, y el RX al TX.
- **GPS NEO-6M:** 9600 baudios. El primer "fix" puede tardar varios minutos y necesita cielo abierto. Dentro del salón es normal ver ceros.
- **SIM800L, alimentación:** necesita entre **3.4 V y 4.4 V** y picos de hasta **2 A**. **Nunca** del pin 3.3 V ni 5 V del ESP32. Usar batería Li-ion 3.7 V o un regulador step-down ajustado a ~4 V, con un capacitor de ≈1000 µF cerca del módulo. GND común con el ESP32. Si el LED parpadea y el módulo se reinicia, casi siempre es alimentación.
- **SIM800L, red:** es 2G. Confirmar que el operador tiene 2G en Huauchinango. El chip debe tener saldo o datos y no tener PIN.
- **API y red celular:** `localhost:3000` solo existe en la computadora. Para el SIM800L, la API debe estar publicada en internet y de preferencia por HTTP.
