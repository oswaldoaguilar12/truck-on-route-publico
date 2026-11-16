package com.example.truckonroute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truckonroute.ui.HomeScreen // Asegúrate que la ruta sea correcta
import com.example.truckonroute.ui.RegistroScreen
import com.example.truckonroute.data.MENSAJE_CORREO_INVALIDO
import com.example.truckonroute.data.esCorreoValido
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.truckonroute.avisos.Notificaciones
import com.example.truckonroute.avisos.ServicioCercania
import com.example.truckonroute.data.ApiTor
import com.example.truckonroute.data.Sesion
import com.example.truckonroute.data.Usuario
import com.example.truckonroute.ui.ChoferScreen
import com.example.truckonroute.ui.DegradadoVerde
import com.example.truckonroute.ui.FondoDecorado
import com.example.truckonroute.ui.TarjetaTor
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TruckOnRouteTheme {
                MainNavigation()
            }
        }
    }
}

// --- TEMA Y COLORES ---
private val GreenMain = Color(0xFF019B43)
private val BackgroundColor = Color(0xFFF3EFE8)
private val GreenDark = Color(0xFF0A7D2C) // versión 2.9: mismo verde oscuro del resto de la app

@Composable
fun TruckOnRouteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = GreenMain,
            background = BackgroundColor
        ),
        content = content
    )
}

// --- NAVEGACIÓN PRINCIPAL ---
// Versión 2.8 (spec 004): la sesión se guarda en el celular y la pantalla depende del rol.
// Chofer → ChoferScreen. Ciudadano (y administrador) → HomeScreen con el mapa.
@Composable
fun MainNavigation() {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var usuario by remember { mutableStateOf(Sesion.usuario(contexto)) }
    var showRegister by remember { mutableStateOf(false) }

    // Android 13+: pide permiso para mostrar notificaciones (avisos del camión)
    val pedirNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        Notificaciones.crearCanales(contexto)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificaciones.hayPermiso(contexto)) {
            pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val cerrarSesion: () -> Unit = {
        val token = Sesion.token(contexto)
        ServicioCercania.detener(contexto)
        Sesion.guardarAvisarCerca(contexto, false)
        Sesion.cerrar(contexto)
        usuario = null
        if (token != null) alcance.launch { ApiTor.cerrarSesion(token) }
    }

    val u = usuario
    if (u != null) {
        if (u.esChofer) ChoferScreen(usuario = u, onLogout = cerrarSesion)
        else HomeScreen(usuario = u, onLogout = cerrarSesion)
    } else {
        if (showRegister) {
            RegistroScreen(onBack = { showRegister = false })
        } else {
            LoginScreen(
                onRegisterClick = { showRegister = true },
                onLoginSuccess = { token, nuevo ->
                    Sesion.guardar(contexto, token, nuevo)
                    usuario = nuevo
                }
            )
        }
    }
}
// --- PANTALLA DE LOGIN ---
// Truck on Rute 2.5: ajustes visuales (logo, ver contraseña, botones del mismo estilo). Mismos colores.
@Composable
fun LoginScreen(onRegisterClick: () -> Unit, onLoginSuccess: (String, Usuario) -> Unit) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    // Truck on Rute 2.7 (spec 003): los errores se muestran después del primer intento de entrar
    var intento by remember { mutableStateOf(false) }
    val correoValido = esCorreoValido(correo)
    val faltaContrasena = contrasena.isBlank()
    // Versión 2.8: el login consulta la API
    val alcance = rememberCoroutineScope()
    var entrando by remember { mutableStateOf(false) }
    var errorServidor by remember { mutableStateOf<String?>(null) }

    // Versión 2.9: fondo con círculos verdes suaves y el formulario dentro de una tarjeta con sombra
    FondoDecorado(BackgroundColor) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // en pantallas chicas el teclado no tapa el botón
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Logo: el mismo ícono de camión, ahora en un círculo con el degradado verde y sombra
        Box(
            modifier = Modifier
                .size(104.dp)
                .shadow(14.dp, CircleShape, ambientColor = GreenDark.copy(alpha = 0.4f), spotColor = GreenDark.copy(alpha = 0.4f))
                .background(DegradadoVerde, CircleShape)
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(56.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("TRUCK\nON ROUTE", color = GreenMain, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 32.sp, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(24.dp))

        TarjetaTor {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Bienvenido", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Consulta dónde va el camión de la basura", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(22.dp))

        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Outlined.Email, null) },
            singleLine = true,
            isError = intento && !correoValido,
            supportingText = if (intento && !correoValido) {
                { Text(MENSAJE_CORREO_INVALIDO) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain, focusedLeadingIconColor = GreenMain)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = contrasena, onValueChange = { contrasena = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
            // Ojo para mostrar u ocultar la contraseña
            trailingIcon = {
                IconButton(onClick = { verContrasena = !verContrasena }) {
                    Icon(
                        if (verContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (verContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            isError = intento && faltaContrasena,
            supportingText = if (intento && faltaContrasena) {
                { Text("Escribe tu contraseña") }
            } else null,
            visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain, focusedLeadingIconColor = GreenMain)
        )
        Spacer(modifier = Modifier.height(26.dp))
        Button(
            onClick = {
                intento = true
                errorServidor = null
                // Primero se revisa el formato (spec 003) y luego la API revisa la cuenta (spec 004)
                if (correoValido && !faltaContrasena) {
                    entrando = true
                    alcance.launch {
                        val (resultado, error) = ApiTor.iniciarSesion(correo, contrasena)
                        entrando = false
                        if (resultado != null) onLoginSuccess(resultado.first, resultado.second)
                        else errorServidor = error
                    }
                }
            },
            enabled = !entrando,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
        ) {
            if (entrando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            else Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        // "Correo o contraseña incorrectos" o "No se pudo conectar con el servidor"
        errorServidor?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it, color = Color(0xFFB00020), textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("¿No tienes cuenta?", color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, GreenMain)
        ) {
            Text("Registrarse", color = GreenMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        } // fin de la columna de la tarjeta
        } // fin de TarjetaTor
        Spacer(modifier = Modifier.height(24.dp))
    }
    } // fin de FondoDecorado
}

// --- PANTALLA DE REGISTRO ---
@Composable
fun RegisterScreen(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(GreenMain),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("Nombre", color = Color.White)
            CustomTextField(nombre) { nombre = it }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Correo electrónico", color = Color.White)
            CustomTextField(correo) { correo = it }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Teléfono", color = Color.White)
            CustomTextField(telefono) { telefono = it }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Contraseña", color = Color.White)
            CustomTextField(contrasena) { contrasena = it }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7ED957))
            ) { Text("Guardar cambios", color = Color.White) }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) { Text("Volver", color = GreenMain) }
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---
@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEDE7DC),
            unfocusedContainerColor = Color(0xFFEDE7DC)
        )
    )
}

@Composable
fun ButtonOutlinedCustom(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.border(2.dp, GreenMain, RoundedCornerShape(12.dp)).padding(horizontal = 18.dp)
    ) { Text(text, color = GreenMain) }
}