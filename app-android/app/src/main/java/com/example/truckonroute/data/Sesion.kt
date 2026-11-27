package com.example.truckonroute.data

import android.content.Context
import org.json.JSONObject

// Versión 2.8 (spec 004): la cuenta con la que se inició sesión.
// rol: "ciudadano", "conductor" o "administrador"
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val colonia: String,
    val rol: String
) {
    val esChofer get() = rol == "conductor"

    companion object {
        fun desdeJson(json: JSONObject) = Usuario(
            id = json.optInt("id"),
            nombre = json.optString("nombre"),
            correo = json.optString("correo"),
            telefono = json.optString("telefono"),
            colonia = json.optString("colonia"),
            rol = json.optString("rol", "ciudadano")
        )
    }

    fun aJson(): JSONObject = JSONObject()
        .put("id", id).put("nombre", nombre).put("correo", correo)
        .put("telefono", telefono).put("colonia", colonia).put("rol", rol)
}

// Guarda el token y el usuario en el celular (SharedPreferences, ya viene con Android),
// así no hay que iniciar sesión cada vez que se abre la app.
object Sesion {
    private const val ARCHIVO = "sesion_truck_on_route"

    fun guardar(contexto: Context, token: String, usuario: Usuario) {
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit()
            .putString("token", token)
            .putString("usuario", usuario.aJson().toString())
            .apply()
    }

    fun token(contexto: Context): String? =
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getString("token", null)

    fun usuario(contexto: Context): Usuario? {
        val texto = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getString("usuario", null) ?: return null
        return try { Usuario.desdeJson(JSONObject(texto)) } catch (e: Exception) { null }
    }

    fun cerrar(contexto: Context) {
        contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().clear().apply()
    }

    // Preferencias del ciudadano: avisos de cercanía y fecha del último aviso visto
    private const val PREFERENCIAS = "preferencias_truck_on_route"

    fun avisarCerca(contexto: Context): Boolean =
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).getBoolean("avisar_cerca", false)

    fun guardarAvisarCerca(contexto: Context, activo: Boolean) {
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit().putBoolean("avisar_cerca", activo).apply()
    }

    // Fecha (ISO) del aviso más nuevo que ya se notificó, para no repetirlo
    fun ultimoAviso(contexto: Context): String? =
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).getString("ultimo_aviso", null)

    fun guardarUltimoAviso(contexto: Context, fecha: String) {
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit().putString("ultimo_aviso", fecha).apply()
    }

    // v2.14 (spec 010, FR-1003): fecha (ISO) del aviso más nuevo que el usuario ya vio en la sección Avisos.
    // Se guarda solo en el celular. "clave" separa al ciudadano del chofer.
    fun avisosLeidosHasta(contexto: Context, clave: String = "ciudadano"): String? =
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).getString("leidos_$clave", null)

    fun guardarAvisosLeidosHasta(contexto: Context, fecha: String, clave: String = "ciudadano") {
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit().putString("leidos_$clave", fecha).apply()
    }

    // v2.14: último aviso del chofer que ya se notificó
    fun ultimoAvisoChofer(contexto: Context): String? =
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).getString("ultimo_aviso_chofer", null)

    fun guardarUltimoAvisoChofer(contexto: Context, fecha: String) {
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit().putString("ultimo_aviso_chofer", fecha).apply()
    }

    // Recorridos de los que ya se avisó "camión cerca" (una vez por recorrido, FR-410)
    fun yaAvisadoCerca(contexto: Context, recorridoId: Int): Boolean =
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
            .getStringSet("cerca_avisados", emptySet())!!.contains(recorridoId.toString())

    fun marcarAvisadoCerca(contexto: Context, recorridoId: Int) {
        val prefs = contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val actuales = prefs.getStringSet("cerca_avisados", emptySet())!!.toMutableSet()
        actuales.add(recorridoId.toString())
        prefs.edit().putStringSet("cerca_avisados", actuales).apply()
    }
}
