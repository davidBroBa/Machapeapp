# MEMORY.md — Machapeapp

## Estado Actual
- App Android: vínculo entre 2 dispositivos + juego MachapeJuego
- Contador atenciones válidas (0-30), "Machape atrasado" (cada 5 → -1, se reinicia)
- Botones: Necesito atención, Sí/No, Machape atrasado, Cambiar sala, Empezar, Pausa, flechas ←→
- Notificaciones push con servidor propio Node.js (funciona con app cerrada)
- **Juego verificado en emulador Medium_Phone_API_36**: mapache visible, objetos separados, flechas mueven, pausa, Game Over con reintentar
- APK: `C:\Users\brole\Desktop\machappv1.apk`

## Decisiones
| Decisión | Por qué |
|---|---|
| Todo el juego en dp, nunca px | px/dp mezclados sacaban el mapache de la pantalla (bug silencioso) |
| `BoxScope` como receptor en composables con `align` | `Modifier.align` solo existe dentro de BoxScope |
| Spawn cada 700ms, no por frame | A 60fps con 1.5% generaba objetos pegados |
| Servidor propio en `brolembservidor@100.87.72.91` | Evita el plan Blaze de Firebase |
| GameLogic.kt con funciones puras | Testeable sin emulador |
| Sonidos envueltos en clase `SonidosJuego` | Evita `SoundPool.release()` doble |

- **Servidor de notificaciones:** `TIPOS_REENVIO` declara los eventos de `/touch/{roomCode}`. Un `if` con un solo tipo (`need_attention`) descartaba en silencio `bored_attention`. Al añadir un evento nuevo hay que registrarlo **en el servidor**, no solo en el cliente.
- **Un solo origen de verdad para la posición:** dibujo y colisión deben leer la misma constante (`MAPACHE_CENTRO_Y`). Con dos cálculos paralelos la hitbox quedó 60dp por encima del mapache sin que nada lo delatara.
- **Medir la silueta, no estimar:** `PIL` + `getbbox()` sobre el canal alfa da los límites reales. Luego renderizar la imagen con el rectángulo encima para confirmarlo a la vista.

## Aprendizaje y Errores a Evitar
- **UNIDADES:** `screenHeightDp.dp.toPx()` da px; aplicar `.toDp()` encima divide mal. Usar `screenHeightDp.toFloat()` y `dp` directo.
- **`align`:** solo compila dentro de `BoxScope`/`RowScope`. En un composable suelto falla con "Unresolved reference".
- **TAP RÁPIDO = 0 MOVIMIENTO:** si el juego solo lee la dirección dentro del bucle, un toque que empieza y acaba en el mismo frame (16ms) no se ve. Solución: aplicar un `PASO_MINIMO` inmediato en `onPresionar`, y el bucle solo añade el arrastre sostenido.
- **VERIFICAR COORDENADAS CON uiautomator, NO A OJO:** la captura que se ve venir reescalada (900x1940) mientras la pantalla real es 1080x2400. Calcular a mano dio un tap 3px fuera del botón y dos "bugs" falsos. Usar `uiautomator dump` y leer `bounds` reales.
- **Gesto propio > click de Material:** `Button(onClick={})` + `detectTapGestures` compiten por el evento y el ripple aparecía sin mover nada. Un `Box` con `awaitEachGesture` + `awaitFirstDown` es predecible y permite pintar el estado de pulsación.
- **Recursos Android:** nombre solo minúsculas/números/underscore (`Sushi.png` rompe el build).
- **adb no está en PATH:** usar `C:\Users\brole\AppData\Local\Android\Sdk\platform-tools\adb.exe`.
- **`adb exec-out screencap > archivo`** corrompe el PNG en PowerShell: usar `shell screencap` + `adb pull`.
- **FCM:** token no se guarda si el roomCode entra después del 1er arranque → guardar también al conectar.
- **Samsung:** notificaciones en 2º plano requieren desactivar optimización de batería.

## Próximos Pasos
1. Probar en el Galaxy S25 Ultra físico
2. Verificar notificación "Machape aburrido" con 2 dispositivos reales
3. Sonidos de la lista del usuario: falta `lasana.png`, `helado.png`, `gorra.png`, `pesa.png`, `polilla.png` (el juego usa sushi/ok/corazón)
