package com.example.truckonroute.avisos

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.truckonroute.R
import com.example.truckonroute.data.Sesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Versión 2.8 (spec 004, FR-410): "Avisarme cuando esté cerca".
// Android no deja revisar cada 5 minutos en segundo plano a menos que la app muestre
// una notificación fija; por eso es un servicio "en primer plano" que el ciudadano enciende y apaga.
class ServicioCercania : Service() {

    companion object {
        private const val CADA_MS = 5 * 60 * 1000L // cada 5 minutos
        private const val ID_NOTIFICACION_FIJA = 1

        fun iniciar(contexto: Context) {
            ContextCompat.startForegroundService(contexto, Intent(contexto, ServicioCercania::class.java))
        }

        fun detener(contexto: Context) {
            contexto.stopService(Intent(contexto, ServicioCercania::class.java))
        }
    }

    private val alcance = CoroutineScope(Dispatchers.IO + SupervisorJob())
    // Mantiene fresca la ubicación del celular (pide una lectura cada minuto como máximo)
    // (se escriben los 4 métodos para que funcione también en Android 7 a 10)
    private val escucha = object : LocationListener {
        override fun onLocationChanged(location: Location) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        @Deprecated("Solo para Android viejos")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Notificaciones.crearCanales(this)
        val fija = NotificationCompat.Builder(this, Notificaciones.CANAL_SERVICIO)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Truck on Route está vigilando el camión")
            .setContentText("Te avisamos cuando esté a menos de 500 m")
            .setContentIntent(Notificaciones.abrirApp(this))
            .setOngoing(true)
            .build()
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        try {
            ServiceCompat.startForeground(this, ID_NOTIFICACION_FIJA, fija, tipo)
        } catch (e: Exception) {
            // Sin permiso de ubicación Android no deja iniciar el servicio: se apaga el interruptor
            Sesion.guardarAvisarCerca(this, false)
            stopSelf()
            return START_NOT_STICKY
        }
        pedirUbicaciones()

        alcance.coroutineContext.cancelChildren()
        alcance.launch {
            while (isActive) {
                Vigilancia.revisarAvisos(this@ServicioCercania)
                Vigilancia.revisarCercania(this@ServicioCercania)
                delay(CADA_MS)
            }
        }
        return START_STICKY // si Android lo cierra por memoria, lo vuelve a abrir
    }

    @SuppressLint("MissingPermission")
    private fun pedirUbicaciones() {
        if (!Permisos.hayUbicacion(this)) return
        val gestor = getSystemService(LOCATION_SERVICE) as LocationManager
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { gestor.isProviderEnabled(it) }
            .forEach { gestor.requestLocationUpdates(it, 60_000L, 50f, escucha, Looper.getMainLooper()) }
    }

    override fun onDestroy() {
        (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(escucha)
        alcance.cancel()
        super.onDestroy()
    }
}
