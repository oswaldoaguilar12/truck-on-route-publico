package com.example.truckonroute.data

// Truck on Rute 2.7 (spec 003): revisa que un correo tenga el formato correcto.
// Solo revisa la forma del texto (nombre@dominio.extension); no comprueba que la cuenta exista.
// Es una función pura (sin Android), así se puede probar con JUnit en la computadora.

// Antes de la @: letras, números y . _ % + -  (sin empezar, terminar ni repetir el punto)
// Después de la @: partes separadas por punto (sin empezar ni terminar con -) y una extensión de 2 o más letras (.com, .mx)
private val PATRON_CORREO = Regex(
    "^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*" +
        "@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*" +
        "\\.[A-Za-z]{2,}$"
)

// Un correo no puede tener más de 254 caracteres
private const val LARGO_MAXIMO = 254

const val MENSAJE_CORREO_INVALIDO = "Escribe un correo válido, por ejemplo nombre@gmail.com"

// true si el correo tiene formato válido. Ignora los espacios al inicio y al final.
fun esCorreoValido(correo: String): Boolean {
    val limpio = correo.trim()
    return limpio.length <= LARGO_MAXIMO && PATRON_CORREO.matches(limpio)
}
