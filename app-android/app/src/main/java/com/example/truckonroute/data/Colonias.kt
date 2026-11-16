package com.example.truckonroute.data

import java.text.Normalizer

// Colonias, barrios y fraccionamientos de la ciudad de Huauchinango, Puebla (zona urbana, C.P. 73170–73178).
// Fuente: listado de códigos postales de Huauchinango (micodigopostal.org, datos de SEPOMEX), consultado 2026-10-22.
// Antes el buscador tenía colonias de otros municipios (Villas de Pachuca, Tulancingo).
val COLONIAS_HUAUCHINANGO = listOf(
    "Centro", "Acatlán", "Santa Dora", "La Petrolera", "El Cerrito", "Agua Blanca",
    "Independencia", "Morelos", "Los Pinos", "Texcapa", "El Derrumbe", "El Ocote",
    "Santiago (Buena Vista)", "Cuacuila", "Los Capulines", "La Cumbre", "Chapultepec", "Guadalupe",
    "5 de Octubre", "Lomas de Chapultepec", "Los Ángeles", "Santiago", "La Partada", "La Joya",
    "Bellavista", "Río Chiquito", "5 de Mayo", "La Aurora", "Nuevo Milenio", "Piedras Pintadas",
    "La Herradura", "La Mesita", "El Mirador I", "Antonio Amador Quintero", "El Llano",
    "Lomas de Bellavista", "Hortalizas", "El Ocotal", "La Paz", "El Mirador II", "Monterrey",
    "Proyecto 2000", "Santa Cruz", "Santa Catarina", "14 de Octubre", "Ahuacatitla",
    "Adolfo López Mateos", "Cruz del Milagro", "Constitución", "Cruz Blanca", "Ruiz Cortines",
    "Francisco Villa", "La Morena", "El Ahuacatal", "El Potro", "Catalina", "Insurgentes",
    "Libertad", "El Paraíso", "Valles del Rincón", "Rincón de la Cruz", "La Estrella",
    "Las Flores", "Sección 39", "FOVISSSTE", "Emiliano Zapata", "Carmen Serdán",
    "Lomas de Zacamila", "Atzopic", "FSTSE", "INFONAVIT Zacamila", "Luis Donaldo Colosio",
    "Valle Fénix", "Electricista", "La Villa", "Rancho Viejo", "Miguel Hidalgo", "La Palpa",
    "Vima", "Lomas del Bosque", "Benito Juárez", "Quetzalcoatl", "Zacamila"
).sorted()

// Quita acentos y mayúsculas para comparar: "angeles" encuentra "Los Ángeles"
private fun sinAcentos(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

// Colonias que contienen lo que escribió el usuario (máximo 6 sugerencias)
fun buscarColonias(texto: String): List<String> {
    val buscado = sinAcentos(texto.trim())
    if (buscado.isEmpty()) return emptyList()
    return COLONIAS_HUAUCHINANGO.filter { sinAcentos(it).contains(buscado) }.take(6)
}
