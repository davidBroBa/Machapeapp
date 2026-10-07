# AGENTS.md

Este archivo proporciona instrucciones para agentes de IA que trabajen en el proyecto TouchApp (Machapeapp).

## Descripción del Proyecto

**Machapeapp** es una aplicación Android de vínculo emocional entre parejas. Dos personas se conectan mediante un código de sala y se envían señales de "necesito atención" en tiempo real con notificaciones push, vibración y animaciones de un mapache.

## Comandos

### Android (Gradle)
```bash
# Compilar debug
./gradlew assembleDebug

# Compilar release
./gradlew assembleRelease

# Instalar en dispositivo
./gradlew installDebug

# Limpiar
./gradlew clean
```

### Firebase Cloud Functions
```bash
cd functions

# Instalar dependencias
npm install

# Desplegar funciones
npm run deploy

# Ver logs
npm run logs

# Emular localmente
npm run serve
```

## Estructura del Proyecto

```
TouchApp/
├── app/                          # Módulo Android principal
│   ├── src/main/java/com/example/touchapp/
│   │   ├── MainActivity.kt       # Toda la UI y lógica (Compose)
│   │   ├── MyFirebaseMessagingService.kt  # Servicio FCM
│   │   └── ui/theme/             # Tema Material 3
│   ├── src/main/res/             # Recursos (drawables, strings, etc.)
│   └── build.gradle.kts          # Dependencias Android
├── functions/                    # Cloud Functions (Node.js)
│   ├── index.js                  # Función sendAttentionNotification
│   └── package.json
├── firebase.json                 # Configuración Firebase
├── build.gradle.kts              # Configuración Gradle raíz
└── settings.gradle.kts           # Módulos del proyecto
```

## Arquitectura

- **UI:** Jetpack Compose con Material 3
- **Estado:** `remember` + `mutableStateOf` directamente en el Composable principal (sin ViewModels)
- **Backend:** Firebase (Auth anónima + Realtime Database + Cloud Messaging)
- **Notificaciones:** Cloud Functions con trigger `onValueWritten` en `/touch/{roomCode}`

## Estructura de Datos Firebase

```
/tokens/{roomCode}/{userId} → FCM token (string)
/counter/{roomCode}         → Atenciones válidas 0-30 (integer)
/tardado/{roomCode}         → Contador machape atrasado (integer)
/state/{roomCode}           → "idle" | "crying" | "happy" (string)
/touch/{roomCode}           → { sender: string, text: string }
```

### Eventos de `/touch/{roomCode}`
- `need_attention` — Alguien pide atención
- `validated_attention` — Atención validada (sube contador)
- `invalid_attention` — Atención invalidada

## Convenciones

- **Idioma del código:** Español (comentarios y strings)
- **Estilo:** Composables con modificadores encadenados, colores como `Color(0xFF...)`
- **Imágenes:** Mapache en diferentes estados (`me`, `feliz`, `mt1`, `mt2`)
- **SharedPreferences:** Nombre "machapeapp", claves: `roomCode`, `myId`

## Reglas Importantes

1. **No agregar dependencias** sin consultar — el proyecto usa versiones específicas de Firebase BOM
2. **Mantener un solo archivo** para la UI (MainActivity.kt) — es intencional
3. **Las Cloud Functions deben desplegarse** para que las notificaciones funcionen
4. **El token FCM** se guarda en `tokens/$roomCode/$myId` — no cambiar la ruta
5. **El contador** usa `runTransaction` para operaciones atómicas

## Problemas Conocidos

- Las notificaciones requieren que las Cloud Functions estén desplegadas
- El permiso `POST_NOTIFICATIONS` debe concederse en runtime (Android 13+)
- La app usa autenticación anónima — no hay login con contraseña
