// Etapa 1: prueba del GPS NEO-6M (tarea T012).
// Conexiones: NEO-6M TX -> GPIO 16, NEO-6M RX -> GPIO 17, VCC -> 3.3 V, GND -> GND.
// Librería: TinyGPSPlus (Arduino IDE > Administrar bibliotecas).
// Monitor serial a 115200. El primer "valido=si" puede tardar varios minutos y necesita cielo abierto.

#include <TinyGPSPlus.h>

const int GPS_RX = 16;  // aquí llega el TX del GPS
const int GPS_TX = 17;  // de aquí sale hacia el RX del GPS

TinyGPSPlus gps;
unsigned long ultimoReporte = 0;

void setup() {
  Serial.begin(115200);
  Serial2.begin(9600, SERIAL_8N1, GPS_RX, GPS_TX);
  Serial.println("Prueba del GPS. Esperando datos...");
}

void loop() {
  // Leer todo lo que mande el GPS
  while (Serial2.available()) {
    gps.encode(Serial2.read());
  }

  // Reportar una vez por segundo
  if (millis() - ultimoReporte >= 1000) {
    ultimoReporte = millis();

    if (gps.charsProcessed() < 10) {
      Serial.println("No llegan datos del GPS: revisa que TX vaya a GPIO 16 y RX a GPIO 17.");
      return;
    }

    Serial.print("valido=");
    Serial.print(gps.location.isValid() ? "si" : "no");
    Serial.print(" · sats=");
    Serial.print(gps.satellites.value());
    Serial.print(" · lat=");
    Serial.print(gps.location.lat(), 6);
    Serial.print(" lng=");
    Serial.println(gps.location.lng(), 6);
  }
}
