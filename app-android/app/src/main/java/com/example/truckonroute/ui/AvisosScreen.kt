package com.example.truckonroute.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truckonroute.data.Aviso
import com.example.truckonroute.data.Sesion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Versión 2.14 (spec 010, FR-1001 a FR-1004): sección Avisos con historial.
// La usan el ciudadano (desde el menú) y el chofer (desde la campanita).
// Lo leído se guarda solo en el celular: la fecha del aviso más nuevo que ya se vio.

private val AzulTor = Color(0xFF1565C0)

// Ícono y color de cada tipo de aviso (FR-1002)
data class EstiloAviso(val icono: ImageVector, val color: Color)

fun estiloAviso(tipo: String): EstiloAviso = when (tipo) {
    "en_ruta" -> EstiloAviso(Icons.Default.LocalShipping, VerdeTor)
    "desvio" -> EstiloAviso(Icons.Default.Warning, AmbarDesvioTor)
    "retraso" -> EstiloAviso(Icons.Default.Schedule, AmbarDesvioTor)
    "no_pasa" -> EstiloAviso(Icons.Default.Block, RojoTor)
    "general" -> EstiloAviso(Icons.Default.Campaign, AzulTor)
    else -> EstiloAviso(Icons.Default.Notifications, VerdeTor)
}

// La API manda la fecha en UTC: 2026-11-27T09:42:00.000Z
fun fechaDeAviso(iso: String): Date? = try {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(iso)
} catch (e: Exception) { null }

// Cuántos avisos son más nuevos que lo último que se vio
fun contarNoLeidos(avisos: List<Aviso>, leidoHasta: String?): Int =
    avisos.count { leidoHasta == null || it.fecha > leidoHasta }

// "Hoy", "Ayer" o la fecha, para agrupar
private fun dia(iso: String): String {
    val f = fechaDeAviso(iso) ?: return ""
    val c = Calendar.getInstance().apply { time = f }
    val hoy = Calendar.getInstance()
    val mismoDia = { a: Calendar, b: Calendar -> a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR) }
    if (mismoDia(c, hoy)) return "Hoy"
    hoy.add(Calendar.DAY_OF_YEAR, -1)
    if (mismoDia(c, hoy)) return "Ayer"
    return SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX")).format(f).replaceFirstChar { it.uppercase() }
}

private fun hora(iso: String): String = fechaDeAviso(iso)?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: ""

// cargar: pide los avisos de los últimos 7 días (ciudadano o chofer)
// clave: "ciudadano" o "chofer", para guardar lo leído por separado
@Composable
fun AvisosScreen(clave: String, cargar: suspend () -> List<Aviso>, onAtras: () -> Unit) {
    BackHandler(onBack = onAtras)
    val contexto = LocalContext.current
    // Lo leído se toma al abrir: así los nuevos se ven marcados mientras la pantalla está abierta
    val leidoAlAbrir = remember { Sesion.avisosLeidosHasta(contexto, clave) }
    var avisos by remember { mutableStateOf<List<Aviso>?>(null) }
    var soloSinLeer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val lista = cargar().sortedByDescending { it.fecha }
        avisos = lista
        // FR-1003: al abrir la sección, todo queda leído
        lista.firstOrNull()?.let { Sesion.guardarAvisosLeidosHasta(contexto, it.fecha, clave) }
    }

    val todos = avisos.orEmpty()
    val sinLeer = contarNoLeidos(todos, leidoAlAbrir)
    val mostrar = if (soloSinLeer) todos.filter { leidoAlAbrir == null || it.fecha > leidoAlAbrir } else todos

    Column(Modifier.fillMaxSize().background(FondoTor)) {
        EncabezadoVerde {
            Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).background(Color.White.copy(alpha = 0.16f), CircleShape).clickable(onClick = onAtras),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowBack, "Regresar", tint = Color.White) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(if (sinLeer == 1) "1 sin leer" else "$sinLeer sin leer", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("Avisos", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Filtro("Todos", !soloSinLeer) { soloSinLeer = false }
            Filtro("Sin leer · $sinLeer", soloSinLeer) { soloSinLeer = true }
        }
        when {
            avisos == null -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { Esqueleto(Modifier.fillMaxWidth().height(72.dp), radio = 18.dp) }
            }
            mostrar.isEmpty() -> EstadoVacio(
                Icons.Default.NotificationsNone,
                if (soloSinLeer) "Ya leíste todo" else "Sin avisos esta semana",
                "Aquí verás los avisos de los últimos 7 días.",
                Modifier.padding(top = 30.dp)
            )
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var anterior = ""
                mostrar.forEachIndexed { i, a ->
                    val d = dia(a.fecha)
                    if (d != anterior) {
                        anterior = d
                        item(key = "dia-$d") { Text(d, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextoGrisTor, modifier = Modifier.padding(top = 8.dp, start = 4.dp)) }
                    }
                    item(key = a.id) {
                        FilaAvisoHistorial(a, nuevo = leidoAlAbrir == null || a.fecha > leidoAlAbrir, Modifier.entradaSuave(retrasoMs = 30 * minOf(i, 8)))
                    }
                }
                item { Text("Se muestran los avisos de los últimos 7 días", fontSize = 11.sp, color = TextoGrisTor, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            }
        }
    }
}

@Composable
private fun Filtro(texto: String, activo: Boolean, onClick: () -> Unit) {
    Text(
        texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        color = if (activo) Color.White else VerdeOscuroTor,
        modifier = Modifier
            .background(if (activo) VerdeTor else Color.White, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

// Un aviso del historial. Los nuevos llevan fondo verde claro y un punto (FR-1002)
@Composable
private fun FilaAvisoHistorial(a: Aviso, nuevo: Boolean, modifier: Modifier = Modifier) {
    val e = estiloAviso(a.tipo)
    Row(
        modifier.fillMaxWidth().background(if (nuevo) VerdeClaroTor else Color.White, RoundedCornerShape(18.dp)).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.size(40.dp).background(e.color.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(e.icono, null, tint = e.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(a.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuroTor)
            Text(a.mensaje, fontSize = 12.sp, color = TextoGrisTor, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(hora(a.fecha), fontSize = 11.sp, color = TextoGrisTor)
            if (nuevo) {
                Spacer(Modifier.height(14.dp))
                Box(Modifier.size(9.dp).background(VerdeTor, CircleShape))
            }
        }
    }
}
