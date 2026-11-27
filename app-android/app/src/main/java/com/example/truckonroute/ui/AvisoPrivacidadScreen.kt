package com.example.truckonroute.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Versión 2.12 (spec 008): aviso de privacidad.
// Es el mismo texto de specs/008-aviso-privacidad/aviso-privacidad.md y de api/public/privacidad.html.
// Si cambia el aviso, se cambia en los tres lugares.

const val CORREO_PRIVACIDAD = "privacidad@truckonroute.mx"
const val FECHA_AVISO = "27 de noviembre de 2026"

// Cada sección: título, texto y si se resalta (la de la ubicación, FR-807)
private data class SeccionAviso(val titulo: String, val texto: String, val resaltada: Boolean = false)

private val SECCIONES = listOf(
    SeccionAviso(
        "Quién es responsable de tus datos",
        "Truck on Route es un proyecto escolar del equipo G2VSIS07 de Ingeniería en Sistemas Computacionales " +
            "del Instituto Tecnológico Superior de Huauchinango, Puebla. El equipo es responsable de cuidar los " +
            "datos personales que nos des en la app. Contacto: $CORREO_PRIVACIDAD"
    ),
    SeccionAviso(
        "Qué datos pedimos",
        "Nombre y correo electrónico (obligatorios). Contraseña: no la guardamos tal cual, guardamos una versión " +
            "cifrada que no se puede convertir de regreso. Teléfono y colonia (opcionales). " +
            "No pedimos datos sensibles, como salud, religión o datos bancarios."
    ),
    SeccionAviso(
        "Tu ubicación",
        "Si activas el aviso \"el camión está cerca\", la app usa la ubicación de tu celular para calcular a qué " +
            "distancia está el camión. Ese cálculo se hace dentro de tu celular y tu ubicación no se manda a " +
            "nuestro servidor. Puedes quitar el permiso cuando quieras desde los ajustes de tu celular. " +
            "La ubicación del camión recolector sí se guarda, pero es la del vehículo, no la de una persona.",
        resaltada = true
    ),
    SeccionAviso(
        "Para qué usamos tus datos",
        "Para crear tu cuenta e iniciar sesión, saber qué ruta pasa por tu colonia y mandarte los avisos del " +
            "camión (\"ya está en ruta\" y \"está cerca\"). No usamos tus datos para publicidad ni para nada más."
    ),
    SeccionAviso(
        "Con quién compartimos tus datos",
        "Con nadie. No vendemos, rentamos ni compartimos tus datos con otras personas o empresas. Solo el equipo " +
            "que administra el sistema puede verlos, y únicamente para darte soporte."
    ),
    SeccionAviso(
        "Tus derechos (ARCO)",
        "Puedes acceder a tus datos, rectificarlos si están mal, cancelarlos (pedir que borremos tu cuenta) u " +
            "oponerte a que los usemos. Escribe a $CORREO_PRIVACIDAD desde el correo de tu cuenta y di qué " +
            "quieres hacer. Te responderemos en un máximo de 20 días hábiles. También puedes retirar tu " +
            "consentimiento en cualquier momento de la misma forma."
    ),
    SeccionAviso(
        "Cambios a este aviso",
        "Si cambiamos este aviso, lo verás actualizado aquí (menú > Aviso de privacidad) y en la página web del " +
            "sistema, con su nueva fecha."
    ),
    SeccionAviso(
        "Aceptación",
        "Al marcar \"Acepto el aviso de privacidad\" cuando creas tu cuenta, aceptas que usemos tus datos como " +
            "dice este aviso."
    )
)

// FR-804: pantalla del aviso con encabezado verde y secciones que se leen deslizando
@Composable
fun AvisoPrivacidadScreen(onAtras: () -> Unit) {
    BackHandler(onBack = onAtras)
    Column(Modifier.fillMaxSize().background(FondoTor)) {
        EncabezadoVerde {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).background(Color.White.copy(alpha = 0.16f), CircleShape).clickable(onClick = onAtras),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.ArrowBack, "Regresar", tint = Color.White) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Tus datos están cuidados", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        Text("Aviso de privacidad", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Última actualización: $FECHA_AVISO", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SECCIONES.forEachIndexed { i, s -> TarjetaSeccion(i + 1, s, Modifier.entradaSuave(retrasoMs = 40 * i)) }
            Text(
                "Este aviso sigue la Ley Federal de Protección de Datos Personales en Posesión de los Particulares. " +
                    "Es un proyecto escolar: antes de publicar la app para el público, conviene que lo revise un " +
                    "asesor legal o el área jurídica del instituto.",
                fontSize = 11.sp, color = TextoGrisTor, lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun TarjetaSeccion(numero: Int, s: SeccionAviso, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = TextoOscuroTor.copy(alpha = 0.06f), spotColor = TextoOscuroTor.copy(alpha = 0.06f))
            .background(Color.White, forma)
            .then(if (s.resaltada) Modifier.border(2.dp, VerdeTor, forma) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(26.dp).background(VerdeClaroTor, CircleShape), contentAlignment = Alignment.Center) {
                Text("$numero", color = VerdeOscuroTor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(10.dp))
            Text(s.titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuroTor)
        }
        Spacer(Modifier.height(6.dp))
        Text(s.texto, fontSize = 13.sp, color = Color(0xFF3D4A42), lineHeight = 20.sp)
    }
}
