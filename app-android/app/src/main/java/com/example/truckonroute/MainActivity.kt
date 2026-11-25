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
import com.example.truckonroute.ui.TipografiaTor
import com.example.truckonroute.ui.PantallaVerdeConHoja
import com.example.truckonroute.ui.BotonPildora
import com.example.truckonroute.ui.FondoTor
import com.example.truckonroute.ui.TextoOscuroTor
import com.example.truckonroute.ui.TextoGrisTor
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.truckonroute.ui.PantallaInicio
import com.example.truckonroute.ui.PantallaEntrando

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
        typography = TipografiaTor, // versión 2.10 (spec 006): fuente Poppins en toda la app
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
    // Versión 2.10 (spec 006, FR-609): logo al abrir la app y camioncito animado al iniciar sesión
    var mostrarInicio by remember { mutableStateOf(true) }
    var entrando by remember { mutableStateOf<Usuario?>(null) }
    LaunchedEffect(Unit) {
        delay(1600)
        mostrarInicio = false
    }
    LaunchedEffect(entrando) {
        val nuevo = entrando ?: return@LaunchedEffect
        delay(2400) // lo que dura la animación del camión
        usuario = nuevo
        entrando = null
    }

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
    val e = entrando
    if (mostrarInicio) {
        PantallaInicio()
    } else if (e != null) {
        PantallaEntrando(
            nombre = e.nombre.substringBefore(" "),
            mensaje = if (e.esChofer) "Cargando tu recorrido…" else "Preparando tu mapa…"
        )
    } else if (u != null) {
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
                    entrando = nuevo // primero la animación; después entra a su pantalla
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

    // Versión 2.10 (spec 006): fondo verde con círculos, título grande y una hoja blanca que sube.
    // La validación y la consulta a la API son las mismas de la v2.8.
    PantallaVerdeConHoja(titulo = "Sabe cuándo pasa\nel camión", subtitulo = "Truck on Route · Huauchinango") {
        Text("Hola de nuevo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor)
        Text("Entra para ver dónde va el camión de la basura", fontSize = 14.sp, color = TextoGrisTor)
        Spacer(modifier = Modifier.height(22.dp))

        EtiquetaCampo("Correo")
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("nombre@gmail.com") },
            leadingIcon = { Icon(Icons.Outlined.Email, null) },
            singleLine = true,
            isError = intento && !correoValido,
            supportingText = if (intento && !correoValido) {
                { Text(MENSAJE_CORREO_INVALIDO) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(16.dp),
            colors = coloresCampo()
        )
        Spacer(modifier = Modifier.height(12.dp))
        EtiquetaCampo("Contraseña")
        OutlinedTextField(
            value = contrasena, onValueChange = { contrasena = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tu contraseña") },
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
            colors = coloresCampo()
        )
        Spacer(modifier = Modifier.height(22.dp))
        BotonPildora(
            texto = "Entrar",
            icono = Icons.AutoMirrored.Filled.ArrowForward,
            habilitado = !entrando,
            cargando = entrando,
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
            }
        )
        // "Correo o contraseña incorrectos" o "No se pudo conectar con el servidor"
        errorServidor?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it, color = Color(0xFFB00020), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = TextoGrisTor, fontSize = 14.sp)
            TextButton(onClick = onRegisterClick) {
                Text("Regístrate", color = GreenDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Versión 2.10: texto pequeño arriba de cada campo
@Composable
fun EtiquetaCampo(texto: String) {
    Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF445248), modifier = Modifier.padding(bottom = 6.dp))
}

// Versión 2.10: campos con fondo claro y borde verde al escribir
@Composable
fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FondoTor,
    unfocusedContainerColor = FondoTor,
    errorContainerColor = FondoTor,
    focusedBorderColor = GreenMain,
    unfocusedBorderColor = Color.Transparent,
    focusedLeadingIconColor = GreenMain,
    unfocusedLeadingIconColor = GreenDark
)

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