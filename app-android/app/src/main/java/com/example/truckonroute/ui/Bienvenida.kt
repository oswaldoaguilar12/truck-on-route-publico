package com.example.truckonroute.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import com.example.truckonroute.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Versión 2.10 (spec 006, FR-609): logo al abrir la app y camioncito animado al iniciar sesión.
// El camión se dibuja con figuras (Canvas), sin GIF ni librerías: así pesa casi nada y usa los verdes de la app.
// La vista previa en GIF está en evidencia/v2.10/camion_caricatura_v2.10.gif

// Colores del camión de caricatura (los mismos de la app: verdes, blanco, grises y el amarillo de los avisos)
private val Contorno = Color(0xFF1C2B22)
private val Vidrio = Color(0xFFE8F6EE)
private val GrisLlanta = Color(0xFFD6DCD8)
private val FaroFondo = Color(0xFFFEF3C7)
private val FaroBorde = Color(0xFFB45309)
private const val CICLO_MS = 2400f

// Pasa de un valor a otro según el tiempo, con entrada y salida suaves (como los keyframes de CSS)
private fun cuadros(t: Float, puntos: List<Pair<Float, Float>>): Float {
    for (i in 0 until puntos.size - 1) {
        val (a, va) = puntos[i]
        val (b, vb) = puntos[i + 1]
        if (t in a..b) {
            val x = if (b > a) (t - a) / (b - a) else 0f
            val suave = if (x < 0.5f) 2 * x * x else 1 - (-2 * x + 2) * (-2 * x + 2) / 2
            return va + (vb - va) * suave
        }
    }
    return puntos.last().second
}

private fun DrawScope.cajaRedonda(color: Color, x: Float, y: Float, w: Float, h: Float, r: Float, borde: Float = 0f) {
    drawRoundRect(color, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(r))
    if (borde > 0f) drawRoundRect(Contorno, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(r), style = Stroke(borde))
}

// Bolsa de basura gris con su amarrito y un brillo
private fun DrawScope.bolsa(x: Float, y: Float, alfa: Float) {
    drawOval(GrisLlanta.copy(alpha = alfa), topLeft = Offset(x - 7f, y - 7.5f), size = Size(14f, 15f))
    drawOval(Contorno.copy(alpha = alfa), topLeft = Offset(x - 7f, y - 7.5f), size = Size(14f, 15f), style = Stroke(2.5f))
    val amarre = Path().apply { moveTo(x - 3f, y - 11f); lineTo(x, y - 7f); lineTo(x + 3f, y - 11f) }
    drawPath(amarre, Contorno.copy(alpha = alfa), style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(Color.White.copy(alpha = 0.8f * alfa), radius = 1.8f, center = Offset(x - 2.5f, y - 2.5f))
}

// Dibuja un cuadro de la animación. Todo se dibuja en un espacio de 240 x 160 "unidades"
// que se estira al tamaño del Canvas. t = milisegundos dentro del ciclo de 2.4 s.
private fun DrawScope.dibujarCaricatura(t: Float) {
    val u = size.width / 240f
    scale(u, u, pivot = Offset.Zero) {
        val avance = (t % 600f) / 600f
        val linea = Stroke(3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Líneas de velocidad detrás del camión
        for ((y, largo, desfase) in listOf(Triple(78f, 22f, 0f), Triple(94f, 30f, 0.33f), Triple(110f, 18f, 0.66f))) {
            val fase = (avance + desfase) % 1f
            val x = 40f - fase * 30f
            drawLine(Color.White.copy(alpha = 0.35f * (1f - fase)), Offset(x, y), Offset(x + largo, y), strokeWidth = 3f, cap = StrokeCap.Round)
        }
        // Rayas del camino que se mueven a la izquierda
        var xr = -avance * 30f
        while (xr < 240f) {
            drawRoundRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(xr, 147f), size = Size(16f, 4f), cornerRadius = CornerRadius(2f))
            xr += 30f
        }
        // Brinco del camión y su sombra
        val brinco = abs(sin(PI.toFloat() * (t % 300f) / 300f))
        val dy = -brinco * 3f
        val anchoSombra = 82f - brinco * 4f
        drawOval(Color.Black.copy(alpha = 0.18f), topLeft = Offset(132f - anchoSombra, 137f), size = Size(anchoSombra * 2, 10f))
        // Humo del escape
        for (k in 0..2) {
            val fase = ((t + k * 200f) % 600f) / 600f
            drawCircle(Color.White.copy(alpha = (1f - fase) * 0.85f), radius = 3f + fase * 8f, center = Offset(151f - fase * 34f, 50f + dy - fase * 24f))
        }

        translate(top = dy) {
            // Tolva: se levanta desde su esquina trasera
            val tolva = cuadros(t, listOf(0f to 0f, 900f to 0f, 1400f to -30f, 1850f to -30f, 2300f to 0f, CICLO_MS to 0f))
            rotate(tolva, pivot = Offset(52f, 112f)) {
                cajaRedonda(VerdeTor, 50f, 50f, 100f, 62f, 14f, borde = 3.5f)
                val tapa = Path().apply {
                    addRoundRect(RoundRect(Rect(50f, 50f, 150f, 64f), topLeft = CornerRadius(14f), topRight = CornerRadius(14f)))
                }
                drawPath(tapa, VerdeOscuroTor)
                drawPath(tapa, Contorno, style = linea)
                cajaRedonda(Color.White.copy(alpha = 0.35f), 60f, 70f, 80f, 4f, 2f)
                for (x in listOf(72f, 95f, 118f)) cajaRedonda(Color.White, x, 80f, 10f, 24f, 5f)
            }
            // Escape
            cajaRedonda(Contorno, 147f, 54f, 7f, 22f, 3f)
            // Chasis
            cajaRedonda(VerdeOscuroTor, 48f, 110f, 162f, 12f, 6f, borde = 3.5f)
            // Cabina
            val cabina = Path().apply {
                moveTo(154f, 116f); lineTo(154f, 78f); quadraticBezierTo(154f, 70f, 162f, 70f); lineTo(184f, 70f)
                quadraticBezierTo(192f, 70f, 196f, 77f); lineTo(208f, 98f); quadraticBezierTo(212f, 104f, 212f, 110f)
                lineTo(212f, 116f); close()
            }
            drawPath(cabina, Color.White)
            drawPath(cabina, Contorno, style = linea)
            val vidrio = Path().apply {
                moveTo(163f, 96f); lineTo(163f, 82f); quadraticBezierTo(163f, 78f, 167f, 78f); lineTo(183f, 78f)
                quadraticBezierTo(187f, 78f, 189f, 82f); lineTo(198f, 96f); close()
            }
            drawPath(vidrio, Vidrio)
            drawPath(vidrio, Contorno, style = Stroke(3f, join = StrokeJoin.Round))
            // Ojos que parpadean
            val abierto = if (t > 2000f && t < 2130f) 0.12f else 1f
            for ((x, y) in listOf(173f to 87f, 186f to 87f)) {
                val alto = 10.8f * abierto
                drawOval(Color.White, topLeft = Offset(x - 4.6f, y - alto / 2), size = Size(9.2f, alto))
                drawOval(Contorno, topLeft = Offset(x - 4.6f, y - alto / 2), size = Size(9.2f, alto), style = Stroke(2f))
                if (abierto > 0.5f) {
                    drawCircle(Contorno, radius = 2.4f, center = Offset(x + 1.6f, y + 0.8f))
                    drawCircle(Color.White, radius = 0.8f, center = Offset(x + 2.3f, y - 0.2f))
                }
            }
            // Sonrisa, cachete, faro y defensa
            val sonrisa = Path().apply { moveTo(190f, 104f); quadraticBezierTo(197f, 110f, 204f, 103f) }
            drawPath(sonrisa, Contorno, style = Stroke(2.5f, cap = StrokeCap.Round))
            drawOval(VerdeTor.copy(alpha = 0.25f), topLeft = Offset(178f, 101.5f), size = Size(8f, 5f))
            drawCircle(FaroFondo, radius = 3.5f, center = Offset(207f, 97f))
            drawCircle(FaroBorde, radius = 3.5f, center = Offset(207f, 97f), style = Stroke(2f))
            cajaRedonda(Contorno, 198f, 113f, 17f, 6f, 3f)
        }

        // Llantas con tornillos que giran
        for (x in listOf(80f, 178f)) {
            val c = Offset(x, 124f + dy * 0.3f)
            drawCircle(Contorno, radius = 16f, center = c)
            drawCircle(GrisLlanta, radius = 8f, center = c)
            for (k in 0..3) {
                val angulo = avance * 2f * PI.toFloat() + k * PI.toFloat() / 2f
                drawCircle(Contorno, radius = 1.6f, center = Offset(c.x + cos(angulo) * 5f, c.y + sin(angulo) * 5f))
            }
            drawCircle(Color.White, radius = 2.2f, center = c)
        }

        // Bolsas de basura: caen de la tolva, rebotan en el piso y se quedan atrás
        for (i in 0..2) {
            val inicio = 1350f + i * 130f
            if (t < inicio) continue
            val d = t - inicio
            var x = 40f - i * 13f
            var y = 104f + 0.00012f * d * d
            val piso = 133f
            if (y > piso) {
                val tPiso = sqrt((piso - 104f) / 0.00012f)
                val d2 = d - tPiso
                y = piso - max(0f, sin(min(d2 / 180f, 1f) * PI.toFloat())) * 6f
                x -= d2 * 0.05f
            }
            val alfa = if (t > 2150f) max(0f, 1f - (t - 2150f) / 250f) else 1f
            bolsa(x, y, alfa)
        }
    }
}

// Camioncito de caricatura: avanza, echa humo, parpadea, levanta la tolva y tira bolsas de basura
@Composable
fun CamionAnimado(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "camion")
    val t by transicion.animateFloat(
        0f, CICLO_MS,
        infiniteRepeatable(tween(CICLO_MS.toInt(), easing = LinearEasing)),
        label = "tiempo"
    )
    Canvas(modifier.aspectRatio(240f / 160f)) { dibujarCaricatura(t) }
}

// Logo de la app (el mismo del ícono): pin de ubicación con el camión, sobre el degradado verde
@Composable
fun LogoTor(modifier: Modifier = Modifier) {
    Box(
        modifier
            .shadow(18.dp, RoundedCornerShape(34.dp))
            .clip(RoundedCornerShape(34.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1DB45A), VerdeTor, VerdeOscuroTor)))
    ) {
        // El dibujo del ícono mide 108 y se ven los 72 del centro, por eso se agranda 1.5 veces
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Logo de Truck on Route",
            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f }
        )
    }
}

// Fondo verde oscuro con dos círculos (el mismo del login)
@Composable
private fun FondoVerde(content: @Composable BoxScope.() -> Unit) {
    IconosBarraEstadoBlancos()
    Box(Modifier.fillMaxSize().background(VerdeOscuroTor), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(VerdeTor, radius = size.width * 0.55f, center = Offset(size.width, size.height * 0.02f))
            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width * 0.28f, center = Offset(0f, size.height * 0.80f))
        }
        content()
    }
}

// Pantalla al abrir la app (1.6 s): el logo aparece "rebotando" y luego el nombre
@Composable
fun PantallaInicio() {
    val escala = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        escala.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
    }
    FondoVerde {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LogoTor(Modifier.size(150.dp).graphicsLayer { scaleX = escala.value; scaleY = escala.value })
            Spacer(Modifier.height(24.dp))
            Text("TRUCK ON ROUTE", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                modifier = Modifier.entradaSuave(retrasoMs = 200))
            Text("Sabe cuándo pasa el camión", color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp,
                modifier = Modifier.entradaSuave(retrasoMs = 320))
        }
    }
}

// Pantalla al iniciar sesión (2.4 s): el camioncito animado y un saludo
@Composable
fun PantallaEntrando(nombre: String, mensaje: String) {
    FondoVerde {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            CamionAnimado(Modifier.fillMaxWidth(0.9f))
            Spacer(Modifier.height(28.dp))
            Text("¡Hola, $nombre!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                modifier = Modifier.entradaSuave())
            Text(mensaje, color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp, textAlign = TextAlign.Center,
                modifier = Modifier.entradaSuave(retrasoMs = 120))
        }
    }
}
