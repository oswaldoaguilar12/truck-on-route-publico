package com.example.truckonroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truckonroute.data.MENSAJE_CORREO_INVALIDO
import com.example.truckonroute.data.esCorreoValido
import com.example.truckonroute.data.ApiTor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(onBack: () -> Unit) {
    // Truck on Rute 2.7 (spec 003): el correo se valida al tocar "Registrarme"
    var correo by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(false) }
    val correoValido = esCorreoValido(correo)
    // Versión 2.8 (spec 004, H14): el registro crea la cuenta de ciudadano en la API
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var colonia by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var errorServidor by remember { mutableStateOf<String?>(null) }
    var creada by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val faltaNombre = nombre.isBlank()
    val contrasenaCorta = contrasena.length < 6

    // Versión 2.10 (spec 006): mismo fondo verde y hoja blanca que el login.
    // Los campos, la validación y la llamada a la API son los mismos de la v2.8.
    PantallaVerdeConHoja(titulo = "Crea tu cuenta", subtitulo = "Te avisamos cuando el camión pase por tu colonia", tamanoTitulo = 28) {
        Text("Tus datos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor)
        Spacer(modifier = Modifier.height(12.dp))

        CampoTextoMejorado(
            "Nombre", Icons.Default.Person, valor = nombre, alCambiar = { nombre = it },
            error = if (intento && faltaNombre) "Escribe tu nombre" else null
        )
        CampoTextoMejorado(
            "Correo electrónico", Icons.Default.Email,
            valor = correo, alCambiar = { correo = it },
            error = if (intento && !correoValido) MENSAJE_CORREO_INVALIDO else null
        )
        CampoTextoMejorado("Teléfono", Icons.Default.Phone, valor = telefono, alCambiar = { telefono = it })
        // Versión 2.8: la colonia sirve para saber qué ruta le toca
        CampoTextoMejorado("Colonia", Icons.Default.Home, valor = colonia, alCambiar = { colonia = it })
        CampoTextoMejorado(
            "Contraseña", Icons.Default.Lock, true, valor = contrasena, alCambiar = { contrasena = it },
            error = if (intento && contrasenaCorta) "Mínimo 6 caracteres" else null
        )
        errorServidor?.let { Text(it, color = Color(0xFFB00020)) }
        if (creada) Text("¡Cuenta creada! Ya puedes iniciar sesión.", color = Color(0xFF0A7D2C), fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(15.dp))

        BotonPildora(
            texto = "Registrarme",
            habilitado = !guardando && !creada,
            cargando = guardando,
            onClick = {
                intento = true
                errorServidor = null
                if (correoValido && !faltaNombre && !contrasenaCorta) {
                    guardando = true
                    alcance.launch {
                        errorServidor = ApiTor.registrar(nombre, correo, telefono, colonia, contrasena)
                        guardando = false
                        if (errorServidor == null) {
                            creada = true
                            delay(1500) // deja leer el mensaje y regresa al login
                            onBack()
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿Ya tienes cuenta? ", color = TextoGrisTor)
            Text("Inicia sesión", color = Color(0xFF0A7D2C), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CampoTextoMejorado(
    label: String,
    icono: ImageVector,
    isPassword: Boolean = false,
    // Opcionales (spec 003): para que la pantalla lea el texto y muestre un error debajo
    valor: String? = null,
    alCambiar: ((String) -> Unit)? = null,
    error: String? = null
) {

    var textoLocal by remember { mutableStateOf("") }
    val texto = valor ?: textoLocal

    OutlinedTextField(
        value = texto,
        onValueChange = { if (alCambiar != null) alCambiar(it) else textoLocal = it },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        placeholder = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color(0xFF0A7D2C)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(15.dp),
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            // Versión 2.10: el mismo fondo claro de los campos del login
            focusedContainerColor = FondoTor,
            unfocusedContainerColor = FondoTor,
            errorContainerColor = FondoTor,
            focusedBorderColor = Color(0xFF0A7D2C),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}