package com.example.truckonroute.data

import android.util.Log
import com.example.truckonroute.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Consulta la API de Truck on Rute.
// Se usan HttpURLConnection y org.json porque ya vienen con Android (sin librerías extra).
object UbicacionApi {

    private const val TAG = "UbicacionApi"
    private const val TIEMPO_MAXIMO_MS = 5_000 // si la API no responde en 5 s, cuenta como "no responde"

    // Pide la última ubicación con GET /ubicacion.
    // Devuelve null si todavía no hay ubicación (404), si la API está apagada o si no hay internet.
    suspend fun obtenerUltima(): Ubicacion? = withContext(Dispatchers.IO) {
        // Dispatchers.IO: la red nunca se usa en el hilo de la pantalla
        var conexion: HttpURLConnection? = null
        try {
            conexion = URL("${BuildConfig.API_URL}/ubicacion").openConnection() as HttpURLConnection
            conexion.connectTimeout = TIEMPO_MAXIMO_MS
            conexion.readTimeout = TIEMPO_MAXIMO_MS

            if (conexion.responseCode != 200) {
                Log.i(TAG, "La API respondió ${conexion.responseCode} (404 = todavía no hay ubicación)")
                return@withContext null
            }

            val texto = conexion.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(texto)
            Ubicacion(
                camion = json.getString("camion"),
                lat = json.getDouble("lat"),
                lng = json.getDouble("lng"),
                fuente = json.getString("fuente"),
                fecha = json.getString("fecha")
            )
        } catch (e: Exception) {
            // Aquí llegan: API apagada, sin internet, URL mal escrita o HTTP bloqueado
            // (si dice "Cleartext HTTP traffic not permitted", revisa network_security_config.xml)
            Log.w(TAG, "No se pudo consultar ${BuildConfig.API_URL}: ${e.message}")
            null
        } finally {
            conexion?.disconnect()
        }
    }

    // Versión 2.6: pide los últimos puntos del camión con GET /ubicacion/historial.
    // Sirven para dibujar en el mapa la ruta que ya recorrió. Si falla, devuelve una lista vacía.
    suspend fun obtenerHistorial(limite: Int = 50): List<Ubicacion> = withContext(Dispatchers.IO) {
        val texto = leer("/ubicacion/historial?limite=$limite") ?: return@withContext emptyList()
        try {
            val arreglo = JSONArray(texto)
            (0 until arreglo.length()).map { i ->
                val json = arreglo.getJSONObject(i)
                Ubicacion(
                    camion = json.getString("camion"),
                    lat = json.getDouble("lat"),
                    lng = json.getDouble("lng"),
                    fuente = json.getString("fuente"),
                    fecha = json.getString("fecha")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Historial con formato inesperado: ${e.message}")
            emptyList()
        }
    }

    // Versión 2.6: pide velocidad y distancia recorrida con GET /estadisticas.
    suspend fun obtenerEstadisticas(): Estadisticas? = withContext(Dispatchers.IO) {
        val texto = leer("/estadisticas") ?: return@withContext null
        try {
            val json = JSONObject(texto)
            Estadisticas(
                velocidadKmh = if (json.isNull("velocidadKmh")) null else json.getDouble("velocidadKmh"),
                distanciaKm = json.optDouble("distanciaKm", 0.0),
                puntosRecibidos = json.optInt("puntosRecibidos", 0)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Estadísticas con formato inesperado: ${e.message}")
            null
        }
    }

    // Hace un GET a la API y devuelve el texto de la respuesta, o null si no respondió 200
    private fun leer(ruta: String): String? {
        var conexion: HttpURLConnection? = null
        return try {
            conexion = URL("${BuildConfig.API_URL}$ruta").openConnection() as HttpURLConnection
            conexion.connectTimeout = TIEMPO_MAXIMO_MS
            conexion.readTimeout = TIEMPO_MAXIMO_MS
            if (conexion.responseCode != 200) null
            else conexion.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo consultar $ruta: ${e.message}")
            null
        } finally {
            conexion?.disconnect()
        }
    }
}
