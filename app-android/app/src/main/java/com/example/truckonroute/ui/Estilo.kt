package com.example.truckonroute.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Versión 2.9 (spec 005): piezas de estilo para que la app no se vea plana.
// Usan SOLO los colores que ya tenía la app (verde, verde oscuro, blanco y grises),
// con sombras y transparencias para dar profundidad.

val VerdeTor = Color(0xFF019B43)
val VerdeOscuroTor = Color(0xFF0A7D2C)

// Degradado de los dos verdes de la app (el mismo del encabezado)
val DegradadoVerde = Brush.linearGradient(listOf(VerdeTor, VerdeOscuroTor))

// Sombra suave con tono verde, en lugar de la sombra gris plana
fun Modifier.sombraSuave(radio: Dp = 20.dp, altura: Dp = 10.dp): Modifier = this.shadow(
    elevation = altura,
    shape = RoundedCornerShape(radio),
    ambientColor = VerdeOscuroTor.copy(alpha = 0.18f),
    spotColor = VerdeOscuroTor.copy(alpha = 0.22f)
)

// Tarjeta blanca con esquinas de 20 dp y sombra suave
@Composable
fun TarjetaTor(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().sombraSuave(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp), // la sombra la pone sombraSuave()
        content = content
    )
}

// Título de sección con su ícono en un círculo verde
@Composable
fun TituloSeccion(texto: String, icono: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(30.dp).shadow(4.dp, CircleShape).background(DegradadoVerde, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VerdeOscuroTor)
    }
}

// Ícono dentro de un círculo verde claro con borde suave (para avisos y mensajes)
@Composable
fun IconoCirculo(icono: ImageVector, tamano: Dp = 44.dp) {
    Box(
        Modifier.size(tamano)
            .background(Brush.linearGradient(listOf(VerdeTor.copy(alpha = 0.20f), VerdeTor.copy(alpha = 0.06f))), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, null, tint = VerdeTor, modifier = Modifier.size(tamano * 0.5f))
    }
}

// Fondo con dos círculos verdes muy transparentes (detrás del login, registro, etc.)
@Composable
fun FondoDecorado(colorFondo: Color, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(colorFondo)) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(VerdeTor.copy(alpha = 0.10f), radius = size.width * 0.55f, center = Offset(size.width * 1.0f, 0f))
            drawCircle(VerdeOscuroTor.copy(alpha = 0.07f), radius = size.width * 0.45f, center = Offset(0f, size.height * 0.95f))
            drawCircle(VerdeTor.copy(alpha = 0.05f), radius = size.width * 0.25f, center = Offset(size.width * 0.15f, size.height * 0.18f))
        }
        content()
    }
}

// Círculos blancos transparentes dentro de los encabezados verdes
@Composable
fun BoxScope.BurbujasEncabezado() {
    Canvas(Modifier.matchParentSize()) {
        drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.9f, center = Offset(size.width * 0.92f, -size.height * 0.15f))
        drawCircle(Color.White.copy(alpha = 0.06f), radius = size.height * 0.55f, center = Offset(size.width * 0.70f, size.height * 1.05f))
    }
}


// ---------------------------------------------------------------------------
// Versión 2.10 (spec 006): piezas del rediseño. Mismos colores, más movimiento.
// ---------------------------------------------------------------------------

val TextoOscuroTor = Color(0xFF1C2B22)   // texto principal (casi negro, con un toque verde)
val TextoGrisTor = Color(0xFF5E6B63)     // texto secundario
val VerdeClaroTor = VerdeTor.copy(alpha = 0.10f) // fondo verde muy claro de chips e íconos
val FondoTor = Color(0xFFF5F7F6)         // fondo claro de las pantallas (el mismo del registro v2.9)
val AmarilloFondoTor = Color(0xFFFEF3C7) // nota amarilla (la misma de la v2.9)
val AmarilloTextoTor = Color(0xFFB45309)
val RojoTor = Color(0xFFB91C1C)          // botón "Terminar ruta" y "Cerrar sesión"

// FR-605: el elemento se achica un poquito mientras se mantiene presionado
@Composable
fun Modifier.alPresionar(interaccion: MutableInteractionSource): Modifier {
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.96f else 1f, label = "presionar")
    return this.graphicsLayer { scaleX = escala; scaleY = escala }
}

// FR-605: al aparecer, el elemento sube un poco y se va haciendo visible (250 ms)
@Composable
fun Modifier.entradaSuave(retrasoMs: Int = 0): Modifier {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(durationMillis = 250, delayMillis = retrasoMs, easing = FastOutSlowInEasing))
    }
    return this.graphicsLayer {
        alpha = progreso.value
        translationY = (1f - progreso.value) * 48f
    }
}

// Botón grande en forma de píldora, con sombra del mismo color
@Composable
fun BotonPildora(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = VerdeTor,
    icono: ImageVector? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    alto: Dp = 56.dp
) {
    val interaccion = remember { MutableInteractionSource() }
    val forma = RoundedCornerShape(50)
    Button(
        onClick = onClick,
        enabled = habilitado,
        interactionSource = interaccion,
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .alPresionar(interaccion)
            .shadow(if (habilitado) 10.dp else 0.dp, forma, ambientColor = color.copy(alpha = 0.4f), spotColor = color.copy(alpha = 0.5f)),
        shape = forma,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        } else {
            if (icono != null) {
                Icon(icono, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Botón con borde verde y fondo blanco (para acciones secundarias, como "Editar perfil")
@Composable
fun BotonBorde(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, icono: ImageVector? = null) {
    val interaccion = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        interactionSource = interaccion,
        modifier = modifier.fillMaxWidth().height(54.dp).alPresionar(interaccion),
        shape = RoundedCornerShape(50),
        border = BorderStroke(2.dp, VerdeTor),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = VerdeOscuroTor)
    ) {
        if (icono != null) {
            Icon(icono, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

// FR-603: etiqueta (chip) de color. "solida" = fondo del color y letra blanca; si no, fondo clarito
@Composable
fun EtiquetaEstado(texto: String, color: Color, modifier: Modifier = Modifier, solida: Boolean = false, conPunto: Boolean = true) {
    val fondo = if (solida) color else color.copy(alpha = 0.13f)
    val letra = if (solida) Color.White else color
    Row(
        modifier
            .then(if (solida) Modifier.shadow(6.dp, RoundedCornerShape(50), ambientColor = color.copy(alpha = 0.4f), spotColor = color.copy(alpha = 0.4f)) else Modifier)
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conPunto) {
            Box(Modifier.size(8.dp).background(letra, CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(texto, color = letra, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Encabezado verde oscuro con un círculo verde grande y otro blanco transparente (Chofer y Perfil)
@Composable
fun EncabezadoVerde(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    IconosBarraEstadoBlancos()
    Box(modifier.fillMaxWidth().background(VerdeOscuroTor)) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(VerdeTor, radius = size.width * 0.38f, center = Offset(size.width * 0.12f, -size.width * 0.05f))
            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width * 0.22f, center = Offset(size.width * 0.95f, size.height))
        }
        Column(Modifier.statusBarsPadding(), content = content)
    }
}

// FR-606: estado vacío con un ícono grande y un texto amable
@Composable
fun EstadoVacio(icono: ImageVector, titulo: String, texto: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).background(VerdeClaroTor, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = VerdeTor, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextoOscuroTor)
        Text(texto, fontSize = 13.sp, color = TextoGrisTor, textAlign = TextAlign.Center)
    }
}

// Pantalla verde con círculos arriba y una hoja blanca que sube desde abajo (Login y Registro)
@Composable
fun PantallaVerdeConHoja(
    titulo: String,
    subtitulo: String,
    tamanoTitulo: Int = 32,
    hoja: @Composable ColumnScope.() -> Unit
) {
    IconosBarraEstadoBlancos()
    Box(Modifier.fillMaxSize().background(VerdeOscuroTor)) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(VerdeTor, radius = size.width * 0.55f, center = Offset(size.width * 1.0f, size.height * 0.02f))
            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width * 0.28f, center = Offset(0f, size.height * 0.30f))
        }
        Column(Modifier.fillMaxSize()) {
            // Logo y título sobre el verde
            Column(
                Modifier.statusBarsPadding().padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 24.dp).entradaSuave(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // v2.10: el mismo logo del ícono de la app (pin con el camión)
                LogoTor(Modifier.size(64.dp))
                Text(titulo, color = Color.White, fontSize = tamanoTitulo.sp, fontWeight = FontWeight.Bold, lineHeight = (tamanoTitulo + 6).sp)
                Text(subtitulo, color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp)
            }
            // Hoja blanca: sube con animación y se puede desplazar si el teclado la tapa
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .entradaSuave(retrasoMs = 80)
                    .background(Color.White, RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 26.dp, vertical = 28.dp),
                content = hoja
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Versión 2.10, mejoras extra (spec 006, FR-611 a FR-618)
// ---------------------------------------------------------------------------

// FR-613: en las pantallas verdes, la hora y la batería de arriba se ven en blanco.
// Al salir de la pantalla se regresa como estaba (oscuro sobre el mapa).
@Composable
fun IconosBarraEstadoBlancos() {
    val vista = LocalView.current
    if (vista.isInEditMode) return
    DisposableEffect(Unit) {
        var ctx = vista.context
        while (ctx is ContextWrapper && ctx !is Activity) ctx = ctx.baseContext
        val actividad = ctx as? Activity
        val control = actividad?.let { WindowCompat.getInsetsController(it.window, vista) }
        val antes = control?.isAppearanceLightStatusBars
        control?.isAppearanceLightStatusBars = false // false = íconos blancos
        onDispose { if (control != null && antes != null) control.isAppearanceLightStatusBars = antes }
    }
}

// FR-614: rectángulo gris que "brilla" mientras llegan los datos (esqueleto de carga)
@Composable
fun Esqueleto(modifier: Modifier = Modifier, radio: Dp = 14.dp) {
    val t = rememberInfiniteTransition(label = "esqueleto")
    val brillo by t.animateFloat(
        0.45f, 1f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "brillo"
    )
    Box(modifier.background(Color(0xFFE3E8E5).copy(alpha = brillo), RoundedCornerShape(radio)))
}

// FR-616: el elemento aparece con un pequeño rebote cada vez que cambia "clave" (por ejemplo, un aviso nuevo)
@Composable
fun Modifier.rebote(clave: Any?): Modifier {
    val escala = remember(clave) { Animatable(0.85f) }
    LaunchedEffect(clave) {
        escala.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow))
    }
    return this.graphicsLayer { scaleX = escala.value; scaleY = escala.value }
}
