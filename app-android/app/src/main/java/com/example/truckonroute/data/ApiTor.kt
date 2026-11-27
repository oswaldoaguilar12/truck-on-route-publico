package com.example.truckonroute.data

import android.util.Log
import com.example.truckonroute.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Versión 2.8 (spec 004): sesión, registro, recorridos del chofer y avisos.
// Contrato: specs/004-roles-recorridos-avisos/contracts/api-v3.md
// Igual que UbicacionApi: HttpURLConnection y org.json, sin librerías extra.

// Respuesta de la API: el código HTTP, el texto y, si falló, el mensaje para mostrar
data class Respuesta(val codigo: Int, val texto: String, val error: String?) {
    val ok get() = codigo in 200..299
}

data class Recorrido(
    val id: Int,
    val camion: String,
    val ruta: String,
    val colonias: List<String>,
    val horario: String,
    val indicaciones: String,
    val estado: String,  // "programado", "en_curso" o "terminado"
    val fecha: String,
    val inicio: String?,
    val motivoDesvio: String? = null // v2.13 (FR-909): motivo si el administrador puso un desvío
)

data class Mensaje(val id: Int, val texto: String, val fecha: String)

// v2.13: tipo "en_ruta" o "desvio" (FR-912)
data class Aviso(val id: Int, val titulo: String, val mensaje: String, val fecha: String, val tipo: String = "")

// v2.13 (spec 009): un punto del camino dibujado
data class PuntoRuta(val lat: Double, val lng: Double)

data class RecorridoEnCurso(
    val id: Int,
    val camion: String,
    val ruta: String,
    val colonias: List<String>,
    val trazo: List<PuntoRuta> = emptyList(),   // FR-904: camino dibujado (vacío si no tiene)
    val avance: Int = 0,                        // cuántos puntos del trazo ya pasó el camión
    val desvio: List<PuntoRuta> = emptyList(),  // FR-908: desvío por bloqueo (vacío si no hay)
    val motivoDesvio: String? = null
) {
    // De 0 a 1: qué parte del camino ya recorrió
    val fraccion: Float get() = if (trazo.isEmpty()) 0f else avance.toFloat() / trazo.size
}

object ApiTor {

    private const val TAG = "ApiTor"
    private const val TIEMPO_MAXIMO_MS = 5_000
    const val SIN_CONEXION = "No se pudo conectar con el servidor. Revisa que la API esté encendida."

    // Hace la petición. codigo = 0 significa que no hubo conexión.
    private suspend fun pedir(metodo: String, ruta: String, cuerpo: JSONObject? = null, token: String? = null): Respuesta =
        withContext(Dispatchers.IO) {
            var conexion: HttpURLConnection? = null
            try {
                conexion = URL("${BuildConfig.API_URL}$ruta").openConnection() as HttpURLConnection
                conexion.requestMethod = metodo
                conexion.connectTimeout = TIEMPO_MAXIMO_MS
                conexion.readTimeout = TIEMPO_MAXIMO_MS
                conexion.setRequestProperty("Content-Type", "application/json")
                if (token != null) conexion.setRequestProperty("Authorization", "Bearer $token")
                if (cuerpo != null) {
                    conexion.doOutput = true
                    conexion.outputStream.use { it.write(cuerpo.toString().toByteArray(Charsets.UTF_8)) }
                }
                val codigo = conexion.responseCode
                val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
                val texto = flujo?.bufferedReader()?.use { it.readText() } ?: ""
                val error = if (codigo in 200..299) null
                    else try { JSONObject(texto).optString("error", "Error $codigo") } catch (e: Exception) { "Error $codigo" }
                Respuesta(codigo, texto, error)
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo consultar $ruta: ${e.message}")
                Respuesta(0, "", SIN_CONEXION)
            } finally {
                conexion?.disconnect()
            }
        }

    // ---------- Sesión y registro ----------

    // Devuelve el token y el usuario, o el mensaje de error
    suspend fun iniciarSesion(correo: String, contrasena: String): Pair<Pair<String, Usuario>?, String?> {
        val r = pedir("POST", "/sesion", JSONObject().put("correo", correo.trim()).put("contrasena", contrasena))
        if (!r.ok) return null to r.error
        val json = JSONObject(r.texto)
        return (json.getString("token") to Usuario.desdeJson(json.getJSONObject("usuario"))) to null
    }

    // Devuelve null si se creó la cuenta, o el mensaje de error
    // v2.12 (spec 008): la API pide aceptaAviso = true para crear la cuenta
    suspend fun registrar(nombre: String, correo: String, telefono: String, colonia: String, contrasena: String, aceptaAviso: Boolean): String? {
        val r = pedir("POST", "/usuarios", JSONObject()
            .put("nombre", nombre).put("correo", correo.trim()).put("telefono", telefono)
            .put("colonia", colonia).put("contrasena", contrasena).put("aceptaAviso", aceptaAviso))
        return r.error
    }

    suspend fun cerrarSesion(token: String) {
        pedir("DELETE", "/sesion", token = token)
    }

    // ---------- Chofer ----------

    // codigo 401 = la sesión venció
    suspend fun misRecorridos(token: String): Pair<List<Recorrido>, Respuesta> {
        val r = pedir("GET", "/chofer/recorridos", token = token)
        if (!r.ok) return emptyList<Recorrido>() to r
        val arreglo = JSONArray(r.texto)
        return (0 until arreglo.length()).map { leerRecorrido(arreglo.getJSONObject(it)) } to r
    }

    suspend fun cambiarRecorrido(token: String, id: Int, accion: String): Respuesta =
        pedir("POST", "/chofer/recorridos/$id/$accion", token = token) // accion: "iniciar" o "terminar"

    suspend fun misMensajes(token: String): List<Mensaje> {
        val r = pedir("GET", "/chofer/mensajes", token = token)
        if (!r.ok) return emptyList()
        val arreglo = JSONArray(r.texto)
        return (0 until arreglo.length()).map {
            val m = arreglo.getJSONObject(it)
            Mensaje(m.optInt("id"), m.optString("texto"), m.optString("fecha"))
        }
    }

    private fun leerRecorrido(j: JSONObject) = Recorrido(
        id = j.optInt("id"),
        camion = j.optString("camion"),
        ruta = j.optString("ruta"),
        colonias = lista(j.optJSONArray("colonias")),
        horario = j.optString("horario"),
        indicaciones = j.optString("indicaciones"),
        estado = j.optString("estado"),
        fecha = j.optString("fecha"),
        inicio = if (j.isNull("inicio")) null else j.optString("inicio"),
        motivoDesvio = if (j.isNull("motivoDesvio") || !j.has("motivoDesvio")) null else j.optString("motivoDesvio")
    )

    // v2.13: [[lat, lng], ...] -> lista de puntos
    private fun puntos(a: JSONArray?): List<PuntoRuta> = if (a == null) emptyList() else (0 until a.length()).mapNotNull {
        val p = a.optJSONArray(it) ?: return@mapNotNull null
        PuntoRuta(p.optDouble(0), p.optDouble(1))
    }

    private fun lista(a: JSONArray?): List<String> = if (a == null) emptyList() else (0 until a.length()).map { a.getString(it) }

    // ---------- Ciudadano ----------

    // Avisos después de "desde" (ISO). Sin fecha, la API da los de las últimas 12 horas.
    suspend fun avisos(desde: String?): List<Aviso> {
        val ruta = if (desde == null) "/avisos" else "/avisos?desde=" + URLEncoder.encode(desde, "UTF-8")
        val r = pedir("GET", ruta)
        if (!r.ok) return emptyList()
        return try {
            val arreglo = JSONArray(r.texto)
            (0 until arreglo.length()).map {
                val a = arreglo.getJSONObject(it)
                Aviso(a.optInt("id"), a.optString("titulo"), a.optString("mensaje"), a.optString("fecha"), a.optString("tipo"))
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun recorridosEnCurso(): List<RecorridoEnCurso> {
        val r = pedir("GET", "/recorridos/en-curso")
        if (!r.ok) return emptyList()
        return try {
            val arreglo = JSONArray(r.texto)
            (0 until arreglo.length()).map {
                val j = arreglo.getJSONObject(it)
                RecorridoEnCurso(
                    j.optInt("id"), j.optString("camion"), j.optString("ruta"), lista(j.optJSONArray("colonias")),
                    trazo = puntos(j.optJSONArray("trazo")),
                    avance = j.optInt("avance"),
                    desvio = puntos(j.optJSONArray("desvio")),
                    motivoDesvio = if (j.isNull("motivoDesvio") || !j.has("motivoDesvio")) null else j.optString("motivoDesvio")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // Última ubicación de un camión en particular
    suspend fun ubicacionDe(camion: String): Ubicacion? {
        val r = pedir("GET", "/ubicacion?camion=" + URLEncoder.encode(camion, "UTF-8"))
        if (!r.ok) return null
        return try {
            val j = JSONObject(r.texto)
            Ubicacion(j.getString("camion"), j.getDouble("lat"), j.getDouble("lng"), j.getString("fuente"), j.getString("fecha"))
        } catch (e: Exception) { null }
    }
}
