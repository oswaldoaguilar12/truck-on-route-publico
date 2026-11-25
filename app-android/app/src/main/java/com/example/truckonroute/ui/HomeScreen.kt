package com.example.truckonroute.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
                // Versión 2.10 (FR-618): encabezado del menú con el logo nuevo y quién inició sesión
                Box(
                    modifier = Modifier.fillMaxWidth().background(DegradadoVerde)
                ) {
                    BurbujasEncabezado()
                    Column(Modifier.statusBarsPadding().padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LogoTor(Modifier.size(48.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Truck on Route", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Text(usuario.nombre.take(1).uppercase().ifBlank { "?" }, color = GreenDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(usuario.nombre, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(usuario.correo, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
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
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    // v2.10: la opción elegida en verde claro (antes salía el morado que trae Material)
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = GreenMain.copy(alpha = 0.14f),
                        selectedIconColor = GreenDark,
                        selectedTextColor = GreenDark,
                        unselectedIconColor = TextoGrisTor
                    )
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
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    // v2.10: la opción elegida en verde claro (antes salía el morado que trae Material)
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = GreenMain.copy(alpha = 0.14f),
                        selectedIconColor = GreenDark,
                        selectedTextColor = GreenDark,
                        unselectedIconColor = TextoGrisTor
                    )
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
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    // v2.10: la opción elegida en verde claro (antes salía el morado que trae Material)
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = GreenMain.copy(alpha = 0.14f),
                        selectedIconColor = GreenDark,
                        selectedTextColor = GreenDark,
                        unselectedIconColor = TextoGrisTor
                    )
                )
            }
        }
    ) {
        // --- CONTENIDO ---
        run {
            // Versión 2.10 (spec 006): sin encabezado; en Inicio el mapa llega hasta arriba
            // (contentWindowInsets = 0: cada pantalla deja su propio espacio para la barra de estado)
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    // Versión 2.10 (FR-615): Inicio y Perfil cambian deslizándose en lugar de brincar
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val lado = if (targetState > initialState) 1 else -1
                            (slideInHorizontally(tween(280)) { ancho -> lado * ancho / 4 } + fadeIn(tween(280))) togetherWith
                                fadeOut(tween(160))
                        },
                        label = "pestanas"
                    ) { pestana ->
                        if (pestana == 0) MainContent(usuario, onMenuClick = { scope.launch { drawerState.open() } })
                        else PerfilScreen(usuario, onLogout)
                    }
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

// Versión 2.10 (spec 006, FR-601): el mapa ocupa casi toda la pantalla.
// Encima flotan el buscador, el botón del menú y la etiqueta del estado.
// Abajo, una hoja que se desliza con la distancia, los datos del recorrido y los avisos.
private val ALTO_HOJA_CERRADA = 250.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(usuario: Usuario, onMenuClick: () -> Unit = {}) {
    // Versión 2.8: se vuelve a usar la lista real de 83 colonias de Huauchinango (data/Colonias.kt, versión 2.5)
    var textoBusqueda by remember { mutableStateOf("") }
    val filtradas = buscarColonias(textoBusqueda)

    val datos = rememberDatosCamion()
    var seguirCamion by remember { mutableStateOf(true) } // false: el usuario movió el mapa a mano
    val estado = calcularEstado(datos.ubicacion, datos.cargando, datos.ahora)

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetPeekHeight = ALTO_HOJA_CERRADA,
        sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        sheetContainerColor = Color.White,
        sheetShadowElevation = 16.dp,
        sheetDragHandle = {
            // La rayita gris que indica que la hoja se puede deslizar
            Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(width = 44.dp, height = 5.dp).background(Color(0xFFD6DCD8), RoundedCornerShape(3.dp)))
        },
        sheetContent = { HojaCamion(usuario, datos, estado) },
        containerColor = BackgroundGray
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            // Truck on Rute 2.5: mapa con la ubicación real (ahora a pantalla completa)
            MapaCamion(
                datos = datos,
                seguirCamion = seguirCamion,
                onMoverMapa = { seguirCamion = false },
                modifier = Modifier.fillMaxSize()
            )

            // Buscador, menú, sugerencias y etiquetas, flotando sobre el mapa
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.entradaSuave()) {
                    Box(Modifier.weight(1f)) { SearchBarSection(textoBusqueda) { textoBusqueda = it } }
                    Spacer(Modifier.width(10.dp))
                    BotonRedondo(Icons.Default.Menu, "Menú", onMenuClick, fondo = GreenMain, colorIcono = Color.White)
                }

                // SUGERENCIAS DE BÚSQUEDA (no se muestran si ya se eligió una colonia de la lista)
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
                } else {
                    // FR-603: estado del camión como etiqueta de color, y la colonia del usuario
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.entradaSuave(retrasoMs = 100)) {
                        EtiquetaEstado(estado.etiqueta, estado.color, solida = true)
                        if (usuario.colonia.isNotBlank()) {
                            EtiquetaEstado(usuario.colonia, TextoOscuroTor, conPunto = false,
                                modifier = Modifier.shadow(6.dp, RoundedCornerShape(50)).background(Color.White, RoundedCornerShape(50)))
                        }
                    }
                }
            }

            // Versión 2.6: botón para volver a centrar el mapa en el camión (arriba de la hoja)
            BotonRedondo(
                Icons.Default.MyLocation, "Centrar en el camión",
                onClick = { seguirCamion = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = ALTO_HOJA_CERRADA + 16.dp),
                colorIcono = if (seguirCamion) GreenMain else GreenDark
            )
        }
    }
}

// Botón redondo flotante (menú y centrar)
@Composable
private fun BotonRedondo(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fondo: Color = Color.White,
    colorIcono: Color = GreenDark
) {
    val interaccion = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier
            .size(52.dp)
            .alPresionar(interaccion)
            .shadow(10.dp, CircleShape, ambientColor = fondo.copy(alpha = 0.5f), spotColor = GreenDark.copy(alpha = 0.35f))
            .background(fondo, CircleShape)
            .clip(CircleShape)
            .clickable(interactionSource = interaccion, indication = LocalIndication.current, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, descripcion, tint = colorIcono, modifier = Modifier.size(24.dp))
    }
}

// Contenido de la hoja inferior de Inicio
@Composable
private fun HojaCamion(usuario: Usuario, datos: DatosCamion, estado: EstadoCamion) {
    val contexto = LocalContext.current
    var avisos by remember { mutableStateOf<List<Aviso>>(emptyList()) }
    var avisarCerca by remember { mutableStateOf(Sesion.avisarCerca(contexto)) }
    var distancia by remember { mutableStateOf<Float?>(null) }
    var nota by remember { mutableStateOf<String?>(null) }
    var avisosListos by remember { mutableStateOf(false) } // v2.10: false = todavía no llegan (se ven esqueletos)
    val vibrar = LocalHapticFeedback.current

    // Versión 2.10 (FR-616): cuando llega un aviso nuevo (no la primera vez), el celular vibra un poquito
    val idMasNuevo = avisos.firstOrNull()?.id
    var idAnterior by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(idMasNuevo) {
        if (idMasNuevo != null && idAnterior != null && idMasNuevo != idAnterior) {
            vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (idMasNuevo != null) idAnterior = idMasNuevo
    }

    // Versión 2.8 (spec 004, H19 y H20): con la app abierta revisa cada 20 s
    // avisos nuevos (y los notifica) y, si está activo, la distancia
    LaunchedEffect(avisarCerca) {
        while (true) {
            Vigilancia.revisarAvisos(contexto)
            avisos = ApiTor.avisos(null).sortedByDescending { it.fecha }.take(5)
            avisosListos = true
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

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Título grande: la distancia (si está activo el aviso) o el estado del camión ---
        val d = distancia
        // FR-614: mientras llega la primera respuesta se ven esqueletos en lugar del título y los datos
        if (datos.cargando) {
            Esqueleto(Modifier.width(120.dp).height(14.dp))
            Esqueleto(Modifier.fillMaxWidth(0.7f).height(30.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { Esqueleto(Modifier.weight(1f).height(64.dp), radio = 18.dp) }
            }
        } else Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (avisarCerca && d != null && datos.ubicacion != null) {
                    Text("El camión está a", fontSize = 13.sp, color = TextoGrisTor)
                    Text(
                        if (d < 1000) "${d.toInt()} m de ti" else String.format(java.util.Locale.US, "%.1f km de ti", d / 1000),
                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenDark, lineHeight = 32.sp
                    )
                } else {
                    Text(estado.hora?.let { "Última señal: $it" } ?: "¿Dónde está el camión?", fontSize = 13.sp, color = TextoGrisTor)
                    Text(estado.titulo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor, lineHeight = 28.sp)
                }
            }
            // La etiqueta solo se pone junto a la distancia; si el título ya dice el estado, sería repetirlo
            if (estado.hora != null && avisarCerca && d != null && datos.ubicacion != null) {
                EtiquetaEstado("${estado.etiqueta} · ${estado.hora}", estado.color, conPunto = false)
            }
        }

        // --- Versión 2.6: velocidad, distancia y puntos que calcula la API, en tres cuadritos ---
        val e = datos.estadisticas
        if (datos.ubicacion != null && e != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DatoHoja(e.velocidadKmh?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "--", "km/h", Modifier.weight(1f))
                DatoHoja(String.format(java.util.Locale.US, "%.2f", e.distanciaKm), "km recorridos", Modifier.weight(1f))
                DatoHoja("${e.puntosRecibidos}", "puntos", Modifier.weight(1f))
            }
        }

        if (usuario.rol == "administrador") {
            FilaAviso(Icons.Default.AdminPanelSettings, "Eres administrador", "Tu panel está en la computadora: abre /panel en el navegador.", destacado = false)
        }

        // --- Avisos ---
        Text("Avisos", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextoOscuroTor)
        if (!avisosListos) {
            repeat(2) { Esqueleto(Modifier.fillMaxWidth().height(64.dp), radio = 18.dp) }
        } else if (avisos.isEmpty()) {
            EstadoVacio(Icons.Default.NotificationsNone, "Sin avisos por ahora", "Te avisamos cuando el camión salga a ruta.")
        } else {
            // El más nuevo va destacado en verde claro
            avisos.forEachIndexed { i, a ->
                // El más nuevo rebota cuando llega (FR-616)
                val efecto = if (i == 0) Modifier.rebote(a.id) else Modifier.entradaSuave(retrasoMs = i * 60)
                FilaAviso(Icons.Default.Notifications, a.titulo, a.mensaje, destacado = i == 0, modifier = efecto)
            }
        }

        // --- Interruptor de cercanía ---
        Row(
            Modifier.fillMaxWidth().background(FondoTor, RoundedCornerShape(18.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).background(VerdeClaroTor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.NearMe, null, tint = GreenMain, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Avisarme cuando esté cerca", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextoOscuroTor)
                Text("Cada 5 minutos revisamos si el camión está a menos de 500 m de ti", fontSize = 12.sp, color = TextoGrisTor, lineHeight = 16.sp)
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
        nota?.let { Text(it, color = Color(0xFFB00020), fontSize = 13.sp) }
    }
}

// Cuadrito con un número grande y su descripción
@Composable
private fun DatoHoja(valor: String, titulo: String, modifier: Modifier = Modifier) {
    Column(modifier.background(FondoTor, RoundedCornerShape(18.dp)).padding(12.dp)) {
        Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor)
        Text(titulo, fontSize = 12.sp, color = TextoGrisTor)
    }
}

// Un aviso: ícono en círculo, título y texto. El destacado va en verde claro con el ícono en verde
@Composable
private fun FilaAviso(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    texto: String,
    destacado: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().background(if (destacado) VerdeClaroTor else FondoTor, RoundedCornerShape(18.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(if (destacado) GreenMain else Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = if (destacado) Color.White else GreenMain, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (destacado) GreenDark else TextoOscuroTor)
            Text(texto, fontSize = 12.sp, color = TextoGrisTor, lineHeight = 16.sp)
        }
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
        modifier = Modifier.fillMaxWidth().height(54.dp).shadow(10.dp, RoundedCornerShape(28.dp), spotColor = GreenDark.copy(alpha = 0.3f)),
        placeholder = { Text("Busca tu colonia") },
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
