package com.example.truckonroute.avisos

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.truckonroute.MainActivity
import com.example.truckonroute.R

// Versión 2.8 (spec 004): notificaciones del propio celular, sin Firebase.
object Notificaciones {

    const val CANAL_AVISOS = "avisos_camion"      // "El camión ya está en ruta" y "El camión está cerca"
    const val CANAL_SERVICIO = "vigilando_camion" // la notificación fija mientras se vigila el camión

    // Android 8 o más nuevo pide crear los canales antes de mostrar notificaciones
    fun crearCanales(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gestor = contexto.getSystemService(NotificationManager::class.java)
        gestor.createNotificationChannel(
            NotificationChannel(CANAL_AVISOS, "Avisos del camión", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Cuando el camión sale a ruta o está cerca de ti" }
        )
        gestor.createNotificationChannel(
            NotificationChannel(CANAL_SERVICIO, "Vigilando el camión", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Se muestra mientras está activo Avisarme cuando esté cerca" }
        )
    }

    // En Android 13 o más nuevo hay que pedir permiso para mostrar notificaciones
    fun hayPermiso(contexto: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    // Al tocar la notificación se abre la app
    fun abrirApp(contexto: Context): PendingIntent = PendingIntent.getActivity(
        contexto, 0,
        Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE
    )

    @SuppressLint("MissingPermission") // el permiso se revisa con hayPermiso()
    fun mostrar(contexto: Context, id: Int, titulo: String, texto: String) {
        if (!hayPermiso(contexto)) return
        crearCanales(contexto)
        val notificacion = NotificationCompat.Builder(contexto, CANAL_AVISOS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrirApp(contexto))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(contexto).notify(id, notificacion)
        } catch (e: SecurityException) {
            // El usuario quitó el permiso: no se muestra nada y la app sigue
        }
    }
}
