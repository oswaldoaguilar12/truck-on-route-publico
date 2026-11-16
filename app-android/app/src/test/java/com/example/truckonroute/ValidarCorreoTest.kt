package com.example.truckonroute

import com.example.truckonroute.data.esCorreoValido
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Spec 003: pruebas de la validación del correo (se corren con el botón Play junto a la clase,
// o en la terminal con: gradlew test)
class ValidarCorreoTest {

    @Test
    fun aceptaCorreosConFormatoCorrecto() {
        listOf(
            "nombre@gmail.com",
            "ana.lopez@itsh.edu.mx",
            "a+b@hotmail.com",
            "OSWALDO_97@Outlook.com",
            "x@mi-dominio.org",
            "  nombre@gmail.com  ", // los espacios de los extremos se ignoran
        ).forEach { assertTrue("Debería aceptar: $it", esCorreoValido(it)) }
    }

    @Test
    fun rechazaTextoSinArroba() {
        assertFalse(esCorreoValido("hola.com")) // el caso que encontró el equipo
    }

    @Test
    fun rechazaCorreosIncompletosOMalEscritos() {
        listOf(
            "", "   ", "@gmail.com", "ana@", "ana@gmail", "ana@gmail.c", "ana@.com",
            "ana@@gmail.com", "ana @gmail.com", "ana..x@gmail.com", "ana@gmail..com",
            ".ana@gmail.com", "ana.@gmail.com", "ana@-gmail.com", "ana@gmail.com.", "ana@gmail.c0m",
        ).forEach { assertFalse("Debería rechazar: '$it'", esCorreoValido(it)) }
    }

    @Test
    fun rechazaCorreosDemasiadoLargos() {
        assertFalse(esCorreoValido("a".repeat(250) + "@gmail.com"))
    }
}
