package com.example.truckonroute.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import com.example.truckonroute.avisos.Permisos
import com.example.truckonroute.avisos.Vigilancia
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.truckonroute.R
import com.example.truckonroute.data.ApiTor
import com.example.truckonroute.data.Estadisticas
import com.example.truckonroute.data.PuntoRuta
import com.example.truckonroute.data.RecorridoEnCurso
import com.example.truckonroute.data.Ubicacion
import com.example.truckonroute.data.UbicacionApi
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Truck on Rute 2.5: mapa de la pantalla de Inicio con la ubicación del camión.
// Cubre R3 (ubicación en un mapa) y R9 (actualización periódica). Ver specs/001-ubicacion-tiempo-real.

private val VerdeTruck = Color(0xFF019B43)

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

// Versión 2.10 (spec 006): lo que antes hacía TruckMapSection() se divide en dos partes:
// 1) rememberDatosCamion(): pregunta a la API cada 10 s (igual que antes).
// 2) MapaCamion(): solo dibuja el mapa, ahora a pantalla completa en Inicio.
// Así la hoja inferior de Inicio puede mostrar los mismos datos sin volver a pedirlos.

// Todo lo que se sabe del camión en este momento
data class DatosCamion(
    val ubicacion: Ubicacion?,
    val cargando: Boolean,
    val ahora: Long,
    val recorrido: List<Ubicacion>,      // versión 2.6
    val estadisticas: Estadisticas?,     // versión 2.6
    val enCurso: RecorridoEnCurso? = null // versión 2.13: camino dibujado, avance y desvío
)

// camion: si se da (pantalla del chofer), se sigue a ese camión; si no, al último que mandó ubicación
@Composable
fun rememberDatosCamion(camion: String? = null): DatosCamion {
    var ubicacion by remember { mutableStateOf<Ubicacion?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var recorrido by remember { mutableStateOf<List<Ubicacion>>(emptyList()) }
    var estadisticas by remember { mutableStateOf<Estadisticas?>(null) }
    var enCurso by remember { mutableStateOf<RecorridoEnCurso?>(null) }

    // Consulta la API cada 10 s mientras Inicio está visible.
    // Al salir de Inicio, Compose cancela este ciclo solo.
    LaunchedEffect(camion) {
        while (true) {
            ubicacion = if (camion != null) ApiTor.ubicacionDe(camion) else UbicacionApi.obtenerUltima()
            if (ubicacion != null) {
                recorrido = UbicacionApi.obtenerHistorial()
                estadisticas = UbicacionApi.obtenerEstadisticas()
            }
            // v2.13 (FR-904): el recorrido en curso de ese camión trae el camino y cuánto lleva
            val clave = camion ?: ubicacion?.camion
            val lista = ApiTor.recorridosEnCurso()
            enCurso = lista.firstOrNull { it.camion == clave } ?: lista.firstOrNull()
            cargando = false
            ahora = System.currentTimeMillis()
            delay(CONSULTA_CADA_MS)
        }
    }
    return DatosCamion(ubicacion, cargando, ahora, recorrido, estadisticas, enCurso)
}

// Guarda el último punto donde se dibujó el camión (no es estado de Compose: no redibuja la pantalla)
private class UltimoPunto(var punto: GeoPoint? = null)

// El mapa de OpenStreetMap (osmdroid) con el camión y la línea de su recorrido.
// seguirCamion: si es true, el mapa se mueve junto con el camión.
// onMoverMapa: se llama cuando el usuario arrastra el mapa con el dedo.
@Composable
fun MapaCamion(
    datos: DatosCamion,
    seguirCamion: Boolean,
    onMoverMapa: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val alMover by rememberUpdatedState(onMoverMapa)

    // El mapa se crea una sola vez
    val mapa = remember { crearMapa(contexto) }
    val marcador = remember {
        Marker(mapa).apply {
            icon = iconoCamion(contexto)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER) // v2.10: el círculo va centrado en el punto
            title = "Camión recolector"
        }
    }
    // Versión 2.6: línea verde con la ruta que ya recorrió el camión
    val linea = remember {
        Polyline(mapa).apply {
            outlinePaint.color = android.graphics.Color.parseColor("#019B43")
            outlinePaint.strokeWidth = 12f
            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
        }
    }
    // Versión 2.13 (FR-906, FR-909): camino dibujado. Gris = ya pasó, verde = falta, ámbar punteado = desvío
    val densidad = contexto.resources.displayMetrics.density
    val faltaLinea = remember { lineaDeRuta(mapa, "#019B43", 6f * densidad) }
    val pasoLinea = remember { lineaDeRuta(mapa, "#9AA3A0", 6f * densidad) }
    val desvioLinea = remember {
        lineaDeRuta(mapa, "#E8A317", 5f * densidad).apply {
            outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f * densidad, 7f * densidad), 0f)
        }
    }
    val ultimo = remember { UltimoPunto() }
    // Versión 2.10: pulso verde debajo del camión (FR-611) y punto azul con tu ubicación (FR-612)
    val pulso = remember { CirculoEnMapa(contexto.resources.displayMetrics.density, android.graphics.Color.parseColor("#019B43"), 23f, false).apply { esPulso = true } }
    val yo = remember { CirculoEnMapa(contexto.resources.displayMetrics.density, android.graphics.Color.parseColor("#1565C0"), 7f, true) }

    // El pulso crece y se desvanece cada 1.5 s
    LaunchedEffect(Unit) {
        while (true) {
            pulso.fase = (pulso.fase + 0.027f) % 1f
            if (pulso.punto != null) mapa.invalidate()
            delay(40)
        }
    }
    // Tu ubicación: solo si diste permiso (se activa con "Avisarme cuando esté cerca").
    // Se usa la última ubicación que conoce el celular, igual que el aviso de cercanía.
    LaunchedEffect(datos.ahora) {
        yo.punto = if (Permisos.hayUbicacion(contexto)) {
            Vigilancia.ubicacionDelCelular(contexto)?.let { GeoPoint(it.latitude, it.longitude) }
        } else null
        mapa.invalidate()
    }

    // Libera el mapa cuando se sale de la pantalla
    DisposableEffect(Unit) {
        onDispose { mapa.onDetach() }
    }

    // FR-604: cuando llega una ubicación nueva, el camión se desliza en 1 s en lugar de saltar
    LaunchedEffect(datos.ubicacion) {
        val u = datos.ubicacion ?: return@LaunchedEffect
        val destino = GeoPoint(u.lat, u.lng)
        val inicio = ultimo.punto
        ultimo.punto = destino
        if (seguirCamion) mapa.controller.animateTo(destino)
        if (inicio == null) {
            marcador.position = destino // primera vez: se pone directo
            pulso.punto = destino
            mapa.invalidate()
        } else {
            animate(0f, 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing)) { t, _ ->
                marcador.position = GeoPoint(
                    inicio.latitude + (destino.latitude - inicio.latitude) * t,
                    inicio.longitude + (destino.longitude - inicio.longitude) * t
                )
                pulso.punto = marcador.position
                mapa.invalidate()
            }
        }
    }

    // Botón "Centrar": cuando vuelve a seguir al camión, el mapa regresa a él
    LaunchedEffect(seguirCamion) {
        if (seguirCamion) ultimo.punto?.let { mapa.controller.animateTo(it) }
    }

    // AndroidView permite usar el mapa (que es una vista clásica de Android) dentro de Compose.
    // "update" se vuelve a ejecutar cada vez que cambian los datos.
    AndroidView(
        factory = {
            // Si el usuario arrastra el mapa, deja de seguir al camión hasta que toque "Centrar"
            mapa.setOnTouchListener { _, evento ->
                if (evento.action == android.view.MotionEvent.ACTION_MOVE) alMover()
                false // false: el mapa sigue recibiendo el gesto normal
            }
            mapa
        },
        modifier = modifier,
        update = { vista ->
            val u = datos.ubicacion
            // v2.13: el camino dibujado va hasta abajo, debajo del camión
            val rec = datos.enCurso
            val trazo = rec?.trazo.orEmpty()
            val geo = { lista: List<PuntoRuta> -> lista.map { GeoPoint(it.lat, it.lng) } }
            val paso = if (rec != null && rec.avance > 0) geo(trazo.take(rec.avance)) else emptyList()
            val falta = geo(trazo.drop(maxOf(0, (rec?.avance ?: 0) - 1)))
            val desvio = geo(rec?.desvio.orEmpty())
            // Se quitan y se vuelven a poner en orden: verde, gris y encima el desvío
            vista.overlays.removeAll(listOf(faltaLinea, pasoLinea, desvioLinea))
            var lugar = 1 // 0 es el crédito del mapa
            for ((l, puntos) in listOf(faltaLinea to falta, pasoLinea to paso, desvioLinea to desvio)) {
                l.setPoints(puntos)
                if (puntos.size >= 2) vista.overlays.add(lugar++, l)
            }
            if (u != null) {
                // FR-907: con camino dibujado, el historial verde ya no se dibuja (el gris lo reemplaza)
                linea.setPoints(if (trazo.isEmpty()) ultimaVuelta(datos.recorrido).map { GeoPoint(it.lat, it.lng) } else emptyList())
                if (ultimo.punto == null) {
                    marcador.position = GeoPoint(u.lat, u.lng)
                    pulso.punto = marcador.position
                }
                // Orden de abajo hacia arriba: línea, tu ubicación, pulso y camión
                if (!vista.overlays.contains(linea)) vista.overlays.add(linea)
                if (!vista.overlays.contains(yo)) vista.overlays.add(yo)
                if (!vista.overlays.contains(pulso)) vista.overlays.add(pulso)
                if (!vista.overlays.contains(marcador)) vista.overlays.add(marcador)
            } else {
                // Sin ubicación: no se dibuja ningún camión falso
                vista.overlays.remove(marcador)
                vista.overlays.remove(pulso)
                vista.overlays.remove(linea)
                pulso.punto = null
            }
            vista.invalidate()
        }
    )
}

// v2.13: línea gruesa con puntas redondas para el camino dibujado
private fun lineaDeRuta(mapa: MapView, color: String, grosor: Float) = Polyline(mapa).apply {
    outlinePaint.color = android.graphics.Color.parseColor(color)
    outlinePaint.strokeWidth = grosor
    outlinePaint.strokeCap = Paint.Cap.ROUND
    outlinePaint.strokeJoin = Paint.Join.ROUND
    setOnClickListener { _, _, _ -> false } // no abre globos al tocarla
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
        // Versión 2.10: sin los botones + y - (quedaban detrás de la hoja); el zoom es con dos dedos
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(16.0)
        controller.setCenter(CENTRO_HUAUCHINANGO)
    }
}

// Versión 2.10 (FR-611): el marcador es el camión blanco dentro de un círculo verde con borde blanco
// (antes era la foto camion.png). Se dibuja una sola vez en un Bitmap.
private fun iconoCamion(contexto: Context): Drawable {
    val densidad = contexto.resources.displayMetrics.density
    val lado = (46 * densidad).toInt()
    val mapa = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
    val lienzo = android.graphics.Canvas(mapa)
    val centro = lado / 2f
    val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
    pintura.color = android.graphics.Color.WHITE
    lienzo.drawCircle(centro, centro, centro, pintura)
    pintura.color = android.graphics.Color.parseColor("#019B43")
    lienzo.drawCircle(centro, centro, centro - 3 * densidad, pintura)
    val camion = contexto.getDrawable(R.drawable.ic_camion_blanco)!!
    val margen = (11 * densidad).toInt()
    camion.setBounds(margen, margen, lado - margen, lado - margen)
    camion.draw(lienzo)
    return BitmapDrawable(contexto.resources, mapa)
}

// FR-611 y FR-612: círculo que se dibuja sobre el mapa en un punto.
// Sirve para el "pulso" verde del camión (crece y se desvanece) y para el punto azul de tu ubicación.
private class CirculoEnMapa(
    private val densidad: Float,
    private val color: Int,
    private val radioDp: Float,
    private val conBorde: Boolean
) : Overlay() {
    var punto: GeoPoint? = null
    var fase = 0f // 0 a 1: qué tanto ha crecido el pulso (solo si es pulso)
    var esPulso = false
    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)

    // osmdroid llama a uno u otro según la versión; los dos terminan en dibujar()
    override fun draw(c: android.graphics.Canvas, vista: MapView, sombra: Boolean) {
        if (!sombra) dibujar(c, vista.projection)
    }

    override fun draw(c: android.graphics.Canvas, proyeccion: Projection) {
        dibujar(c, proyeccion)
    }

    private fun dibujar(c: android.graphics.Canvas, proyeccion: Projection) {
        val p = punto ?: return
        val px = proyeccion.toPixels(p, null)
        val d = densidad
        val x = px.x.toFloat()
        val y = px.y.toFloat()
        if (esPulso) {
            pintura.color = color
            pintura.alpha = ((1f - fase) * 120).toInt()
            c.drawCircle(x, y, radioDp * d * (1f + fase * 1.4f), pintura)
        } else {
            // Halo claro, borde blanco y punto de color
            pintura.color = color
            pintura.alpha = 50
            c.drawCircle(x, y, radioDp * d * 2.4f, pintura)
            pintura.color = android.graphics.Color.WHITE
            pintura.alpha = 255
            if (conBorde) c.drawCircle(x, y, (radioDp + 3f) * d, pintura)
            pintura.color = color
            c.drawCircle(x, y, radioDp * d, pintura)
        }
    }
}

// Estado del camión (tabla de estados de data-model.md).
// Versión 2.10 (FR-603): se muestra como etiqueta de color ("En ruta", "Simulación", "Sin señal")
// y con un título grande en la hoja de Inicio.
data class EstadoCamion(val etiqueta: String, val titulo: String, val hora: String?, val color: Color)

fun calcularEstado(u: Ubicacion?, cargando: Boolean, ahora: Long): EstadoCamion {
    if (cargando) return EstadoCamion("Buscando…", "Buscando camión…", null, Color.Gray)
    if (u == null) return EstadoCamion("Sin ubicación", "Sin ubicación disponible", null, Color(0xFFB00020))

    val recibida = leerFecha(u.fecha)
    val hora = recibida?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "--:--"
    val antigua = recibida != null && ahora - recibida.time > SIN_SENAL_DESPUES_MS

    return when {
        antigua -> EstadoCamion("Sin señal", "Sin señal reciente", hora, Color(0xFFE65100))
        u.fuente == "gps" -> EstadoCamion("En ruta", "El camión va en ruta", hora, VerdeTruck)
        else -> EstadoCamion("Simulación", "Simulación en curso", hora, Color(0xFF1565C0))
    }
}

// Sin recorrido en curso solo se dibuja la última vuelta del camión, para que no se encimen
// las líneas viejas. Se corta hacia atrás donde hay un hueco de más de 2 minutos sin datos
// o un salto de más de 300 m (cuando la simulación vuelve a empezar).
fun ultimaVuelta(puntos: List<Ubicacion>): List<Ubicacion> {
    if (puntos.size < 2) return puntos
    var inicio = puntos.size - 1
    while (inicio > 0) {
        val a = puntos[inicio - 1]
        val b = puntos[inicio]
        val ta = leerFecha(a.fecha)?.time
        val tb = leerFecha(b.fecha)?.time
        val hueco = ta != null && tb != null && tb - ta > 2 * 60_000
        val salto = FloatArray(1).also { android.location.Location.distanceBetween(a.lat, a.lng, b.lat, b.lng, it) }[0] > 300f
        if (hueco || salto) break
        inicio--
    }
    return puntos.subList(inicio, puntos.size)
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
