package com.example.truckonroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextDecoration
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

    // Versión 2.10 (spec 006): encabezado verde con saludo y "Hoy te toca", y la tarjeta del recorrido encimada.
    // El recorrido principal es el que está en curso o, si no hay, el siguiente programado.
    val principal = recorridos.firstOrNull { it.estado == "en_curso" }
        ?: recorridos.firstOrNull { it.estado == "programado" }
        ?: recorridos.firstOrNull()

    Column(Modifier.fillMaxSize().background(FondoTor).verticalScroll(rememberScrollState())) {
        EncabezadoVerde {
            Column(Modifier.padding(start = 22.dp, end = 14.dp, top = 20.dp, bottom = 72.dp).entradaSuave(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Text(usuario.nombre.take(1).uppercase(), color = VerdeOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(saludo(), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(usuario.nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.background(Color.White.copy(alpha = 0.16f), CircleShape)) {
                        Icon(Icons.Default.Logout, "Cerrar sesión", tint = Color.White)
                    }
                }
                Column {
                    Text(if (principal != null) "Hoy te toca" else "Chofer · Truck on Route", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text(principal?.ruta ?: "Sin recorrido", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().offset(y = (-48).dp).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                cargando -> TarjetaTor {
                    // FR-614: esqueleto de la tarjeta mientras llega el recorrido
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Esqueleto(Modifier.fillMaxWidth(0.6f).height(20.dp))
                        Esqueleto(Modifier.fillMaxWidth(0.4f).height(14.dp))
                        repeat(3) { Esqueleto(Modifier.fillMaxWidth().height(30.dp)) }
                        Esqueleto(Modifier.fillMaxWidth().height(60.dp), radio = 30.dp)
                    }
                }
                recorridos.isEmpty() -> TarjetaTor {
                    EstadoVacio(Icons.Default.LocalShipping, "Sin recorridos por ahora", "El administrador te los asigna desde el panel.", Modifier.padding(horizontal = 16.dp))
                }
                else -> recorridos.forEachIndexed { i, rec ->
                    Box(Modifier.entradaSuave(retrasoMs = 80 + i * 60)) {
                        TarjetaRecorrido(rec, ocupado, onIniciar = { cambiar(rec, "iniciar") }, onTerminar = { cambiar(rec, "terminar") })
                    }
                }
            }
            error?.let { Text(it, color = Color(0xFFB00020)) }

            // Mensajes del administrador
            TarjetaTor(Modifier.entradaSuave(retrasoMs = 160)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(VerdeClaroTor, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChatBubbleOutline, null, tint = VerdeOscuro, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Mensajes del administrador", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuroTor)
                            if (mensajes.isEmpty()) Text("Todo tranquilo, sin mensajes nuevos", fontSize = 12.sp, color = TextoGrisTor)
                        }
                    }
                    mensajes.forEach { m ->
                        Column(Modifier.fillMaxWidth().background(FondoTor, RoundedCornerShape(14.dp)).padding(12.dp)) {
                            Text(horaCorta(m.fecha), fontSize = 11.sp, color = TextoGrisTor)
                            Text(m.texto, fontSize = 14.sp, color = TextoOscuroTor)
                        }
                    }
                }
            }
        }
    }
}

// Saludo según la hora del celular
private fun saludo(): String {
    val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hora) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

@Composable
private fun TarjetaRecorrido(rec: Recorrido, ocupado: Boolean, onIniciar: () -> Unit, onTerminar: () -> Unit) {
    val (textoEstado, colorEstado) = when (rec.estado) {
        "en_curso" -> "En ruta" to Verde
        "terminado" -> "Terminado" to Color.Gray
        else -> "Programado" to Color(0xFFB45309)
    }
    // Versión 2.10: tarjeta grande con esquinas de 26 dp, colonias numeradas y botón tipo píldora
    TarjetaTor {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(rec.ruta, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextoOscuroTor)
                    val horario = if (rec.horario.isNotBlank()) " · ${rec.horario}" else ""
                    Text("Camión ${rec.camion}$horario", fontSize = 13.sp, color = TextoGrisTor)
                    Text(fechaCorta(rec.fecha), fontSize = 12.sp, color = TextoGrisTor)
                }
                EtiquetaEstado(textoEstado, colorEstado)
            }

            // Versión 2.10 (FR-617): con la ruta en curso, el chofer toca cada colonia al terminarla
            // y una barrita muestra cuántas lleva. Es solo en su pantalla (no se guarda en la API).
            val enCurso = rec.estado == "en_curso"
            var hechas by rememberSaveable(rec.id) { mutableIntStateOf(0) }
            val total = rec.colonias.size
            if (enCurso && total > 0) {
                val avance by animateFloatAsState(hechas.toFloat() / total, label = "avance")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text("Vas en", fontSize = 13.sp, color = TextoGrisTor, modifier = Modifier.weight(1f))
                        Text("$hechas de $total colonias", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VerdeOscuro)
                    }
                    LinearProgressIndicator(
                        progress = { avance },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                        color = Verde,
                        trackColor = VerdeClaroTor
                    )
                    Text("Toca cada colonia cuando la termines", fontSize = 12.sp, color = TextoGrisTor)
                }
            }

            // Colonias en orden. Terminada = palomita; la que sigue = verde fuerte; las demás = verde claro
            Column {
                rec.colonias.forEachIndexed { i, c ->
                    val terminada = enCurso && i < hechas
                    val actual = if (enCurso) i == hechas else i == 0
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .then(if (enCurso) Modifier.clickable { hechas = if (i + 1 == hechas) i else i + 1 } else Modifier)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(30.dp).background(if (terminada || actual) Verde else VerdeClaroTor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (terminada) Icon(Icons.Default.Check, "Terminada", tint = Color.White, modifier = Modifier.size(18.dp))
                            else Text("${i + 1}", fontSize = 13.sp, color = if (actual) Color.White else VerdeOscuro, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            c, fontSize = 15.sp,
                            fontWeight = if (actual) FontWeight.Medium else FontWeight.Normal,
                            color = if (terminada) TextoGrisTor else TextoOscuroTor,
                            textDecoration = if (terminada) TextDecoration.LineThrough else null
                        )
                    }
                }
            }

            // Indicaciones del administrador: solo las ve el chofer
            if (rec.indicaciones.isNotBlank()) {
                Row(Modifier.fillMaxWidth().background(AmarilloFondoTor, RoundedCornerShape(16.dp)).padding(12.dp)) {
                    Icon(Icons.Default.Info, null, tint = AmarilloTextoTor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(rec.indicaciones, fontSize = 13.sp, color = TextoOscuroTor)
                }
            }

            when (rec.estado) {
                "programado" -> BotonPildora("Iniciar ruta", onIniciar, icono = Icons.Default.PlayArrow, habilitado = !ocupado, alto = 60.dp)
                "en_curso" -> BotonPildora("Terminar ruta", onTerminar, color = RojoTor, icono = Icons.Default.Stop, habilitado = !ocupado, alto = 60.dp)
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
