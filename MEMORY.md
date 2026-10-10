# MEMORY.md — Machapeapp

## Estado Actual
- App Android: vínculo entre 2 dispositivos + juego MachapeJuego
- Contador atenciones válidas (0-30), "Machape atrasado" (cada 5 → -1, se reinicia)
- Botones: Necesito atención, Sí/No, Machape atrasado, Cambiar sala, Empezar, Pausa, flechas ←→
- Notificaciones push con servidor propio Node.js (funciona con app cerrada)
- **Juego verificado en emulador Medium_Phone_API_36**: mapache visible, objetos separados, flechas mueven, pausa, Game Over con reintentar, marcador visible
- **Las 8 imágenes caen** (verificado por id de recurso, no a ojo): sushi, lasana, helado (comida) · ok, gorra, pesa, polilla (basura) · corazon
- Dificultad por tramos con escalones en 20/30/40/50 y **tope en 50**; notificación "Machape aburrido" también en 50
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
| Un radio por imagen, no por tipo | El helado es 26x60dp y la pesa 64x34dp: un radio común hacía que una colisionara y la otra no |
| Dificultad por tramos, no rampa recta | El salto cada 10 puntos a partir de 20 se nota; una recta de 0 a 50 se percibía como "siempre igual" |
| Marcador en columna a la izquierda | En una fila con `SpaceBetween`, "Vidas" caía bajo el botón de pausa |

- **El servidor de notificaciones NO puede depender de `nohup &`:** al cerrar la sesión SSH el proceso muerde (se comprobó: `ps` vacío y log congelado en la 01:31 mientras el usuario jugaba a las 19:38). Va como **servicio systemd de usuario** en `~/.config/systemd/user/machape-notificaciones.service` con `Restart=always`. Estado: `systemctl --user status machape-notificaciones`.
- **`on('value')` reenvía el estado viejo al arrancar:** la primera emisión de `value` es el snapshot que ya había, no un evento nuevo. Sin descartarla, cada reinicio reenvía la última notificación a todos. Se ignora con un flag `primero`.
- **`Modifier.offset` coloca la esquina superior izquierda, no el centro:** los objetos se dibujaban con `offset(x = objeto.x)` mientras la colisiónMiraba `objeto.x` como centro. El sushi se dibujaba 40dp arriba-izquierda de donde colisionaba, de ahí "se come donde no hay nada" y "no come donde está el mapache". Todo lo que se guarde como centro y se dibuje con `offset` necesita restar media caja: helper `esquinaObjeto(centro, lado)`.
- **Servidor de notificaciones:** `TIPOS_REENVIO` declara los eventos de `/touch/{roomCode}`. Un `if` con un solo tipo (`need_attention`) descartaba en silencio `bored_attention`. Al añadir un evento nuevo hay que registrarlo **en el servidor**, no solo en el cliente.
- **Un solo origen de verdad para la posición:** dibujo y colisión deben leer la misma constante (`MAPACHE_CENTRO_Y`). Con dos cálculos paralelos la hitbox quedó 60dp por encima del mapache sin que nada lo delatara.
- **Medir la silueta, no estimar:** `PIL` + `getbbox()` sobre el canal alfa da los límites reales. Luego renderizar la imagen con el rectángulo encima para confirmarlo a la vista.
- **Varias iteraciones "a ojo" se acumulan en error:** la hitbox de la cabeza se afinó tres veces eligiendo el rectángulo sobre un render, y cada vez arrastró el error anterior (primino las orejas, luego se salió a la derecha). Lo que funcionó: renderizar con **rejilla de 10dp** para tener una escala legible, y medir por bandas de píxeles con `getbbox()` para sacar el centro real. La rejilla es lo que hizo visible el desfase.
- **Una banda de la silueta no es una zona anatómica:** medir "la cara" como el bbox de las filas de los ojos incluye las orejas, que se salen por los lados. Hay que decidir la zona por lo que el jugador apunta, no por lo que la silueta ocupa.
- **Verificar que un objeto existe en pantalla no basta mirarlo caer:** `pesa` es 1 de 4 basuras = 7,5% de los spawns. Tras varias capturas sin verla, la duda real no era "no sale" sino "no la he visto". Se resolvió con un `Log.d` temporal en el spawn y `aapt2 dump resources` para mapear id → nombre. El log se quitó después.
- **El marcador necesita `statusBarsPadding()`:** sin él, "Puntos" queda pegado al reloj. La Column ya no cabe contra el borde superior de la pantalla.
- **Capturar en ráfaga no cubre una partida larga:** 30 capturas seguidas pesaban lo mismo porque la partida había terminado y todas eran el Game Over. Hay que reiniciar antes de cada tanda.

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
2. Verificar notificación "Machape aburrido" a 50 puntos con 2 dispositivos reales (ya no hace falta jugar 50 en el emulador)
3. Versionar `server.js`: hoy solo vive en el servidor y si se pierde se pierden las notificaciones
4. Añadir sonido al caer cada tipo nuevo (lasana, helado, gorra, pesa, polilla)
