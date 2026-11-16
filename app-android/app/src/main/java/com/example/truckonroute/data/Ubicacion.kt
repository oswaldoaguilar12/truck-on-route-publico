package com.example.truckonroute.data

// Una ubicación del camión, tal como la entrega GET /ubicacion.
// Formato: specs/001-ubicacion-tiempo-real/contracts/api-ubicacion.md
data class Ubicacion(
    val camion: String,
    val lat: Double,
    val lng: Double,
    val fuente: String, // "gps" (dato real) o "simulacion"
    val fecha: String   // hora en que la API la recibió, ISO 8601 en UTC
)

// Números de GET /estadisticas (versión 2.6) que se muestran debajo del mapa
data class Estadisticas(
    val velocidadKmh: Double?, // null si todavía hay menos de 2 puntos
    val distanciaKm: Double,
    val puntosRecibidos: Int
)
