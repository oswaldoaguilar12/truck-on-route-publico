package com.example.truckonroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truckonroute.data.ApiTor
import com.example.truckonroute.data.Mensaje
import com.example.truckonroute.data.Recorrido
import com.example.truckonroute.data.Sesion
import com.example.truckonroute.data.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Versión 2.8 (spec 004, H18): lo que ve el chofer.
// Su recorrido (camión, ruta, colonias, horario e indicaciones del administrador),
// los botones Iniciar ruta / Terminar ruta y los mensajes del administrador.

private val Verde = Color(0xFF019B43)
private val VerdeOscuro = Color(0xFF0A7D2C)
private val Fondo = Color(0xFFEFEFEF)
private const val CONSULTA_CADA_MS = 20_000L

@Composable
fun ChoferScreen(usuario: Usuario, onLogout: () -> Unit) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val token = remember { Sesion.token(contexto) ?: "" }

    var recorridos by remember { mutableStateOf<List<Recorrido>>(emptyList()) }
    var mensajes by remember { mutableStateOf<List<Mensaje>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    var recargar by remember { mutableIntStateOf(0) }

    // Pide recorridos y mensajes al abrir y cada 20 s
    LaunchedEffect(recargar) {
        while (true) {
            val (lista, r) = ApiTor.misRecorridos(token)
            if (r.codigo == 401) { onLogout(); return@LaunchedEffect } // la sesión venció
            error = if (r.ok) null else r.error
            if (r.ok) recorridos = lista
            mensajes = ApiTor.misMensajes(token)
            cargando = false
            delay(CONSULTA_CADA_MS)
        }
    }

    fun cambiar(rec: Recorrido, accion: String) {
        alcance.launch {
            ocupado = true
            val r = ApiTor.cambiarRecorrido(token, rec.id, accion)
            error = r.error
            ocupado = false
            recargar++
        }
    }

    Column(Modifier.fillMaxSize().background(Fondo)) {
        // Encabezado con el mismo verde de la app
        // Versión 2.9: con sombra, esquinas más redondas, burbujas y avatar con la inicial
        val forma = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
        Box(
            Modifier.fillMaxWidth()
                .shadow(12.dp, forma, ambientColor = VerdeOscuro.copy(alpha = 0.35f), spotColor = VerdeOscuro.copy(alpha = 0.35f))
                .clip(forma)
                .background(Brush.horizontalGradient(listOf(Verde, VerdeOscuro)))
        ) {
            BurbujasEncabezado()
            Row(Modifier.padding(top = 40.dp, bottom = 22.dp, start = 20.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(usuario.nombre.take(1).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Hola, ${usuario.nombre}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Chofer · Truck on Route", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                }
                IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Cerrar sesión", tint = Color.White) }
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            error?.let { Text(it, color = Color(0xFFB00020)) }

            TituloSeccion("Mi recorrido", Icons.Default.LocalShipping)
            when {
                cargando -> CircularProgressIndicator(color = Verde)
                recorridos.isEmpty() -> TarjetaSimple("Todavía no tienes recorridos asignados. El administrador los asigna desde el panel.")
                else -> recorridos.forEach { rec -> TarjetaRecorrido(rec, ocupado, onIniciar = { cambiar(rec, "iniciar") }, onTerminar = { cambiar(rec, "terminar") }) }
            }

            TituloSeccion("Mensajes del administrador", Icons.Default.Email)
            if (mensajes.isEmpty()) TarjetaSimple("Sin mensajes.")
            mensajes.forEach { MessageItem(horaCorta(it.fecha), it.texto) }
        }
    }
}

@Composable
private fun TarjetaRecorrido(rec: Recorrido, ocupado: Boolean, onIniciar: () -> Unit, onTerminar: () -> Unit) {
    val (textoEstado, colorEstado) = when (rec.estado) {
        "en_curso" -> "En ruta" to Verde
        "terminado" -> "Terminado" to Color.Gray
        else -> "Programado" to Color(0xFFB45309)
    }
    // Versión 2.9: tarjeta con sombra suave
    TarjetaTor {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCirculo(Icons.Default.LocalShipping)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(rec.ruta, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Camión ${rec.camion} · ${fechaCorta(rec.fecha)}", fontSize = 13.sp, color = Color.Gray)
                }
                Text(textoEstado, color = colorEstado, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(colorEstado.copy(alpha = 0.12f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
            if (rec.horario.isNotBlank()) Text("Horario: ${rec.horario}", fontSize = 14.sp)

            HorizontalDivider(color = Fondo)
            Text("Colonias en orden", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VerdeOscuro)
            // Versión 2.9: las colonias se unen con una línea, como paradas de una ruta
            rec.colonias.forEachIndexed { i, c ->
                Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (rec.colonias.size > 1) {
                            Box(
                                Modifier.width(2.dp).fillMaxHeight(if (i == 0 || i == rec.colonias.lastIndex) 0.5f else 1f)
                                    .align(if (i == 0) Alignment.BottomCenter else if (i == rec.colonias.lastIndex) Alignment.TopCenter else Alignment.Center)
                                    .background(Verde.copy(alpha = 0.3f))
                            )
                        }
                        Box(Modifier.size(24.dp).background(DegradadoVerde, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(c, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            // Indicaciones del administrador: solo las ve el chofer
            if (rec.indicaciones.isNotBlank()) {
                Row(
                    Modifier.fillMaxWidth().background(Color(0xFFFEF3C7), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFB45309).copy(alpha = 0.25f), RoundedCornerShape(14.dp)).padding(12.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFFB45309))
                    Spacer(Modifier.width(8.dp))
                    Text(rec.indicaciones, fontSize = 14.sp)
                }
            }

            when (rec.estado) {
                "programado" -> Button(
                    onClick = onIniciar, enabled = !ocupado, modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Iniciar ruta", fontWeight = FontWeight.Bold)
                }
                "en_curso" -> Button(
                    onClick = onTerminar, enabled = !ocupado, modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Stop, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Terminar ruta", fontWeight = FontWeight.Bold)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun TarjetaSimple(texto: String) {
    TarjetaTor {
        Text(texto, Modifier.padding(16.dp), color = Color.Gray)
    }
}

// La API manda fechas en UTC (2026-11-11T17:20:00.000Z); se muestran en la hora del celular
private fun leer(iso: String): java.util.Date? {
    val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return try { f.parse(iso) } catch (e: Exception) { null }
}

private fun horaCorta(iso: String) = leer(iso)?.let { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(it) } ?: ""
private fun fechaCorta(iso: String) = leer(iso)?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it) } ?: ""
