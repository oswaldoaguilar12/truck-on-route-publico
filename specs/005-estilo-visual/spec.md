# Feature Specification: Estilo visual con profundidad (versión 2.9)

**Feature Branch**: `version-2.9` · **Created**: 2026-11-11 · **Status**: Implementada, falta compilar y probar en el emulador

**Input**: "Sin cambiar colores, ¿puedes hacerla visualmente más agradable? Me refiero a la app, que no se vea tan plana." (Oswaldo, 2026-11-11)

## Historia de usuario

**H21 (P1)**: Como usuario de la app (ciudadano, chofer o administrador), quiero que las pantallas se vean con profundidad y orden, para que la app se sienta más cuidada y sea más fácil distinguir cada sección.

**Prueba independiente**: abrir la app en el emulador y recorrer login, registro, inicio, perfil y pantalla del chofer; todo funciona igual que en la v2.8 y los colores son los mismos.

## Requisitos (EARS)

- **FR-501**: La app DEBE usar solo los colores que ya tenía (verde 0xFF019B43, verde oscuro 0xFF0A7D2C, fondos 0xFFF3EFE8 / 0xFFEFEFEF / 0xFFF5F7F6, blanco, grises, amarillo y rojo de avisos). Se permiten versiones transparentes de esos colores y el degradado entre los dos verdes, que ya existía en el encabezado.
- **FR-502**: Las tarjetas DEBEN tener esquinas de 20 dp y una sombra suave con tono verde.
- **FR-503**: Cada sección (¿Dónde está el camión?, Avisos, Mi recorrido, Mensajes) DEBE tener un título con su ícono.
- **FR-504**: El login y el registro DEBEN tener un fondo con círculos verdes muy transparentes y el logo en un círculo con degradado y sombra; el formulario del login va dentro de una tarjeta.
- **FR-505**: Los encabezados verdes DEBEN tener sombra, esquinas de 28 dp y burbujas blancas transparentes.
- **FR-506**: Ningún cambio DEBE alterar el comportamiento de la v2.8 (login, roles, avisos, cercanía, mapa).
- **FR-507** (agregado el 2026-11-24, aprobado por el equipo): El Perfil DEBE mostrar el nombre, correo, teléfono, colonia y rol de la cuenta que inició sesión, no datos fijos. Se encontró en la prueba T509: a un ciudadano le salía "Rol: Administrador". Editar el perfil sigue siendo solo en pantalla (no se guarda en la API).

## Fuera de alcance
- Cambiar la paleta de colores, tipografías nuevas, modo oscuro, animaciones nuevas.
- El panel web del administrador (solo la app Android).
- Guardar en la API los cambios hechos en "Editar perfil" (queda para v3.0).

## Criterios de éxito
- **SC-501**: El proyecto compila en Android Studio sin errores.
- **SC-502**: El equipo confirma en el emulador que la app "ya no se ve plana" y que todo sigue funcionando.
- **SC-503**: Al entrar como ciudadano o como administrador, el Perfil muestra los datos y el rol de esa cuenta.
