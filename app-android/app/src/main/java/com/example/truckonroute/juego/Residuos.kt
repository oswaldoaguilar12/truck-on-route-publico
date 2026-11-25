package com.example.truckonroute.juego

import androidx.compose.ui.graphics.Color

// Versión 2.11 (spec 007): datos del memorama "¿Dónde va?".
// Todo vive en la app: no usa internet ni la API.

// Las 4 categorías, con los colores de la norma de separación (NADF-024).
// Estos colores se usan solo dentro del juego.
enum class Categoria(val nombre: String, val color: Color, val fondo: Color, val emoji: String, val ejemplos: String) {
    ORGANICO("Orgánicos", Color(0xFF2E9E4F), Color(0xFFE3F4E8), "🍌", "Restos de comida, cáscaras, hojas"),
    RECICLABLE("Reciclables", Color(0xFF7A8580), Color(0xFFECEFEE), "🧴", "PET, latas, cartón, papel"),
    NO_RECICLABLE("No reciclables", Color(0xFFE8731A), Color(0xFFFDEBDD), "🧻", "Pañales, unicel, servilletas"),
    MANEJO_ESPECIAL("Manejo especial", Color(0xFF8B5A3C), Color(0xFFF1E6DD), "🔋", "Pilas, celulares, aceite, medicinas")
}

// Un residuo del juego: su dibujo (emoji), en qué bote va y un consejo corto
data class Residuo(val nombre: String, val emoji: String, val categoria: Categoria, val consejo: String)

// FR-703 / SC-703: los 20 residuos del juego (5 por categoría)
val RESIDUOS = listOf(
    Residuo("Cáscara de plátano", "🍌", Categoria.ORGANICO, "Las cáscaras se vuelven abono. Sepáralas en el bote de orgánicos."),
    Residuo("Restos de comida", "🍲", Categoria.ORGANICO, "Sin plásticos ni servilletas, van con los orgánicos y sirven para hacer composta."),
    Residuo("Cáscara de huevo", "🥚", Categoria.ORGANICO, "Le da calcio a la tierra. Va con los orgánicos."),
    Residuo("Hojas secas", "🍂", Categoria.ORGANICO, "Las hojas y ramas del jardín son orgánicas y sirven para composta."),
    Residuo("Café molido usado", "☕", Categoria.ORGANICO, "El café usado es orgánico y es buen abono para las plantas."),

    Residuo("Botella de plástico (PET)", "🧴", Categoria.RECICLABLE, "Vacíala, enjuágala y aplástala antes de tirarla. Así ocupa menos espacio."),
    Residuo("Periódico", "📰", Categoria.RECICLABLE, "Mantenlo seco y limpio: el papel mojado o con grasa ya no se recicla."),
    Residuo("Caja de cartón", "📦", Categoria.RECICLABLE, "Desármala y aplánala. Si tiene grasa, como la de pizza, ya no se recicla."),
    Residuo("Lata de aluminio", "🥫", Categoria.RECICLABLE, "El aluminio se recicla una y otra vez. Enjuágala y aplástala."),
    Residuo("Envase de jugo", "🧃", Categoria.RECICLABLE, "Enjuágalo y aplánalo. Con él se hacen cartón y láminas."),

    Residuo("Pañal usado", "👶", Categoria.NO_RECICLABLE, "No se puede reciclar. Envuélvelo bien y va al bote de no reciclables."),
    Residuo("Servilleta usada", "🧻", Categoria.NO_RECICLABLE, "El papel sucio o usado ya no se recicla. Va con los no reciclables."),
    Residuo("Charola de unicel", "🥡", Categoria.NO_RECICLABLE, "El unicel casi no se recicla. Mejor lleva tu propio recipiente cuando compres comida."),
    Residuo("Envoltura de dulce", "🍬", Categoria.NO_RECICLABLE, "Las envolturas de dulces y frituras tienen capas mezcladas y no se reciclan."),
    Residuo("Colilla de cigarro", "🚬", Categoria.NO_RECICLABLE, "Tiene químicos tóxicos. Apágala bien y tírala al bote, nunca al suelo."),

    Residuo("Pila", "🔋", Categoria.MANEJO_ESPECIAL, "Las pilas contaminan el agua y la tierra. Júntalas en un frasco y llévalas a un punto de acopio."),
    Residuo("Celular viejo", "📱", Categoria.MANEJO_ESPECIAL, "Tiene metales que se pueden recuperar. Llévalo a un centro de acopio de electrónicos."),
    Residuo("Foco ahorrador", "💡", Categoria.MANEJO_ESPECIAL, "Tiene mercurio. Si se rompe, no lo toques con las manos y llévalo a acopio."),
    Residuo("Aceite de cocina usado", "🛢️", Categoria.MANEJO_ESPECIAL, "Nunca lo tires al fregadero. Guárdalo en una botella cerrada y llévalo a acopio."),
    Residuo("Medicinas caducadas", "💊", Categoria.MANEJO_ESPECIAL, "Llévalas a la farmacia o a un contenedor de medicamentos caducos.")
)

// Una carta del tablero. "id" la distingue de su pareja (que tiene el mismo residuo).
data class Carta(val id: Int, val residuo: Residuo)

// FR-703: 6 residuos al azar, cada uno dos veces, y todo revuelto
fun nuevoTablero(pares: Int = 6): List<Carta> =
    RESIDUOS.shuffled().take(pares)
        .flatMap { listOf(it, it) }
        .shuffled()
        .mapIndexed { i, r -> Carta(i, r) }

// FR-709: 3 estrellas con 9 intentos o menos, 2 con 10 a 14, 1 con 15 o más
fun estrellas(intentos: Int): Int = when {
    intentos <= 9 -> 3
    intentos <= 14 -> 2
    else -> 1
}
