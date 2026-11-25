package com.example.truckonroute.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truckonroute.data.Usuario
import coil.compose.AsyncImage // Si no tienes Coil, te daré una alternativa abajo

@Composable
fun PerfilScreen(usuario: Usuario) {
    // 1. Estados para los datos
    // T510 (FR-507): se llenan con la cuenta que inició sesión, ya no con datos fijos.
    // Editar solo cambia lo que se ve en pantalla; guardarlo en la API queda para v3.0.
    var nombre by remember(usuario) { mutableStateOf(usuario.nombre) }
    var correo by remember(usuario) { mutableStateOf(usuario.correo) }
    var telefono by remember(usuario) { mutableStateOf(usuario.telefono.ifBlank { "Sin teléfono" }) }
    var direccion by remember(usuario) { mutableStateOf(usuario.colonia.ifBlank { "Sin colonia" }) }
    val rol = when (usuario.rol) {
        "administrador" -> "Administrador"
        "conductor" -> "Chofer"
        else -> "Ciudadano"
    }
    var isEditing by remember { mutableStateOf(false) }

    // 2. ESTADO PARA LA FOTO (Guarda la dirección de la imagen)
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 3. LANZADOR DE GALERÍA
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri // Guardamos la imagen seleccionada
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // --- FOTO DE PERFIL ---
        // Versión 2.9: la foto "flota" con una sombra verde suave
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.shadow(12.dp, CircleShape, ambientColor = Color(0xFF0A7D2C).copy(alpha = 0.4f), spotColor = Color(0xFF0A7D2C).copy(alpha = 0.4f))) {
            if (imageUri != null) {
                // Si seleccionó una foto, la mostramos
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF0A7D2C), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Foto por defecto
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF0A7D2C), CircleShape)
                        .background(Color.White),
                    tint = Color.Gray
                )
            }

            // BOTÓN PARA CAMBIAR FOTO (Solo aparece si estamos editando)
            if (isEditing) {
                IconButton(
                    onClick = { launcher.launch("image/*") }, // Abre la galería
                    modifier = Modifier
                        .size(35.dp)
                        .background(Color(0xFF0A7D2C), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (isEditing) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(0.8f),
                singleLine = true
            )
        } else {
            Text(nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A7D2C))
            Text(correo, fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(25.dp))

        // --- CONTENEDOR VERDE ---
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                // Versión 2.9: sombra, esquinas de 24 dp y el degradado de los dos verdes de la app
                .shadow(14.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF0A7D2C).copy(alpha = 0.35f), spotColor = Color(0xFF0A7D2C).copy(alpha = 0.35f))
                .background(DegradadoVerde, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text(
                text = if (isEditing) "Editando Información" else "Información personal",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (isEditing) {
                EditField("Correo", correo, Icons.Default.Email) { correo = it }
                EditField("Teléfono", telefono, Icons.Default.Phone) { telefono = it }
                EditField("Colonia", direccion, Icons.Default.LocationOn) { direccion = it }
            } else {
                ItemPerfil("Teléfono", telefono, Icons.Default.Phone)
                ItemPerfil("Colonia", direccion, Icons.Default.LocationOn)
                ItemPerfil("Rol", rol, Icons.Default.Person)
            }

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditing) Color(0xFF7ED957) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF0A7D2C)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Guardar Cambios" else "Editar Perfil",
                    color = Color(0xFF0A7D2C),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun EditField(label: String, value: String, icono: ImageVector, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        leadingIcon = { Icon(icono, null, tint = Color.White) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
        ),
        singleLine = true
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
fun ItemPerfil(titulo: String, contenido: String, icono: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Versión 2.9: el ícono va en un círculo blanco transparente
        Box(Modifier.size(40.dp).background(Color.White.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(titulo, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(contenido, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}