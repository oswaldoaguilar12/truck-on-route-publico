package com.example.truckonroute.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.truckonroute.R

// Versión 2.10 (spec 006, FR-602): la app usa la fuente Poppins.
// Los archivos .ttf están en res/font (vienen dentro de la app, no se descargan).
// Licencia: SIL Open Font License, ver app-android/app/LICENCIA-Poppins-OFL.txt
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_bold, FontWeight.ExtraBold) // no hay ExtraBold: se usa Bold
)

// Se cambia la fuente de todos los estilos de Material 3, así todos los Text() la usan sin tocarlos uno por uno
private val base = Typography()

private fun TextStyle.conPoppins() = copy(fontFamily = Poppins)

val TipografiaTor = Typography(
    displayLarge = base.displayLarge.conPoppins(),
    displayMedium = base.displayMedium.conPoppins(),
    displaySmall = base.displaySmall.conPoppins(),
    headlineLarge = base.headlineLarge.conPoppins(),
    headlineMedium = base.headlineMedium.conPoppins(),
    headlineSmall = base.headlineSmall.conPoppins(),
    titleLarge = base.titleLarge.conPoppins(),
    titleMedium = base.titleMedium.conPoppins(),
    titleSmall = base.titleSmall.conPoppins(),
    bodyLarge = base.bodyLarge.conPoppins(),
    bodyMedium = base.bodyMedium.conPoppins(),
    bodySmall = base.bodySmall.conPoppins(),
    labelLarge = base.labelLarge.conPoppins(),
    labelMedium = base.labelMedium.conPoppins(),
    labelSmall = base.labelSmall.conPoppins()
)
