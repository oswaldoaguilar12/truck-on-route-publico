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

    // Versión 2.9: mismo fondo con círculos verdes suaves que el login
    FondoDecorado(Color(0xFFF5F7F6)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // v2.8: con 5 campos el teclado podía tapar el botón
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // Logo (versión 2.9: en un círculo con el degradado verde y sombra, igual que el login)
        Box(
            Modifier.size(84.dp)
                .shadow(12.dp, CircleShape, ambientColor = Color(0xFF0A7D2C).copy(alpha = 0.4f), spotColor = Color(0xFF0A7D2C).copy(alpha = 0.4f))
                .background(DegradadoVerde, CircleShape)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "TRUCK ON ROUTE",
            color = Color(0xFF0A7D2C),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Card principal
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).sombraSuave(radio = 25.dp, altura = 12.dp),
            shape = RoundedCornerShape(25.dp),
            elevation = CardDefaults.cardElevation(0.dp), // v2.9: la sombra la pone sombraSuave()
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Crear cuenta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A7D2C)
                )

                Spacer(modifier = Modifier.height(20.dp))

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

                Button(
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
                    },
                    enabled = !guardando && !creada,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0A7D2C)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
                ) {
                    Text(
                        text = "Registrarme",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onBack) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
    } // fin de FondoDecorado
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
            focusedContainerColor = Color(0xFFF1F1F1),
            unfocusedContainerColor = Color(0xFFF1F1F1),
            focusedBorderColor = Color(0xFF0A7D2C),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}