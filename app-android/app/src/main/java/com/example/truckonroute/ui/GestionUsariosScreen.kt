package com.example.truckonroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GestionUsuariosScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
            .verticalScroll(rememberScrollState())
    ) {
        // Truck on Rute 2.5: se quitó el encabezado verde de aquí porque la pantalla
        // ya tiene la barra "Panel Administrativo" arriba (se veían dos encabezados).
        Spacer(modifier = Modifier.height(15.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Gestión de Usuarios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Buscador + botón
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Buscar") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFD6D0CD),
                        unfocusedContainerColor = Color(0xFFD6D0CD),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { /* añadir usuario */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A7D2C)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Añadir")
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Lista de usuarios (datos ficticios de ejemplo, T034)
            UsuarioItem("Laura Méndez", "laura.mendez@ejemplo.com", "Administrador")
            UsuarioItem("Ana López", "ana.lopez@ejemplo.com", "Usuario")
            UsuarioItem("Carlos Ruiz", "carlos.ruiz@ejemplo.com", "Editor")
            UsuarioItem("Pedro Sánchez", "pedro.sanchez@ejemplo.com", "Usuario")

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Notificaciones (4)", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp) // Aumenté altura para que se vea
                        .padding(vertical = 5.dp)
                        .background(Color.LightGray, RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

// Truck on Rute 2.5: cada usuario en una tarjeta con su inicial, el correo debajo del nombre
// y el rol como etiqueta. Antes eran tres columnas apretadas con letra de 10–12 sp.
@Composable
fun UsuarioItem(nombre: String, correo: String, rol: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo con la inicial del nombre
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF0A7D2C).copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(nombre.take(1), color = Color(0xFF0A7D2C), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(correo, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Etiqueta del rol
            Text(
                text = rol,
                fontSize = 12.sp,
                color = Color(0xFF0A7D2C),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(Color(0xFF0A7D2C).copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
