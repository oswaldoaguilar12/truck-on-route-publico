package com.example.truckonroute.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.truckonroute.data.ApiTor
import com.example.truckonroute.data.MENSAJES_RAPIDOS
import com.example.truckonroute.data.MensajeChat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

// Versión 2.14 (spec 010, FR-1009 a FR-1016): chat del chofer con el administrador.
// Mensajes rápidos de un toque, texto libre, foto (galería o cámara) y audio (mantener presionado).
// Las fotos y audios viajan en base64 dentro del JSON; la API revisa formato y tamaño (5 MB).

private const val REVISAR_CADA_MS = 5_000L   // FR-1016
private const val MAX_AUDIO_MS = 60_000      // FR-1010: máximo 60 s
private const val MAX_LADO_FOTO = 1280       // la foto se achica para que pese poco

@Composable
fun ChatScreen(token: String, miId: Int, onAtras: () -> Unit) {
    BackHandler(onBack = onAtras)
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var mensajes by remember { mutableStateOf<List<MensajeChat>?>(null) }
    var escrito by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var grabando by remember { mutableStateOf(false) }
    var menuFoto by remember { mutableStateOf(false) }
    var fotoGrande by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }
    val lista = rememberLazyListState()

    // Pide la conversación al abrir y cada 5 s (al pedirla, lo del administrador queda leído)
    LaunchedEffect(recargar) {
        while (true) {
            ApiTor.chat(token)?.let { mensajes = it }
            delay(REVISAR_CADA_MS)
        }
    }
    // Siempre se ve el último mensaje
    val total = mensajes?.size ?: 0
    LaunchedEffect(total) { if (total > 0) lista.animateScrollToItem(total - 1) }

    fun mandar(tipo: String, texto: String = "", archivo: String? = null) {
        alcance.launch {
            enviando = true
            val r = ApiTor.mandarChat(token, tipo, texto, archivo)
            enviando = false
            error = r.error
            if (r.ok) recargar++
        }
    }

    // Foto de la galería o de la cámara: se achica y se manda como JPG
    fun mandarFoto(bitmap: Bitmap?) {
        if (bitmap == null) return
        alcance.launch {
            val b64 = withContext(Dispatchers.Default) { aJpgBase64(bitmap) }
            mandar("foto", archivo = b64)
        }
    }
    fun mandarAudio(archivo: File) {
        alcance.launch {
            val b64 = withContext(Dispatchers.IO) { Base64.encodeToString(archivo.readBytes(), Base64.NO_WRAP) }
            mandar("audio", archivo = b64)
        }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) mandarFoto(leerImagen(contexto, uri))
    }
    // La cámara guarda la foto completa en un archivo de la caché (TakePicturePreview daba una foto muy chiquita)
    val fotoCamara = remember {
        File(contexto.cacheDir, "fotos").apply { mkdirs() }.let { File(it, "foto_chat.jpg") }
    }
    val uriCamara = remember {
        androidx.core.content.FileProvider.getUriForFile(contexto, contexto.packageName + ".archivos", fotoCamara)
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) mandarFoto(leerImagen(contexto, uriCamara))
    }

    // Audio: se pide el permiso del micrófono la primera vez
    val grabadora = remember { Grabadora(contexto) }
    DisposableEffect(Unit) { onDispose { grabadora.cancelar() } }
    val pedirMicrofono = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        error = if (ok) "Listo. Ahora mantén presionado el micrófono para grabar." else "Sin permiso del micrófono no se pueden mandar audios."
    }

    Column(Modifier.fillMaxSize().background(FondoTor)) {
        EncabezadoVerde {
            Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).background(Color.White.copy(alpha = 0.16f), CircleShape).clickable(onClick = onAtras),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowBack, "Regresar", tint = Color.White) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Chat", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("Administrador", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Conversación ---
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val actuales = mensajes
            when {
                actuales == null -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { Esqueleto(Modifier.fillMaxWidth(0.7f).height(48.dp), radio = 16.dp) }
                }
                actuales.isEmpty() -> EstadoVacio(
                    Icons.Default.ChatBubbleOutline, "Sin mensajes todavía",
                    "Avisa al administrador de una falla o una pausa con los botones de abajo.",
                    Modifier.padding(16.dp)
                )
                else -> LazyColumn(
                    state = lista,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(actuales, key = { it.id }) { m ->
                        Burbuja(m, mio = m.deId == miId, token = token, onVerFoto = { fotoGrande = it })
                    }
                }
            }
        }

        error?.let { Text(it, color = AmarilloTextoTor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp)) }
        if (grabando) {
            Text("● Grabando… suelta para mandar", color = RojoTor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
        }

        // --- Mensajes rápidos (FR-1010) ---
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(MENSAJES_RAPIDOS) { r ->
                val urgente = r == "Falla mecánica" || r == "Necesito ayuda"
                val color = if (urgente) RojoTor else VerdeOscuroTor
                Text(
                    r, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .border(1.5.dp, if (urgente) RojoTor else VerdeTor, RoundedCornerShape(50))
                        .clickable(enabled = !enviando) { mandar("rapido", r) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }

        // --- Escribir, foto y audio ---
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                BotonCirculo(Icons.Default.PhotoCamera, "Mandar foto", VerdeClaroTor, VerdeOscuroTor) { menuFoto = true }
                DropdownMenu(expanded = menuFoto, onDismissRequest = { menuFoto = false }) {
                    DropdownMenuItem(text = { Text("Tomar foto") }, leadingIcon = { Icon(Icons.Default.PhotoCamera, null) },
                        onClick = { menuFoto = false; camara.launch(uriCamara) })
                    DropdownMenuItem(text = { Text("Elegir de la galería") }, leadingIcon = { Icon(Icons.Default.PhotoLibrary, null) },
                        onClick = { menuFoto = false; galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
                }
            }
            OutlinedTextField(
                value = escrito, onValueChange = { if (it.length <= 1000) escrito = it },
                placeholder = { Text("Escribe un mensaje…", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = {
                    if (escrito.isNotBlank()) { mandar("texto", escrito.trim()); escrito = "" }
                }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeTor, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
            if (escrito.isNotBlank()) {
                BotonCirculo(Icons.Default.Send, "Enviar", VerdeTor, Color.White) {
                    mandar("texto", escrito.trim()); escrito = ""
                }
            } else {
                // Mantener presionado para grabar; al soltar se manda (FR-1010)
                Box(
                    Modifier
                        .size(46.dp)
                        .background(if (grabando) RojoTor else VerdeTor, CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                    pedirMicrofono.launch(Manifest.permission.RECORD_AUDIO)
                                    return@detectTapGestures
                                }
                                val inicio = System.currentTimeMillis()
                                grabando = grabadora.empezar()
                                if (!grabando) { error = "No se pudo usar el micrófono"; return@detectTapGestures }
                                tryAwaitRelease()
                                grabando = false
                                val archivo = grabadora.terminar()
                                if (System.currentTimeMillis() - inicio < 700 || archivo == null) {
                                    error = "Mantén presionado el micrófono para grabar"
                                } else {
                                    error = null
                                    mandarAudio(archivo)
                                }
                            })
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Mic, "Mantén presionado para grabar", tint = Color.White) }
            }
        }
    }

    // Foto en grande al tocarla
    fotoGrande?.let { nombre ->
        Dialog(onDismissRequest = { fotoGrande = null }) {
            AsyncImage(
                model = ApiTor.urlArchivo(nombre, token), contentDescription = "Foto",
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { fotoGrande = null },
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@Composable
private fun BotonCirculo(icono: androidx.compose.ui.graphics.vector.ImageVector, desc: String, fondo: Color, color: Color, onClick: () -> Unit) {
    Box(Modifier.size(46.dp).clip(CircleShape).background(fondo).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icono, desc, tint = color)
    }
}

// Un mensaje: verde a la derecha si es mío, blanco a la izquierda si es del administrador.
// Los rápidos urgentes van en rojo.
@Composable
private fun Burbuja(m: MensajeChat, mio: Boolean, token: String, onVerFoto: (String) -> Unit) {
    val urgente = m.tipo == "rapido" && (m.texto == "Falla mecánica" || m.texto == "Necesito ayuda")
    val fondo = when { urgente -> RojoTor; mio -> VerdeTor; else -> Color.White }
    val colorTexto = if (mio || urgente) Color.White else TextoOscuroTor
    val forma = if (mio) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mio) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 280.dp).background(fondo, forma).padding(if (m.tipo == "foto") 4.dp else 10.dp)
        ) {
            when (m.tipo) {
                "foto" -> m.archivo?.let { nombre ->
                    AsyncImage(
                        model = ApiTor.urlArchivo(nombre, token), contentDescription = "Foto",
                        modifier = Modifier.size(width = 220.dp, height = 150.dp).clip(RoundedCornerShape(12.dp)).clickable { onVerFoto(nombre) },
                        contentScale = ContentScale.Crop
                    )
                }
                "audio" -> m.archivo?.let { ReproductorAudio(ApiTor.urlArchivo(it, token), mio || urgente) }
                "rapido" -> Text("⚠ ${m.texto}", color = colorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                else -> Text(m.texto, color = colorTexto, fontSize = 14.sp, lineHeight = 19.sp)
            }
            Text(
                horaCorta(m.fecha) + if (mio && m.leido) " · Visto" else "",
                color = colorTexto.copy(alpha = 0.7f), fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End).padding(horizontal = if (m.tipo == "foto") 6.dp else 0.dp, vertical = 2.dp)
            )
        }
    }
}

// Botón de reproducir/pausar un audio del chat (MediaPlayer lo baja directo de la API)
@Composable
private fun ReproductorAudio(url: String, sobreColor: Boolean) {
    var sonando by remember { mutableStateOf(false) }
    val reproductor = remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(url) { onDispose { reproductor.value?.release(); reproductor.value = null } }
    val color = if (sobreColor) Color.White else VerdeTor
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(200.dp)) {
        Box(
            Modifier.size(34.dp).background(if (sobreColor) Color.White else VerdeTor, CircleShape).clickable {
                val mp = reproductor.value
                if (sonando && mp != null) { mp.pause(); sonando = false; return@clickable }
                if (mp != null) { mp.start(); sonando = true; return@clickable }
                try {
                    MediaPlayer().apply {
                        setDataSource(url)
                        setOnPreparedListener { start(); sonando = true }
                        setOnCompletionListener { sonando = false; seekTo(0) }
                        setOnErrorListener { _, _, _ -> sonando = false; true }
                        prepareAsync()
                    }.also { reproductor.value = it }
                } catch (e: Exception) { sonando = false }
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(if (sonando) Icons.Default.Pause else Icons.Default.PlayArrow, if (sonando) "Pausar" else "Escuchar",
                tint = if (sobreColor) VerdeTor else Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        // Rayitas tipo onda (solo de adorno)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(8, 16, 11, 20, 9, 14, 18, 7, 13, 17, 9, 12).forEach { h ->
                Box(Modifier.size(width = 3.dp, height = h.dp).background(color.copy(alpha = if (sonando) 1f else 0.7f), RoundedCornerShape(2.dp)))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("Audio", color = color, fontSize = 11.sp)
    }
}

// ---------- Ayudantes ----------

// Graba con el micrófono a un archivo M4A (AAC) en la caché de la app
private class Grabadora(private val contexto: Context) {
    private var grabadora: MediaRecorder? = null
    private var archivo: File? = null

    fun empezar(): Boolean = try {
        val f = File(contexto.cacheDir, "audio_chat.m4a")
        val g = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(contexto) else @Suppress("DEPRECATION") MediaRecorder()
        g.setAudioSource(MediaRecorder.AudioSource.MIC)
        g.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        g.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        g.setAudioSamplingRate(44_100)
        g.setAudioEncodingBitRate(64_000)
        g.setMaxDuration(MAX_AUDIO_MS) // a los 60 s deja de grabar solo
        g.setOutputFile(f.absolutePath)
        g.prepare()
        g.start()
        grabadora = g
        archivo = f
        true
    } catch (e: Exception) {
        cancelar()
        false
    }

    // Devuelve el archivo grabado, o null si falló (por ejemplo, si fue muy corto)
    fun terminar(): File? {
        val g = grabadora ?: return null
        grabadora = null
        return try {
            g.stop()
            g.release()
            archivo?.takeIf { it.exists() && it.length() > 0 }
        } catch (e: Exception) {
            g.release()
            null
        }
    }

    fun cancelar() {
        try { grabadora?.release() } catch (e: Exception) { }
        grabadora = null
    }
}

// Lee la foto de la galería o de la cámara (achicada al leerla para no gastar memoria)
// y la endereza si el celular la guardó girada
private fun leerImagen(contexto: Context, uri: Uri): Bitmap? = try {
    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
    var escala = 1
    while (maxOf(opciones.outWidth, opciones.outHeight) / (escala * 2) >= MAX_LADO_FOTO) escala *= 2
    val foto = contexto.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = escala })
    }
    val giro = contexto.contentResolver.openInputStream(uri)?.use {
        when (android.media.ExifInterface(it).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1)) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f
    if (foto != null && giro != 0f) {
        Bitmap.createBitmap(foto, 0, 0, foto.width, foto.height, android.graphics.Matrix().apply { postRotate(giro) }, true)
    } else foto
} catch (e: Exception) { null }

// La foto a JPG (calidad 80, máximo 1280 px por lado) y luego a base64
private fun aJpgBase64(original: Bitmap): String {
    val lado = maxOf(original.width, original.height)
    val foto = if (lado > MAX_LADO_FOTO) {
        val f = MAX_LADO_FOTO.toFloat() / lado
        Bitmap.createScaledBitmap(original, (original.width * f).toInt(), (original.height * f).toInt(), true)
    } else original
    val salida = ByteArrayOutputStream()
    foto.compress(Bitmap.CompressFormat.JPEG, 80, salida)
    return Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
}

// "2026-11-27T10:12:00.000Z" -> "10:12" (hora del celular)
private fun horaCorta(iso: String): String =
    fechaDeAviso(iso)?.let { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(it) } ?: ""
