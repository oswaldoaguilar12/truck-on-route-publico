# Tasks: Rutas dibujadas (versión 2.13)

**Input**: `specs/009-rutas-dibujadas/`

- [X] T901 Spec 009, enmienda 4.3.0 y maqueta (2026-11-27)
- [X] T902 Maqueta aprobada; FR-912 agregado; plan con su revisión (2026-11-27)
- [X] T903 API: trazo, avance, desvío y aviso con pruebas (FR-903 a FR-905, FR-908, FR-910, FR-912; SC-901) — 2026-11-27, `npm test` 32/32
- [X] T904 Panel: dibujar el camino de una ruta (FR-901, FR-902) — 2026-11-27, probado con Playwright
- [X] T905 Panel: dibujar el desvío de un recorrido en curso y mapa con gris (FR-906, FR-908) — 2026-11-27, probado con Playwright
- [X] T906 App: leer trazo, avance y desvío; dibujar gris, verde y ámbar en el mapa (FR-906, FR-907, FR-909) — programada 2026-11-27, falta compilar (T908)
- [X] T907 App: aviso del chofer y notificación de desvío al ciudadano (FR-909, FR-912) — programada 2026-11-27, falta compilar (T908)
- [X] T908 Compilar en Android Studio sin errores (SC-903) — compiló el 2026-11-27 (equipo)
- [ ] T909 En el emulador: dibujar la ruta, simular, ver el gris y poner un desvío con las tres cuentas (SC-902, SC-903) — equipo
- [X] T910 Simulación: `simular.js` sigue el camino dibujado y el desvío del recorrido en curso de CAMION-01; sin camino usa las coordenadas de ejemplo (petición del equipo al ver la demo, 2026-11-27; solo herramienta de prueba, no cambia la API ni la app)
- [ ] T911 App: sin recorrido en curso, el rastro verde solo muestra la última vuelta del camión (corta en huecos de más de 2 min o saltos de más de 300 m) para que no se encimen las líneas (petición del equipo, 2026-11-27) — programada, falta compilar y verlo en el emulador
