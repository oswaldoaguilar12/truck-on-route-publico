package com.example.truckonroute.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.truckonroute.avisos.Permisos
import com.example.truckonroute.avisos.ServicioCercania
import com.example.truckonroute.avisos.Vigilancia
import com.example.truckonroute.data.ApiTor
import com.example.truckonroute.data.Aviso
import com.example.truckonroute.data.Sesion
import com.example.truckonroute.data.Usuario
import kotlinx.coroutines.delay
import com.example.truckonroute.data.buscarColonias

// Colores Corporativos
private val GreenMain = Color(0xFF019B43)
private val GreenDark = Color(0xFF0A7D2C)
private val BackgroundGray = Color(0xFFEFEFEF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Versión 2.8 (spec 004): recibe el usuario de la sesión (ciudadano o administrador)
fun HomeScreen(usuario: Usuario, onLogout: () -> Unit = {}) {
    // --- ESTADOS DE NAVEGACIÓN Y MENÚ ---
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Inicio, 1: Perfil
    var showHelpDialog by remember { mutableStateOf(false) } // Diálogo Ayuda

    // Versión 2.8: se retiró la clave fija "admin123"; el administrador usa el panel web con su cuenta

    // --- MENÚ LATERAL (DRAWER) ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                // Versión 2.9: encabezado del menú con el degradado verde y burbujas
                Box(
                    modifier = Modifier.fillMaxWidth().background(DegradadoVerde)
                ) {
                    BurbujasEncabezado()
                    Column(Modifier.padding(24.dp)) {
                        Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("TRUCK ON ROUTE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Opción: Inicio
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Opción: Ayuda y Soporte
                NavigationDrawerItem(
                    label = { Text("Ayuda y Soporte") },
                    selected = false,
                    onClick = {
                        showHelpDialog = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.HelpOutline, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                // Opción: Cerrar Sesión
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.Logout, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        // --- CONTENIDO ---
        run {
            Scaffold(
                topBar = {
                    HeaderSection(onMenuClick = { scope.launch { drawerState.open() } })
                },
                bottomBar = {
                    // Versión 2.9: barra inferior con sombra y la "píldora" verde clara en la opción elegida
                    NavigationBar(
                        containerColor = Color.White,
                        modifier = Modifier.shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), ambientColor = GreenDark.copy(alpha = 0.2f), spotColor = GreenDark.copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text("Inicio") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenMain, selectedTextColor = GreenDark, indicatorColor = GreenMain.copy(alpha = 0.14f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, null) },
                            label = { Text("Perfil") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenMain, selectedTextColor = GreenDark, indicatorColor = GreenMain.copy(alpha = 0.14f))
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    if (selectedTab == 0) MainContent(usuario) else PerfilScreen(usuario)
                }
            }
        }
    }

    // --- DIÁLOGOS (AYUDA Y CONTRASEÑA ADMIN) ---

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("OK") } },
            title = { Text("Ayuda y Soporte") },
            text = { Text("Contáctanos para reportar fallas:\n📧 soporte@truckonroute.com\n📞 771-123-4567") }
        )
    }

}

@Composable
fun MainContent(usuario: Usuario) {
    // Versión 2.8: se vuelve a usar la lista real de 83 colonias de Huauchinango (data/Colonias.kt, versión 2.5);
    // esta copia tenía otra vez la lista de ejemplo con colonias de Pachuca
    var textoBusqueda by remember { mutableStateOf("") }
    val filtradas = buscarColonias(textoBusqueda)

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundGray)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BUSCADOR
        SearchBarSection(textoBusqueda) { textoBusqueda = it }

        // SUGERENCIAS DE BÚSQUEDA
        // (no se muestran si ya se eligió una colonia de la lista)
        if (filtradas.isNotEmpty() && textoBusqueda !in filtradas) {
            TarjetaTor {
                Column(Modifier.padding(8.dp)) {
                    filtradas.forEach { col ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { textoBusqueda = col }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Place, null, tint = GreenMain, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(col)
                        }
                    }
                }
            }
        }

        // Truck on Rute 2.5: mapa con la ubicación real en lugar del camión animado con "23 min" fijo
        TruckMapSection()

        // Versión 2.8 (spec 004): avisos reales en lugar de textos fijos
        AvisosSection(usuario)
    }
}

@Composable
fun HeaderSection(onMenuClick: () -> Unit) {
    // Versión 2.9: el encabezado "flota" con sombra, esquinas más redondas, burbujas y el logo en un círculo
    val forma = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    Box(
        modifier = Modifier.fillMaxWidth()
            .shadow(12.dp, forma, ambientColor = GreenDark.copy(alpha = 0.35f), spotColor = GreenDark.copy(alpha = 0.35f))
            .clip(forma)
            .background(Brush.horizontalGradient(listOf(GreenMain, GreenDark)))
    ) {
        BurbujasEncabezado()
        Row(
            Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 22.dp, start = 20.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("TRUCK", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("ON ROUTE", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, letterSpacing = 3.sp)
                }
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, "Menú", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun SearchBarSection(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        // Versión 2.9: buscador en forma de píldora con sombra suave
        modifier = Modifier.fillMaxWidth().sombraSuave(radio = 28.dp, altura = 6.dp),
        placeholder = { Text("Buscar colonia...") },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = GreenMain) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, null) }
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedBorderColor = GreenMain, unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun TruckCardSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "truck")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -50f, targetValue = 50f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = ""
    )
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalShipping, null, tint = GreenMain, modifier = Modifier.size(80.dp).graphicsLayer { translationX = offsetX })
            Text("El camión llega en 23 min", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Próxima parada: Centro", color = Color.Gray)
        }
    }
}

@Composable
fun NotificationItem(t: String, d: String) {
    // Versión 2.9: tarjeta con sombra suave, franja verde a la izquierda e ícono con degradado
    ItemConIcono(Icons.Default.Notifications, t, d)
}

@Composable
fun MessageItem(f: String, m: String) {
    // Versión 2.9: mismo estilo que los avisos
    ItemConIcono(Icons.Default.Email, f, m)
}

@Composable
private fun ItemConIcono(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, texto: String) {
    TarjetaTor {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(DegradadoVerde))
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconoCirculo(icono)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(titulo, fontWeight = FontWeight.Bold)
                    Text(texto, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

// Versión 2.8 (spec 004, H19 y H20): avisos del camión y "Avisarme cuando esté cerca".
@Composable
fun AvisosSection(usuario: Usuario) {
    val contexto = LocalContext.current
    var avisos by remember { mutableStateOf<List<Aviso>>(emptyList()) }
    var avisarCerca by remember { mutableStateOf(Sesion.avisarCerca(contexto)) }
    var distancia by remember { mutableStateOf<Float?>(null) }
    var nota by remember { mutableStateOf<String?>(null) }

    // Con la app abierta revisa cada 20 s: avisos nuevos (y los notifica) y, si está activo, la distancia
    LaunchedEffect(avisarCerca) {
        while (true) {
            Vigilancia.revisarAvisos(contexto)
            avisos = ApiTor.avisos(null).sortedByDescending { it.fecha }.take(5)
            distancia = if (avisarCerca) Vigilancia.revisarCercania(contexto) else null
            delay(20_000)
        }
    }

    fun activar() {
        Sesion.guardarAvisarCerca(contexto, true)
        avisarCerca = true
        nota = null
        ServicioCercania.iniciar(contexto)
    }

    val pedirUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        if (resultado.values.any { it }) activar()
        else nota = "Sin permiso de ubicación no podemos saber si el camión está cerca de ti."
    }

    if (usuario.rol == "administrador") {
        NotificationItem("Eres administrador", "Tu panel está en la computadora: abre /panel en el navegador.")
    }

    TituloSeccion("Avisos", Icons.Default.Notifications)
    if (avisos.isEmpty()) {
        NotificationItem("Sin avisos por ahora", "Te avisamos cuando el camión salga a ruta.")
    } else {
        avisos.forEach { NotificationItem(it.titulo, it.mensaje) }
    }

    // Interruptor de cercanía
    // Versión 2.9: tarjeta con sombra suave e ícono
    TarjetaTor {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCirculo(Icons.Default.NearMe)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Avisarme cuando esté cerca", fontWeight = FontWeight.Bold)
                    Text("Cada 5 minutos revisamos si el camión está a menos de 500 m de ti", fontSize = 13.sp, color = Color.Gray)
                }
                Switch(
                    checked = avisarCerca,
                    onCheckedChange = { activo ->
                        if (activo) {
                            if (Permisos.hayUbicacion(contexto)) activar()
                            else pedirUbicacion.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        } else {
                            Sesion.guardarAvisarCerca(contexto, false)
                            avisarCerca = false
                            ServicioCercania.detener(contexto)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = GreenMain)
                )
            }
            val d = distancia
            if (avisarCerca && d != null) {
                // Versión 2.9: la distancia se muestra en una "píldora" verde clara
                Text(
                    if (d < 1000) "El camión está a ${d.toInt()} m de ti" else String.format(java.util.Locale.US, "El camión está a %.1f km de ti", d / 1000),
                    color = GreenDark, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp).background(GreenMain.copy(alpha = 0.12f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
            nota?.let { Text(it, color = Color(0xFFB00020), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}
