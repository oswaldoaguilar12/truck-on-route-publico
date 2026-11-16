package com.example.truckonroute.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.truckonroute.R
import com.example.truckonroute.data.Estadisticas
import com.example.truckonroute.data.Ubicacion
import com.example.truckonroute.data.UbicacionApi
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Truck on Rute 2.5: mapa de la pantalla de Inicio con la ubicación del camión.
// Cubre R3 (ubicación en un mapa) y R9 (actualización periódica). Ver specs/001-ubicacion-tiempo-real.

private val VerdeTruck = Color(0xFF019B43)
private val VerdeOscuro = Color(0xFF0A7D2C)

private const val CONSULTA_CADA_MS = 10_000L      // la app pregunta a la API cada 10 s
private const val SIN_SENAL_DESPUES_MS = 60_000L  // más de 60 s sin datos nuevos = "Sin señal reciente"
private val CENTRO_HUAUCHINANGO = GeoPoint(20.1767, -98.0527)

// Nombre con el que la app se identifica al descargar las imágenes del mapa
private const val NOMBRE_APP_PARA_MAPA = "TruckOnRute/2.5 (proyecto escolar ITSH Huauchinango)"

// Imágenes del mapa: "World Street Map" de Esri. No necesita clave.
// Antes se probaron OpenStreetMap ("Access blocked") y CARTO ("API key required").
// Esri pide las imágenes en orden zoom/fila/columna (z/y/x), por eso se arma la dirección a mano.
// "Esri-WorldStreetMap" es el nombre de la carpeta donde el celular guarda las imágenes.
private val MAPA_ESRI = object : OnlineTileSourceBase(
    "Esri-WorldStreetMap", 0, 19, 256, "",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"),
    "Esri, HERE, Garmin, © OpenStreetMap contributors"
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
            MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
}

@Composable
fun TruckMapSection() {
    val contexto = LocalContext.current

    // Estado de la pantalla
    var ubicacion by remember { mutableStateOf<Ubicacion?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var recorrido by remember { mutableStateOf<List<Ubicacion>>(emptyList()) } // versión 2.6
    var estadisticas by remember { mutableStateOf<Estadisticas?>(null) }      // versión 2.6
    var seguirCamion by remember { mutableStateOf(true) } // si es false, el usuario movió el mapa a mano

    // Consulta la API cada 10 s mientras esta pantalla está visible.
    // Al salir de Inicio, Compose cancela este ciclo solo.
    LaunchedEffect(Unit) {
        while (true) {
            ubicacion = UbicacionApi.obtenerUltima()
            if (ubicacion != null) {
                recorrido = UbicacionApi.obtenerHistorial()
                estadisticas = UbicacionApi.obtenerEstadisticas()
            }
            cargando = false
            ahora = System.currentTimeMillis()
            delay(CONSULTA_CADA_MS)
        }
    }

    // El mapa de OpenStreetMap (osmdroid) se crea una sola vez
    val mapa = remember { crearMapa(contexto) }
    val marcador = remember {
        Marker(mapa).apply {
            icon = iconoCamion(contexto)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Camión recolector"
        }
    }
    // Versión 2.6: línea verde con la ruta que ya recorrió el camión
    val linea = remember {
        Polyline(mapa).apply {
            outlinePaint.color = android.graphics.Color.parseColor("#019B43")
            outlinePaint.strokeWidth = 10f
        }
    }

    // Libera el mapa cuando se sale de la pantalla
    DisposableEffect(Unit) {
        onDispose { mapa.onDetach() }
    }

    val estado = calcularEstado(ubicacion, cargando, ahora)

    // Versión 2.9: tarjeta con sombra suave y título con ícono
    TarjetaTor {
        Column(Modifier.padding(14.dp)) {
            TituloSeccion("¿Dónde está el camión?", Icons.Default.LocationOn)
            Spacer(Modifier.height(12.dp))

            // AndroidView permite usar el mapa (que es una vista clásica de Android) dentro de Compose.
            // "update" se vuelve a ejecutar cada vez que cambia "ubicacion".
            AndroidView(
                factory = {
                    // Si el usuario arrastra el mapa, deja de seguir al camión hasta que toque "Centrar"
                    mapa.setOnTouchListener { _, evento ->
                        if (evento.action == android.view.MotionEvent.ACTION_MOVE) seguirCamion = false
                        false // false: el mapa sigue recibiendo el gesto normal
                    }
                    mapa
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, VerdeTruck.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                update = { vista ->
                    val u = ubicacion
                    if (u != null) {
                        val punto = GeoPoint(u.lat, u.lng)
                        marcador.position = punto
                        linea.setPoints(recorrido.map { GeoPoint(it.lat, it.lng) })
                        // La línea va debajo del camión
                        if (!vista.overlays.contains(linea)) vista.overlays.add(linea)
                        if (!vista.overlays.contains(marcador)) vista.overlays.add(marcador)
                        if (seguirCamion) vista.controller.animateTo(punto)
                    } else {
                        // Sin ubicación: no se dibuja ningún camión falso
                        vista.overlays.remove(marcador)
                        vista.overlays.remove(linea)
                    }
                    vista.invalidate()
                }
            )

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Versión 2.9: el estado va dentro de una "píldora" del mismo color, pero muy clara
                Row(
                    Modifier.background(estado.color.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(9.dp).background(estado.color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(estado.texto, color = estado.color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.weight(1f))
                // Versión 2.6: botón para volver a centrar el mapa en el camión
                TextButton(onClick = {
                    seguirCamion = true
                    ubicacion?.let { mapa.controller.animateTo(GeoPoint(it.lat, it.lng)) }
                }) {
                    Icon(Icons.Default.MyLocation, null, tint = VerdeOscuro, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Centrar", color = VerdeOscuro, fontWeight = FontWeight.SemiBold)
                }
            }
            // Versión 2.6: velocidad y distancia que calcula la API
            val e = estadisticas
            if (ubicacion != null && e != null) {
                val velocidad = e.velocidadKmh?.let { String.format(Locale.US, "%.1f km/h", it) } ?: "-- km/h"
                val distancia = String.format(Locale.US, "%.2f km", e.distanciaKm)
                // Versión 2.9: los datos van en tres cuadritos en lugar de una línea de texto
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatoMapa("Velocidad", velocidad, Modifier.weight(1f))
                    DatoMapa("Recorrido", distancia, Modifier.weight(1f))
                    DatoMapa("Puntos", "${e.puntosRecibidos}", Modifier.weight(1f))
                }
            }
        }
    }
}

// Versión 2.9: cuadrito con un dato del recorrido (fondo verde muy claro)
@Composable
private fun DatoMapa(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier.background(VerdeTruck.copy(alpha = 0.08f), RoundedCornerShape(14.dp)).padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, fontWeight = FontWeight.Bold, color = VerdeOscuro, fontSize = 14.sp)
        Text(titulo, color = Color.Gray, fontSize = 11.sp)
    }
}

// Crea el mapa centrado en Huauchinango
private fun crearMapa(contexto: Context): MapView {
    // osmdroid pide identificar la app al descargar el mapa
    Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    Configuration.getInstance().userAgentValue = NOMBRE_APP_PARA_MAPA

    return MapView(contexto).apply {
        setTileSource(MAPA_ESRI)
        // Las condiciones de uso piden mostrar el crédito del mapa
        overlays.add(CopyrightOverlay(contexto))
        setMultiTouchControls(true) // zoom con dos dedos
        controller.setZoom(16.0)
        controller.setCenter(CENTRO_HUAUCHINANGO)
    }
}

// Achica camion.png (502 x 304 px) a 56 dp de ancho para usarlo como ícono del mapa
private fun iconoCamion(contexto: Context): Drawable {
    val original = BitmapFactory.decodeResource(contexto.resources, R.drawable.camion)
    val ancho = (56 * contexto.resources.displayMetrics.density).toInt()
    val alto = ancho * original.height / original.width
    return BitmapDrawable(contexto.resources, Bitmap.createScaledBitmap(original, ancho, alto, true))
}

// Texto y color que se muestran debajo del mapa (tabla de estados de data-model.md)
private data class Estado(val texto: String, val color: Color)

private fun calcularEstado(u: Ubicacion?, cargando: Boolean, ahora: Long): Estado {
    if (cargando) return Estado("Buscando camión…", Color.Gray)
    if (u == null) return Estado("Sin ubicación disponible", Color(0xFFB00020))

    val recibida = leerFecha(u.fecha)
    val hora = recibida?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "--:--"
    val antigua = recibida != null && ahora - recibida.time > SIN_SENAL_DESPUES_MS

    return when {
        antigua -> Estado("Sin señal reciente · $hora", Color(0xFFE65100))
        u.fuente == "gps" -> Estado("Ubicación real · $hora", VerdeTruck)
        else -> Estado("Simulación · $hora", Color(0xFF1565C0))
    }
}

// La API manda la fecha así: 2026-10-22T06:38:21.828Z (UTC).
// Se usa SimpleDateFormat porque java.time necesita Android 8 y la app soporta desde Android 7.
private fun leerFecha(texto: String): Date? {
    val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        formato.parse(texto)
    } catch (e: Exception) {
        null
    }
}
