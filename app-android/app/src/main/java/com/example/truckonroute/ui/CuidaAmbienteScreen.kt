package com.example.truckonroute.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truckonroute.juego.Carta
import com.example.truckonroute.juego.Categoria
import com.example.truckonroute.juego.Residuo
import com.example.truckonroute.juego.estrellas
import com.example.truckonroute.juego.nuevoTablero
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Versión 2.11 (spec 007): sección "Cuida tu ambiente" con el memorama "¿Dónde va?".
// Funciona sin internet: no llama a la API ni guarda nada.

// Las etapas de la pantalla (una sola variable decide qué se ve)
private enum class Etapa { SECCION, INICIO, TABLERO, RESUMEN }

@Composable
fun CuidaAmbienteScreen(onSalir: () -> Unit) {
    var etapa by remember { mutableStateOf(Etapa.SECCION) }
    var partida by remember { mutableIntStateOf(0) } // cambia para empezar un tablero nuevo
    var intentosFinal by remember { mutableIntStateOf(0) }
    var segundosFinal by remember { mutableIntStateOf(0) }
    var residuosFinal by remember { mutableStateOf(listOf<Residuo>()) }

    // El botón atrás del celular regresa a la etapa anterior
    BackHandler {
        when (etapa) {
            Etapa.SECCION -> onSalir()
            Etapa.INICIO -> etapa = Etapa.SECCION
            else -> etapa = Etapa.INICIO
        }
    }

    Box(Modifier.fillMaxSize().background(FondoTor)) {
        when (etapa) {
            Etapa.SECCION -> SeccionAmbiente(onAtras = onSalir, onJuego = { etapa = Etapa.INICIO })
            Etapa.INICIO -> InicioJuego(onAtras = { etapa = Etapa.SECCION }, onJugar = { partida++; etapa = Etapa.TABLERO })
            Etapa.TABLERO -> key(partida) {
                TableroJuego(
                    onAtras = { etapa = Etapa.INICIO },
                    onTerminar = { intentos, segundos, residuos ->
                        intentosFinal = intentos; segundosFinal = segundos; residuosFinal = residuos
                        etapa = Etapa.RESUMEN
                    }
                )
            }
            Etapa.RESUMEN -> ResumenJuego(
                intentos = intentosFinal,
                segundos = segundosFinal,
                residuos = residuosFinal,
                onOtraVez = { partida++; etapa = Etapa.TABLERO },
                onSalir = { etapa = Etapa.SECCION }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// FR-701: la sección con sus actividades (hoy el juego; "Composta en casa" viene después)
// ---------------------------------------------------------------------------
@Composable
private fun SeccionAmbiente(onAtras: () -> Unit, onJuego: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        EncabezadoVerde {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 64.dp)) {
                FilaTitulo("Cuida tu ambiente", "Aprende y ayuda a tu colonia", onAtras)
            }
        }
        Column(
            Modifier.padding(horizontal = 16.dp).offset(y = (-44).dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TarjetaActividad(
                emoji = "♻️",
                titulo = "Juego: ¿Dónde va?",
                texto = "Un memorama para aprender a separar tu basura.",
                habilitada = true,
                onClick = onJuego,
                modifier = Modifier.entradaSuave()
            )
            TarjetaActividad(
                emoji = "🌱",
                titulo = "Composta en casa",
                texto = "Convierte tus restos de comida en abono.",
                habilitada = false,
                onClick = {},
                modifier = Modifier.entradaSuave(retrasoMs = 80)
            )
        }
    }
}

@Composable
private fun TarjetaActividad(emoji: String, titulo: String, texto: String, habilitada: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaccion = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .alPresionar(interaccion)
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = TextoOscuroTor.copy(alpha = 0.12f), spotColor = TextoOscuroTor.copy(alpha = 0.12f))
            .background(Color.White, RoundedCornerShape(24.dp))
            .clickable(interactionSource = interaccion, indication = null, enabled = habilitada, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(58.dp).background(if (habilitada) VerdeClaroTor else Color(0xFFECEFEE), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 30.sp) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (habilitada) TextoOscuroTor else TextoGrisTor)
            Text(texto, fontSize = 13.sp, color = TextoGrisTor)
            if (!habilitada) {
                Spacer(Modifier.height(6.dp))
                EtiquetaEstado("Próximamente", TextoGrisTor, conPunto = false)
            }
        }
        if (habilitada) {
            Box(Modifier.size(40.dp).background(VerdeTor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FR-702: cómo se juega y las 4 categorías
// ---------------------------------------------------------------------------
@Composable
private fun InicioJuego(onAtras: () -> Unit, onJugar: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        EncabezadoVerde {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 60.dp)) {
                FilaTitulo("¿Dónde va?", "Aprende jugando", onAtras)
            }
        }
        Column(
            Modifier
                .weight(1f)
                // el desplazamiento va antes del scroll: si va dentro, el scroll recorta la parte de arriba
                .offset(y = (-40).dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .entradaSuave()
                .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = TextoOscuroTor.copy(alpha = 0.12f), spotColor = TextoOscuroTor.copy(alpha = 0.12f))
                .background(Color.White, RoundedCornerShape(26.dp))
                .padding(18.dp)
        ) {
            Text("Encuentra los pares", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuroTor)
            Text(
                "Voltea dos cartas. Si son iguales, te decimos en qué bote va ese residuo.",
                fontSize = 13.sp, color = TextoGrisTor
            )
            Spacer(Modifier.height(6.dp))
            Categoria.entries.forEachIndexed { i, cat ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(cat.fondo, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Text(cat.emoji, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(cat.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuroTor)
                        Text(cat.ejemplos, fontSize = 12.sp, color = TextoGrisTor)
                    }
                    Box(Modifier.size(14.dp).background(cat.color, CircleShape))
                }
                if (i < Categoria.entries.size - 1) HorizontalDivider(color = Color(0xFFEEF0EE))
            }
        }
        BotonPildora(
            "Jugar", onJugar,
            icono = Icons.Default.PlayArrow,
            alto = 58.dp,
            modifier = Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// FR-703 a FR-708: el tablero
// ---------------------------------------------------------------------------
@Composable
private fun TableroJuego(onAtras: () -> Unit, onTerminar: (Int, Int, List<Residuo>) -> Unit) {
    val cartas = remember { nuevoTablero() }
    val totalPares = cartas.size / 2
    val volteadas = remember { mutableStateListOf<Int>() }        // cartas boca arriba que aún no hacen par
    val encontradas = remember { mutableStateListOf<Residuo>() }  // residuos ya emparejados, en orden
    var intentos by remember { mutableIntStateOf(0) }
    var segundos by remember { mutableIntStateOf(0) }
    var esperando by remember { mutableStateOf(false) }           // FR-707: hay dos cartas distintas a la vista
    var consejo by remember { mutableStateOf<Residuo?>(null) }     // FR-705: tarjeta del par encontrado
    var ultimoConsejo by remember { mutableStateOf<Residuo?>(null) } // se queda para que la tarjeta no salga vacía al cerrarse
    val scope = rememberCoroutineScope()
    val vibrar = LocalHapticFeedback.current

    // FR-708: reloj que avanza mientras falten pares
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (encontradas.size < totalPares) segundos++
        }
    }

    fun tocar(carta: Carta) {
        // FR-707: no se voltea nada mientras hay dos cartas a la vista o el consejo está abierto
        if (esperando || consejo != null) return
        if (carta.id in volteadas || carta.residuo in encontradas) return
        volteadas.add(carta.id)
        if (volteadas.size < 2) return

        intentos++
        val (a, b) = volteadas.map { id -> cartas.first { it.id == id } }
        if (a.residuo == b.residuo) {
            // FR-705: par encontrado
            encontradas.add(a.residuo)
            volteadas.clear()
            vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
            consejo = a.residuo
            ultimoConsejo = a.residuo
        } else {
            // FR-706: no son iguales; regresan boca abajo después de 1 segundo
            esperando = true
            scope.launch {
                delay(1000)
                volteadas.clear()
                esperando = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            EncabezadoVerde {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp)) {
                    FilaTitulo("¿Dónde va?", null, onAtras)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatoJuego("Pares", "${encontradas.size} de $totalPares", Modifier.weight(1f))
                        DatoJuego("Intentos", "$intentos", Modifier.weight(1f))
                        DatoJuego("Tiempo", formatoTiempo(segundos), Modifier.weight(1f))
                    }
                }
            }
            // Cuadrícula de 3 columnas × 4 filas que llena el espacio libre
            Column(
                Modifier.weight(1f).fillMaxWidth().navigationBarsPadding().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                cartas.chunked(3).forEach { fila ->
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        fila.forEach { carta ->
                            CartaMemorama(
                                carta = carta,
                                bocaArriba = carta.id in volteadas || carta.residuo in encontradas,
                                encontrada = carta.residuo in encontradas,
                                onClick = { tocar(carta) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }

        // FR-705: velo oscuro y tarjeta con la categoría y el consejo
        AnimatedVisibility(visible = consejo != null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.fillMaxSize().background(TextoOscuroTor.copy(alpha = 0.35f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            )
        }
        AnimatedVisibility(
            visible = consejo != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ultimoConsejo?.let { r ->
                TarjetaConsejo(r) {
                    consejo = null
                    // FR-709: al cerrar el último consejo se pasa al resumen
                    if (encontradas.size == totalPares) onTerminar(intentos, segundos, encontradas.toList())
                }
            }
        }
    }
}

// FR-704: carta que se voltea girando sobre su eje (280 ms)
@Composable
private fun CartaMemorama(carta: Carta, bocaArriba: Boolean, encontrada: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val giro by animateFloatAsState(if (bocaArriba) 180f else 0f, tween(280), label = "giro")
    val forma = RoundedCornerShape(18.dp)
    Box(
        modifier
            .graphicsLayer { rotationY = giro; cameraDistance = 12f * density }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    ) {
        if (giro <= 90f) {
            // Reverso: verde con un signo de pregunta
            Box(
                Modifier.fillMaxSize()
                    .shadow(6.dp, forma, ambientColor = VerdeOscuroTor.copy(alpha = 0.25f), spotColor = VerdeOscuroTor.copy(alpha = 0.25f))
                    .background(DegradadoVerde, forma),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(44.dp).border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            }
        } else {
            // Frente: se gira otros 180° para que el texto no se vea al revés
            val colorBorde = if (encontrada) carta.residuo.categoria.color else Color(0xFFDDE3DF)
            Column(
                Modifier.fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .shadow(6.dp, forma, ambientColor = TextoOscuroTor.copy(alpha = 0.10f), spotColor = TextoOscuroTor.copy(alpha = 0.10f))
                    .background(Color.White, forma)
                    .border(3.dp, colorBorde, forma)
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(carta.residuo.emoji, fontSize = 36.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    carta.residuo.nombre, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = TextoOscuroTor, textAlign = TextAlign.Center, lineHeight = 14.sp, maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun TarjetaConsejo(residuo: Residuo, onSeguir: () -> Unit) {
    val cat = residuo.categoria
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(12.dp)
            .shadow(24.dp, RoundedCornerShape(28.dp))
            .background(Color.White, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(60.dp).background(cat.fondo, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Text(residuo.emoji, fontSize = 34.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("¡Par encontrado!", fontSize = 13.sp, color = TextoGrisTor)
                Text(residuo.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor)
            }
        }
        Spacer(Modifier.height(14.dp))
        EtiquetaEstado(cat.nombre, cat.color, solida = true, conPunto = false, modifier = Modifier.rebote(residuo))
        Spacer(Modifier.height(10.dp))
        Text(residuo.consejo, fontSize = 14.sp, color = TextoOscuroTor, lineHeight = 21.sp)
        Spacer(Modifier.height(16.dp))
        BotonPildora("Seguir jugando", onSeguir, alto = 52.dp)
    }
}

// ---------------------------------------------------------------------------
// FR-709: resumen con estrellas y los residuos por categoría
// ---------------------------------------------------------------------------
@Composable
private fun ResumenJuego(intentos: Int, segundos: Int, residuos: List<Residuo>, onOtraVez: () -> Unit, onSalir: () -> Unit) {
    val ganadas = estrellas(intentos)
    Column(Modifier.fillMaxSize()) {
        EncabezadoVerde {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 46.sp, modifier = Modifier.rebote(Unit))
                Text("¡Lo lograste!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Ya sabes dónde van ${residuos.size} residuos", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
        }
        Column(
            Modifier
                .weight(1f)
                // el desplazamiento va antes del scroll: si va dentro, el scroll recorta la parte de arriba
                .offset(y = (-54).dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .entradaSuave()
                .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = TextoOscuroTor.copy(alpha = 0.12f), spotColor = TextoOscuroTor.copy(alpha = 0.12f))
                .background(Color.White, RoundedCornerShape(26.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                repeat(3) { i ->
                    Text(
                        "★", fontSize = 46.sp,
                        color = if (i < ganadas) Color(0xFFF5B700) else Color(0xFFE3E8E5),
                        modifier = Modifier.padding(horizontal = 4.dp).entradaSuave(retrasoMs = 150 * i)
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
                DatoResumen("$intentos", "intentos")
                DatoResumen(formatoTiempo(segundos), "tiempo")
            }
            Text("Con 9 intentos o menos ganas 3 estrellas", fontSize = 12.sp, color = TextoGrisTor, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEF0EE))
            Spacer(Modifier.height(6.dp))
            // Los residuos de esta partida, agrupados por su bote
            Categoria.entries.forEach { cat ->
                val deEsta = residuos.filter { it.categoria == cat }
                if (deEsta.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        EtiquetaEstado(cat.nombre, cat.color, solida = true, conPunto = false, modifier = Modifier.width(150.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(deEsta.joinToString("  ") { it.emoji }, fontSize = 24.sp)
                    }
                }
            }
        }
        Column(
            Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotonPildora("Jugar otra vez", onOtraVez, icono = Icons.Default.Refresh)
            BotonBorde("Salir", onSalir)
        }
    }
}

// ---------------------------------------------------------------------------
// Piezas pequeñas
// ---------------------------------------------------------------------------

// Flecha para regresar + título (y subtítulo opcional) sobre el encabezado verde
@Composable
private fun FilaTitulo(titulo: String, subtitulo: String?, onAtras: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp).background(Color.White.copy(alpha = 0.16f), CircleShape).clickable(onClick = onAtras),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ArrowBack, "Regresar", tint = Color.White) }
        Spacer(Modifier.width(12.dp))
        Column {
            if (subtitulo != null) Text(subtitulo, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            Text(titulo, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Cuadrito transparente del encabezado del tablero (Pares, Intentos, Tiempo)
@Composable
private fun DatoJuego(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(titulo, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        Text(valor, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DatoResumen(valor: String, texto: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor)
        Text(texto, fontSize = 12.sp, color = TextoGrisTor)
    }
}

// 92 segundos -> "1:32"
private fun formatoTiempo(segundos: Int): String = "%d:%02d".format(segundos / 60, segundos % 60)
