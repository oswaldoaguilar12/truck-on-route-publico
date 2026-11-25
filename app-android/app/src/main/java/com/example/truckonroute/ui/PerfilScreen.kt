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
fun PerfilScreen(usuario: Usuario, onLogout: () -> Unit = {}) {
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

    // Versión 2.10 (spec 006): encabezado verde, tarjeta encimada con el avatar y chip del rol,
    // y una lista con los datos. Editar sigue cambiando solo lo que se ve (guardar en la API es v3.0).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoTor)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoVerde {
            Text(
                "Mi perfil", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 120.dp)
            )
        }

        // --- TARJETA CON LA FOTO, EL NOMBRE Y EL ROL ---
        Column(
            Modifier.fillMaxWidth().offset(y = (-70).dp).padding(horizontal = 16.dp).entradaSuave(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                TarjetaTor(Modifier.padding(top = 48.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 58.dp, bottom = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            Text(nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuroTor)
                            Text(correo, fontSize = 14.sp, color = TextoGrisTor)
                        }
                        Spacer(Modifier.height(10.dp))
                        EtiquetaEstado(rol, VerdeOscuroTor, conPunto = false)
                    }
                }

                // --- FOTO DE PERFIL: la inicial del nombre o la foto elegida ---
                Box(contentAlignment = Alignment.BottomEnd) {
                    val forma = CircleShape
                    Box(
                        Modifier.size(96.dp)
                            .shadow(12.dp, forma, ambientColor = VerdeOscuroTor.copy(alpha = 0.4f), spotColor = VerdeOscuroTor.copy(alpha = 0.4f))
                            .background(Color.White, forma)
                            .padding(4.dp) // borde blanco de 4 dp
                            .clip(forma)
                            .background(VerdeClaroTor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(nombre.take(1).uppercase().ifBlank { "?" }, color = VerdeOscuroTor, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // BOTÓN PARA CAMBIAR FOTO (Solo aparece si estamos editando)
                    if (isEditing) {
                        IconButton(
                            onClick = { launcher.launch("image/*") }, // Abre la galería
                            modifier = Modifier
                                .size(34.dp)
                                .background(VerdeOscuroTor, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // --- DATOS ---
            TarjetaTor {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    if (isEditing) {
                        Spacer(Modifier.height(10.dp))
                        EditField("Correo", correo, Icons.Default.Email) { correo = it }
                        EditField("Teléfono", telefono, Icons.Default.Phone) { telefono = it }
                        EditField("Colonia", direccion, Icons.Default.LocationOn) { direccion = it }
                    } else {
                        ItemPerfil("Colonia", direccion, Icons.Default.LocationOn)
                        HorizontalDivider(color = Color(0xFFEEF0EE))
                        ItemPerfil("Teléfono", telefono, Icons.Default.Phone)
                        HorizontalDivider(color = Color(0xFFEEF0EE))
                        ItemPerfil("Rol", rol, Icons.Default.Person)
                    }
                }
            }

            // --- BOTONES ---
            BotonBorde(
                texto = if (isEditing) "Guardar cambios" else "Editar perfil",
                icono = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                onClick = { isEditing = !isEditing }
            )
            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Default.Logout, null, tint = RojoTor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = RojoTor, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }
    }
}

// Versión 2.10: campos claros (antes eran blancos sobre el fondo verde)
@Composable
fun EditField(label: String, value: String, icono: ImageVector, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icono, null, tint = VerdeOscuroTor) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FondoTor,
            unfocusedContainerColor = FondoTor,
            focusedBorderColor = VerdeTor,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = VerdeOscuroTor
        ),
        singleLine = true
    )
    Spacer(Modifier.height(10.dp))
}

// Versión 2.10: fila con el ícono en un cuadrito verde claro, el título gris y el dato
@Composable
fun ItemPerfil(titulo: String, contenido: String, icono: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(VerdeClaroTor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = VerdeOscuroTor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(titulo, color = TextoGrisTor, fontSize = 12.sp)
            Text(contenido, color = TextoOscuroTor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
