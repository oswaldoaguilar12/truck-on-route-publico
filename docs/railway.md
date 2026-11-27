# Publicar Truck on Route en Railway (versión 2.15, spec 011)

Al terminar vas a tener la API y el panel en una dirección pública con HTTPS, por ejemplo
`https://truck-on-route-production.up.railway.app/panel`, y el MySQL de Railway guardando las ubicaciones.

> Railway cobra por uso. Al crear la cuenta te da un crédito de prueba; revisa en **Usage** cuánto lleva gastado.
> Con un solo servicio y un MySQL pequeño el gasto es bajo, pero apaga el proyecto si no lo vas a usar.

## 0. Antes de empezar

- Una cuenta en <https://railway.com> entrando con tu GitHub.
- El repositorio que se va a publicar: `oswaldoaguilar12/truck-on-route-publico`, rama `main`.
- Inventa y anota en un lugar seguro (no en el repositorio):
  - `ADMIN_CONTRASENA`: la contraseña del administrador, de **8 caracteres o más** y que no sea `Admin2026`.
  - `CLAVE_GPS`: una clave larga para el GPS, por ejemplo 20 letras y números al azar.
  - `CONTRASENA_BD`: la contraseña del usuario `truck_app` de MySQL.

## 1. Crear el proyecto y la API

1. En Railway: **New Project → Deploy from GitHub repo** y elige `truck-on-route-publico`.
   Si no aparece, da permiso a Railway en GitHub (**Configure GitHub App**).
2. Abre el servicio que se creó → **Settings**:
   - **Root Directory**: `api`
   - **Start Command**: `npm start` (normalmente lo detecta solo).
3. Todavía no importa si el primer despliegue falla: faltan las variables.

## 2. Agregar MySQL

1. En el proyecto: **+ Create → Database → MySQL**. Railway crea el servicio `MySQL`.
2. Crear el usuario `truck_app` (la constitución pide no usar `root`):
   - Abre el servicio `MySQL` → **Variables** y copia `MYSQL_PUBLIC_URL`.
   - En tu computadora (ya tienen MySQL instalado), en una terminal:
     ```
     mysql --host=<host> --port=<puerto> -u root -p railway
     ```
     (host, puerto y contraseña salen de `MYSQL_PUBLIC_URL`: `mysql://root:CONTRASEÑA@HOST:PUERTO/railway`).
   - Ya dentro, pega esto cambiando `CONTRASENA_BD`:
     ```sql
     CREATE USER 'truck_app'@'%' IDENTIFIED BY 'CONTRASENA_BD';
     GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, REFERENCES ON railway.* TO 'truck_app'@'%';
     ```
   Las tablas no hace falta crearlas: la API las crea sola la primera vez.

## 3. Volume para los datos

Usuarios, rutas, recorridos, chat, fotos y audios viven en una carpeta. Sin Volume se borran en cada despliegue.

1. Clic derecho en el servicio de la API → **Attach Volume** (o **+ Create → Volume** y elegir la API).
2. **Mount path**: `/data`

## 4. Variables de la API

En el servicio de la API → **Variables → Raw Editor**, pega y cambia lo que va en mayúsculas:

```
PRODUCCION=1
ADMIN_CORREO=admin@truckonroute.mx
ADMIN_CONTRASENA=TU_CONTRASENA_DE_ADMIN
CLAVE_GPS=TU_CLAVE_GPS
DATOS_DIR=/data
DB_URL=mysql://truck_app:CONTRASENA_BD@${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/railway
```

`${{MySQL.…}}` se escribe tal cual: Railway lo cambia por la dirección interna del MySQL.
No pongas `PORT`: Railway lo da solo.

Guarda y Railway vuelve a desplegar. En **Deployments → View logs** debes ver:

```
Conectada a MySQL: base railway
Primera vez (producción): se creó solo la cuenta del administrador y CAMION-01.
API de Truck on Rute escuchando en el puerto ...
```

Si sale `PRODUCCION=1 pero ADMIN_CONTRASENA falta...`, revisa esa variable.

## 5. Dirección pública

Servicio de la API → **Settings → Networking → Generate Domain**. Copia la dirección, por ejemplo
`https://truck-on-route-production.up.railway.app`.

- Panel: `https://…up.railway.app/panel` → entra con `ADMIN_CORREO` y `ADMIN_CONTRASENA`.
- En el servidor no existen las cuentas de ejemplo: crea el chofer desde el panel (pestaña Choferes)
  y la ciudadana registrándote desde la app.

## 6. La app apuntando a Railway

1. En `app-android/local.properties` cambia:
   ```
   API_URL=https://truck-on-route-production.up.railway.app
   ```
2. **Build → Rebuild Project** y vuelve a instalar. Ya no hace falta `adb reverse`, y funciona también en un celular con datos.

## 7. Simulación contra Railway

En tu computadora, en `api/.env`:

```
API_URL=https://truck-on-route-production.up.railway.app
CLAVE_GPS=TU_CLAVE_GPS
```

y `npm run simular`. Si ves `→ 200`, el camión ya se mueve en el panel y en la app.
Si ves `→ 401`, la `CLAVE_GPS` no es la misma que en Railway.

## 8. Comprobar que todo quedó (T1107)

- [ ] El panel abre por HTTPS y entra el administrador.
- [ ] `admin@truckonroute.mx` con `Admin2026` **no** entra.
- [ ] Una ubicación sin clave responde 401 (por ejemplo, `npm run simular` sin `CLAVE_GPS`).
- [ ] La app compilada con la dirección de Railway muestra el camión simulado.
- [ ] Después de **Redeploy**, el chofer y las rutas que creaste siguen ahí (el Volume funciona).

## Pendiente: el ESP32 (T1108)

Se hace al final, con el equipo del GPS. Hay que cambiar en `secrets.h` la dirección de la API por la de Railway
y mandar la cabecera `X-Clave-GPS`. Railway solo atiende por HTTPS: el ESP32 por WiFi sí puede, pero el SIM800L batalla
con HTTPS, así que eso se revisa con calma.
