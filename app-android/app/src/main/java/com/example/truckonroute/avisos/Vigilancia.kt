package com.example.truckonroute.avisos

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.example.truckonroute.data.ApiTor
import com.example.truckonroute.data.Aviso
import com.example.truckonroute.data.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Versión 2.8 (spec 004): lo que revisa la app para avisar al ciudadano.
// La usan la pantalla de Inicio (cada 20 s, con la app abierta) y ServicioCercania (cada 5 min, en segundo plano).
object Vigilancia {

    const val DISTANCIA_CERCA_M = 500f // "cerca" = a menos de 500 m (FR-410)

    // Fecha actual en el mismo formato que usa la API (UTC)
    fun ahoraIso(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return formato.format(Date())
    }

    // Pide los avisos nuevos ("el camión ya está en ruta") y notifica los que no se habían visto (FR-409).
    // La primera vez solo guarda la fecha, para no llenar el celular de avisos viejos.
    suspend fun revisarAvisos(contexto: Context): List<Aviso> {
        val ultimo = Sesion.ultimoAviso(contexto)
        if (ultimo == null) {
            Sesion.guardarUltimoAviso(contexto, ahoraIso())
            return emptyList()
        }
        val nuevos = ApiTor.avisos(ultimo)
        nuevos.forEach { Notificaciones.mostrar(contexto, 1000 + it.id, it.titulo, it.mensaje) }
        nuevos.maxByOrNull { it.fecha }?.let { Sesion.guardarUltimoAviso(contexto, it.fecha) }
        return nuevos
    }

    // Compara la ubicación del celular con la de cada camión en ruta (FR-410).
    // Devuelve la distancia en metros al camión más cercano, o null si no se pudo calcular.
    suspend fun revisarCercania(contexto: Context): Float? {
        val yo = ubicacionDelCelular(contexto) ?: return null
        var menor: Float? = null
        for (recorrido in ApiTor.recorridosEnCurso()) {
            val camion = ApiTor.ubicacionDe(recorrido.camion) ?: continue
            val resultado = FloatArray(1)
            Location.distanceBetween(yo.latitude, yo.longitude, camion.lat, camion.lng, resultado)
            val metros = resultado[0]
            if (menor == null || metros < menor) menor = metros
            // Solo una vez por recorrido
            if (metros < DISTANCIA_CERCA_M && !Sesion.yaAvisadoCerca(contexto, recorrido.id)) {
                Notificaciones.mostrar(
                    contexto, 2000 + recorrido.id,
                    "El camión está cerca",
                    "${recorrido.ruta} (${recorrido.camion}) está a ${metros.toInt()} m de ti. Saca tu basura."
                )
                Sesion.marcarAvisadoCerca(contexto, recorrido.id)
            }
        }
        return menor
    }

    // La última ubicación conocida del celular (GPS o red), la más reciente.
    // Se usa LocationManager porque ya viene con Android (sin Google Play Services).
    @SuppressLint("MissingPermission") // quien llama revisa el permiso antes
    fun ubicacionDelCelular(contexto: Context): Location? {
        if (!Permisos.hayUbicacion(contexto)) return null
        val gestor = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { gestor.isProviderEnabled(it) }
            .mapNotNull { gestor.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
    }
}
