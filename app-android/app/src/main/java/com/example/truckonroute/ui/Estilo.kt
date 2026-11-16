package com.example.truckonroute.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
