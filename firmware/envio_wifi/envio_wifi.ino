// Etapa 3: envía la ubicación real a la API por WiFi cada 10 s (tarea T014).
// Mismas conexiones del GPS que gps_prueba.ino.
// Antes de cargar: copia firmware/secrets.h.example aquí como secrets.h y llena WIFI_SSID, WIFI_PASSWORD y API_URL.
// API_URL es la IP de tu computadora en el mismo WiFi, por ejemplo "http://192.168.1.70:3000" (ver con ipconfig).

#include <WiFi.h>
#include <HTTPClient.h>
#include <TinyGPSPlus.h>
#include "secrets.h"

const int GPS_RX = 16;
const int GPS_TX = 17;
const unsigned long ENVIAR_CADA_MS = 10000;
const char* CAMION = "CAMION-01";

TinyGPSPlus gps;
unsigned long ultimoEnvio = 0;

void conectarWifi() {
  Serial.print("Conectando a ");
  Serial.println(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  // Espera máximo 15 s para no quedarse bloqueado
  unsigned long inicio = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - inicio < 15000) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("WiFi conectado. IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("No se pudo conectar al WiFi. Se reintenta en el siguiente envío.");
  }
}

void enviarUbicacion(double lat, double lng) {
  if (WiFi.status() != WL_CONNECTED) {
    conectarWifi();
    if (WiFi.status() != WL_CONNECTED) return;
  }

  // Mismo formato que contracts/api-ubicacion.md
  String cuerpo = String("{\"camion\":\"") + CAMION + "\",\"lat\":" + String(lat, 6) +
                  ",\"lng\":" + String(lng, 6) + ",\"fuente\":\"gps\"}";

  HTTPClient http;
  http.setTimeout(5000);
  http.begin(String(API_URL) + "/ubicacion");
  http.addHeader("Content-Type", "application/json");
  int codigo = http.POST(cuerpo);
  if (codigo > 0) {
    Serial.print("POST ");
    Serial.println(codigo);  // 200 = guardado, 400 = dato inválido
  } else {
    Serial.print("Error al enviar: ");
    Serial.println(http.errorToString(codigo));  // por ejemplo, la API está apagada
  }
  http.end();
}

void setup() {
  Serial.begin(115200);
  Serial2.begin(9600, SERIAL_8N1, GPS_RX, GPS_TX);
  conectarWifi();
}

void loop() {
  while (Serial2.available()) {
    gps.encode(Serial2.read());
  }

  if (millis() - ultimoEnvio < ENVIAR_CADA_MS) return;
  ultimoEnvio = millis();

  // Solo se envía un dato válido y reciente (sin fix el GPS da 0, 0)
  if (!gps.location.isValid() || gps.location.age() > 2000) {
    Serial.print("Esperando señal del GPS · sats=");
    Serial.println(gps.satellites.value());
    return;
  }

  Serial.print("GPS valido · sats=");
  Serial.print(gps.satellites.value());
  Serial.print(" · lat=");
  Serial.print(gps.location.lat(), 6);
  Serial.print(" lng=");
  Serial.print(gps.location.lng(), 6);
  Serial.print(" → ");
  enviarUbicacion(gps.location.lat(), gps.location.lng());
}
