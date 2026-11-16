# Implementation Plan: Validar el formato del correo

**Spec**: [spec.md](spec.md) · **Fecha**: 2026-11-02 · **Constitución**: v2.1.0

## Summary

Una función pura `esCorreoValido(correo)` con una expresión regular revisa el formato. El login y el registro la usan al tocar su botón y muestran un mensaje debajo del campo si el correo no sirve.

## Technical Context

| Parte | Decisión | Por qué |
|---|---|---|
| Validación | `Regex` de Kotlin en `data/ValidarCorreo.kt` | Pocas líneas, sin librerías (Principio IV) |
| Por qué no `android.util.Patterns` | Necesita Android para probarse; la función pura se prueba con JUnit en la computadora | Principio V: lógica con pruebas |
| Mensaje de error | `isError` + `supportingText` del `OutlinedTextField` (Material 3) | Ya viene en la app; no cambia colores ni diseño |
| Pruebas | `app/src/test/.../ValidarCorreoTest.kt` con JUnit 4 (ya está en el proyecto) | Se corren en Android Studio o con `gradlew test` |

## Constitution Check (v2.1.0)

- I Alcance: ✅ no es login real; solo formato.
- II Tres partes: ✅ solo cambia `app-android/`; la API y el firmware no se tocan.
- III Partir de lo que existe: ✅ con la excepción de v2.1.0. Login y registro se ven igual; solo agregan el mensaje de error. Perfil y gestión no cambian.
- IV Sencillo y en español: ✅ una función comentada en español; mensajes en español.
- V Con prueba: ✅ 4 pruebas JUnit; prueba manual en el celular (T308).

## Archivos

```text
app-android/app/src/main/java/com/example/truckonroute/
├── data/ValidarCorreo.kt          # nuevo: esCorreoValido() y el mensaje
├── MainActivity.kt                # LoginScreen: valida correo y contraseña al tocar Iniciar sesión
└── ui/RegistroScreen.kt           # valida el correo al tocar Registrarme; CampoTextoMejorado acepta valor, alCambiar y error (opcionales)
app-android/app/src/test/java/com/example/truckonroute/
└── ValidarCorreoTest.kt           # nuevo: 4 pruebas
```

`CampoTextoMejorado` sigue funcionando igual para Nombre, Teléfono y Contraseña, porque los parámetros nuevos son opcionales.
