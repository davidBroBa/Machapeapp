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
- **Comida** — sushi, lasaña, helado: suman punto.
- **Basura** — el "OK", gorra, pesa, polilla: quitan una de las 3 vidas.
- **Corazón** — recupera una vida.
- La **hitbox es un rectángulo sobre la cara** del mapache, no la silueta
  entera: hay que acertar en la cabeza, no rozar el cuerpo.
- Cada objeto lleva **su propia hitbox**, porque las siluetas son muy
  distintas: el helado es 26×60 dp (alto y estrecho) y la pesa 64×34 dp.
- La dificultad **sube por escalones** en 20, 30, 40 y 50 puntos, y ahí se
  queda: es el tope, para que el juego no llegue a ser imposible.
- Al llegar a 50 puntos se avisa a la otra persona con **"Machape aburrido"**.
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
| Tests | JUnit 4 (47 tests) |

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
├── app/src/test/                  GameLogicTest: 47 tests
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
.\gradlew testDebugUnitTest          # 47 tests de la lógica del juego
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

## Dificultad

Sube por tramos, no con una rampa recta. En `GameLogic.kt`:

```kotlin
private val ESCALONES_DIFICULTAD = floatArrayOf(0f, 20f, 30f, 40f, 50f)
private val VELOCIDADES_TRAMO   = floatArrayOf(2.2f, 4.4f, 5.4f, 6.4f, 7.4f)
```

| Puntos | Velocidad (dp/frame) |
|---|---|
| 0 | 2.2 |
| 20 | 4.4 |
| 30 | 5.4 |
| 40 | 6.4 |
| 50 | 7.4 ← tope, no sube más |

`velocidadCaida()` interpola dentro de cada tramo y a partir de 50 devuelve
siempre 7.4. El tope es también el umbral de la notificación "Machape
aburrido": cuando ya no queda dificultad que ganar, se avisa.

---

## Objetos que caen

Cada objeto lleva **su propia hitbox**, medida sobre los píxeles opacos de
su PNG (alfa ≥ 24) y escalada a la caja de 80 dp. No se puede compartir un
radio por tipo:

| Imagen | Tipo | Silueta | Semiejes |
|---|---|---|---|
| `sushi` | comida | 48.1 × 43.4 dp | 24.1 × 21.7 |
| `lasana` | comida | 47.7 × 43.4 dp | 23.8 × 21.7 |
| `helado` | comida | 26.2 × 60.5 dp | 13.1 × 30.3 |
| `ok` | basura | 56.4 × 49.8 dp | 28.2 × 24.9 |
| `gorra` | basura | 60.5 × 41.7 dp | 30.3 × 20.9 |
| `pesa` | basura | 64.1 × 34.1 dp | 32.0 × 17.0 |
| `polilla` | basura | 64.1 × 47.8 dp | 32.0 × 23.9 |
| `corazon` | corazón | 53.4 × 43.7 dp | 26.7 × 21.8 |

El helado es alto y estrecho y la pesa ancha y baja: con un radio común,
una colisionaría donde la otra no.

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
- [ ] **Notificación "Machape aburrido" sin verificar en físico.** El umbral
      ya está en 50 y la parte cliente está probada, pero falta confirmar
      que el push llega con dos Galaxy reales.
- [ ] **Sonido solo para sushi/ok/corazón.** Las cinco imágenes nuevas
      reutilizan esos sonidos hasta que se graben los suyos.
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