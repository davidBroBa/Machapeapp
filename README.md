# Machapeapp

App Android de vínculo entre dos personas. Una pareja se conecta con un
código de sala, se pide atención con un toque y el otro teléfono vibra y
recibe una notificación. Además incluye **MachapeJuego**, un juego de
atrapar comida con el mapache.

<div align="center">

| Pestaña **Conectar** | Pestaña **Juego** |
|---|---|
| Pedir atención, validar, marcar atrasos | Atrapar sushi, esquivar basura, high score |

</div>

---

## Qué hace

### Pestaña Conectar

- **Código de vínculo**: ambas personas escriben el mismo código y quedan
  conectadas.
- **Necesito atención**: vibra, suena y muestra notificación en el otro
  teléfono, con un mapache animado llorando.
- **Sí fue válida / No**: el que pide confirma o rechaza el aviso.
- **Machape atrasado**: cada 5 pulsaciones baja 1 punto del contador de
  atenciones (de 30) y reinicia su propio contador.
- **Cambiar sala**: desconecta y permite entrar en otra.

### Pestaña Juego (MachapeJuego)

- Objetos caen y hay que atraparlos con el mapache.
- **Comida** (sushi) suma punto.
- **Basura** (el "OK") quita una de las 3 vidas.
- **Corazón** recupera una vida.
- La **hitbox es un rectángulo sobre la cara** del mapache, no la silueta
  entera: hay que acertar en la cabeza, no rozar el cuerpo.
- Al llegar a `PUNTOS_NOTIFICACION_ABURRIDO` puntos se avisa a la otra
  persona con **"Machape aburrido"** (hoy 10, subir a 50).
- Pantalla de inicio con **Empezar**, botón de **Pausa**, y al perder las
  3 vidas un diálogo que ofrece **reintentar** o **salir**.
- **Mejor marca** guardada en el dispositivo.

---

## Stack

| | |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01) + Material 3 |
| Android Gradle Plugin | 9.1.0 |
| minSdk / targetSdk / compileSdk | 24 / 35 / 35 |
| Firebase | Auth anónima, Realtime Database, Cloud Messaging (BOM 34.12.0) |
| Backend de notificaciones | Node.js 22 + `firebase-admin` 13.x en servidor propio |
| Tests | JUnit 4 (43 tests) |

`applicationId`: `com.example.touchapp` · proyecto Firebase: `touchapp-a6cf4`

---

## Estructura

```
TouchApp/
├── app/src/main/java/com/example/touchapp/
│   ├── MainActivity.kt            Pantalla principal, sala, lista de Firebase
│   ├── GameScreen.kt              Composable del juego y su dibujo
│   ├── GameLogic.kt               Lógica pura del juego (sin UI, testeable)
│   ├── MyFirebaseMessagingService.kt  Recibe push con la app cerrada
│   └── ui/theme/                  Tema Material 3
├── app/src/test/                  GameLogicTest: 43 tests
├── app/src/main/res/
│   ├── drawable/                  Mapache y objetos del juego
│   └── raw/                       Sonidos (comer, error, vida, gameover)
├── specs/                         Specs de cada funcionalidad
├── MEMORY.md                      Memoria del proyecto entre sesiones
├── AGENTS.md                      Reglas para agentes de IA
└── functions/                     Ver "Pendiente" más abajo: no se usa
```

La lógica del juego vive **separada de la UI** en `GameLogic.kt`: son
funciones puras sin Compose ni Firebase, todas cubiertas por tests.

---

## Comandos

### Build y tests

```powershell
.\gradlew assembleDebug              # compila el APK
.\gradlew testDebugUnitTest          # 43 tests de la lógica del juego
.\gradlew installDebug               # instala en el dispositivo conectado
```

El APK sale en `app/build/outputs/apk/debug/app-debug.apk`.

### Instalación manual por adb

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

`adb` no está en el PATH por defecto; está en
`C:\Users\brole\AppData\Local\Android\Sdk\platform-tools\adb.exe`.

### Emulador

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" `
    -avd Medium_Phone_API_36 -no-snapshot-load
adb devices
```

---

## Notificaciones push

Las notificaciones **no usan Cloud Functions**: el plan Spark de Firebase
no permite desplegarlas. Hay un servidor Node.js propio que escucha la
Realtime Database y envía el push.

### Qué necesita

- Un servidor con Node.js 22 y acceso a la Realtime Database.
- La **service account key** de Firebase, en `serviceAccountKey.json` junto
  al `server.js`. Está en `.gitignore`: no se sube al repo.

### Estructura en el servidor

```
~/machape-server/
├── server.js                # escucha /touch/{roomCode} y envía el push
├── serviceAccountKey.json   # credencial, NUNCA en el repositorio
└── server.log
```

### Instalación como servicio

Un `nohup &` **no sobrevive** al cierre de la sesión SSH: el proceso muere
y las notificaciones dejan de llegar sin ningún error visible. Va como
servicio systemd de usuario:

```bash
# ~/.config/systemd/user/machape-notificaciones.service
[Unit]
Description=Machapeapp - servidor de notificaciones push
After=network-online.target

[Service]
WorkingDirectory=/home/<usuario>/machape-server
ExecStart=/usr/bin/node /home/<usuario>/machape-server/server.js
Restart=always
RestartSec=5
StandardOutput=append:/home/<usuario>/machape-server/server.log
StandardError=append:/home/<usuario>/machape-server/server.log

[Install]
WantedBy=default.target
```

```bash
systemctl --user daemon-reload
systemctl --user enable --now machape-notificaciones
systemctl --user status machape-notificaciones
tail -f ~/machape-server/server.log
```

### Eventos que reenvía

El cliente escribe en `/touch/{roomCode}` y el servidor reenvía al resto
de tokens de la sala, **excluyendo al remitente**:

| `text` | Quién lo escribe | Notificación |
|---|---|---|
| `need_attention` | Pestaña Conectar | "Tu machape te necesita" |
| `bored_attention` | Juego, al llegar al umbral | "Machape aburrido" |

**Al añadir un evento nuevo hay que registrarlo también en el servidor**
(`TIPOS_REENVIO`). Si no, se descarta en silencio y la notificación nunca
llega.

### Samsung y notificaciones en segundo plano

Los Galaxy cierran la app para ahorrar batería y dejan de llegar las
notificaciones. Hay que desactivarlo:

> Ajustes → Apps → Machapeapp → Batería → **Sin restricciones**

---

## Estructura de datos

```
/tokens/{sala}/{userId}   token FCM del dispositivo
/counter/{sala}            atenciones válidas, 0..30
/tardado/{sala}            contador de "machape atrasado", 0..4
/state/{sala}              "idle" | "crying" | "happy"
/touch/{sala}              { sender, text } — evento actual
```

---

## Cómo se ajustó la hitbox

Un recordatorio de cómo se midió, porque es lo que más costó acertar:

1. La imagen `me.png` (1024×1536) se dibuja en una caja de 120×120dp con
   `ContentScale.Fit`, así que el contenido real ocupa **80×120dp** y queda
   20dp de margen a cada lado.
2. Con Pillow se mide la silueta (canal alfa, umbral ≥ 24) y se escala a dp.
3. **Un detalle que costó tres correcciones**: `Modifier.offset` coloca la
   esquina superior izquierda, no el centro. Todo lo que se guarde como
   centro y se dibuje con `offset` necesita restar media caja. Sin eso los
   objetos se dibujaban 40dp a la izquierda y 40dp arriba de donde
   colisionaban.

Para comprobar la hitbox sin calcular nada, `GameScreen.kt` tiene:

```kotlin
private const val DEBUG_VER_HITBOX = false
```

Ponerlo en `true` y sale el rectángulo de la cabeza dibujado sobre el
mapache.

---

## Pendiente

Cosas que **no están terminadas**, para no darlas por buenas:

- [ ] **`server.js` no está versionado.** Solo vive en el servidor. Si se
      pierde, el backend de notificaciones se pierde con él. Debería ir en
      el repo con la service account fuera.
- [ ] **`functions/` está muerto.** Tiene el código de Cloud Functions pero
      no se despliega (requiere plan Blaze). Se puede borrar o dejar como
      referencia.
- [ ] **No hay SDD ni modelo de amenazas** (`docs/` no existe).
- [ ] **Umbral de notificación en 10**, pendiente de subir a 50 cuando se
      confirme que el push llega.
- [ ] **Imágenes sin usar**: `lasana`, `helado`, `gorra`, `pesa` y
      `polilla` están en `drawable` pero el juego solo usa `sushi`, `ok` y
      `corazon`.
- [ ] Sin tests instrumentados: la UI solo se verifica a mano en emulador.
- [ ] Sin ProGuard en release (`isMinifyEnabled = false`).

---

## Documentación

| Archivo | Contenido |
|---|---|
| `MEMORY.md` | Estado, decisiones con su porqué, errores a evitar, próximos pasos |
| `AGENTS.md` | Reglas del proyecto para agentes de IA |
| `specs/001-juego-pou/` | Spec, plan y tareas del juego (aprobada) |
| `specs/002-fixes-juego/` | Spec, plan y tareas de las correcciones (aprobada) |

No hay `docs/` todavía: no hay SDD ni modelo de amenazas escritos.