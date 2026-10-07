# Plan 001 — Juego Pou

- **Spec:** [`spec.md`](spec.md) — **Estado:** aprobada
- **Constitución:** [`../../constitution.md`](../../constitution.md)
- **Fecha:** 2026-10-06

> El plan responde al **CÓMO**. El QUÉ y el POR QUÉ viven en `spec.md`.
> Si aquí aparece un requisito nuevo, el error está en `spec.md`: corrígilo allí.

---

## 1. Qué RF cubre este plan

| RF | Dónde se cubre en este plan |
|---|---|
| RF-1 | §3 `MoverMapache`, §5 Controles táctiles |
| RF-2 | §3 `ColisionObjeto`, §4 Algoritmo de colisión |
| RF-3 | §3 `ProcesarComida`, §4 Algoritmo de puntuación |
| RF-4 | §3 `ProcesarBasura`, §4 Algoritmo de vidas |
| RF-5 | §3 `ProcesarCorazon`, §4 Algoritmo de vida extra |
| RF-6 | §3 `VerificarGameOver`, §5 Game Over |
| RF-7 | §3 `CalcularVelocidad`, §4 Dificultad progresiva |
| RF-8 | §3 `CalcularVelocidad`, §4 Dificultad progresiva |
| RF-9 | §3 `EnviarNotificacionAburrido`, §4 Notificación |
| RF-10 | §3 `EnviarNotificacionAburrido`, §4 Notificación |
| RF-11 | §5 Game Over |
| RF-12 | §3 `ReiniciarPartida`, §5 Game Over |
| RF-13 | §3 `SalirJuego`, §5 Navegación |
| RF-14 | §5 Interfaz |
| RF-15 | §5 Interfaz |

---

## 2. Archivos creados o modificados

| Archivo | Acción | Responsabilidad (una frase) | RF |
|---|---|---|---|
| `MainActivity.kt` | modificar | Agregar pestaña de navegación y pantalla de juego | RF-1 a RF-15 |
| `GameScreen.kt` | crear | Composable del juego con lógica de renderizado | RF-1 a RF-15 |
| `GameLogic.kt` | crear | Funciones puras de lógica del juego | RF-1 a RF-10, RF-12 |
| `app/src/main/res/raw/` | crear | Sonidos del juego (comer, error, vida, gameover) | RF-3, RF-4, RF-5, RF-6 |

---

## 3. Lógica de negocio (funciones puras)

Sin DOM, sin base de datos, sin red. Reciben sus datos, devuelven su resultado.

| Función | Archivo | Entrada | Salida | RF |
|---|---|---|---|---|
| `moverMapache` | `GameLogic.kt` | `posicionActual: Float, posicionTocada: Float, anchoPantalla: Float` | `nuevaPosicion: Float` | RF-1 |
| `colisionObjeto` | `GameLogic.kt` | `mapacheX: Float, mapacheY: Float, objetoX: Float, objetoY: Float, radioColision: Float` | `Boolean` | RF-2 |
| `procesarComida` | `GameLogic.kt` | `puntuacionActual: Int` | `nuevaPuntuacion: Int` | RF-3 |
| `procesarBasura` | `GameLogic.kt` | `vidasActuales: Int` | `nuevasVidas: Int` | RF-4 |
| `procesarCorazon` | `GameLogic.kt` | `vidasActuales: Int` | `nuevasVidas: Int` | RF-5 |
| `verificarGameOver` | `GameLogic.kt` | `vidas: Int` | `Boolean` | RF-6 |
| `calcularVelocidad` | `GameLogic.kt` | `puntuacion: Int` | `velocidad: Float` | RF-7, RF-8 |
| `verificarNotificacionAburrido` | `GameLogic.kt` | `puntuacion: Int, notificacionEnviada: Boolean` | `Boolean` | RF-9, RF-10 |
| `reiniciarPartida` | `GameLogic.kt` | — | `Partida(0, 3, false)` | RF-12 |

---

## 4. Algoritmo

```
INICIO
  -> inicializar partida (puntuacion=0, vidas=3, notificacionEnviada=false)
  -> MIENTRAS partida activa:
       -> calcular velocidad segun puntuacion (maximo en 20 puntos)
       -> generar objetos aleatorios (comida, basura, corazon)
       -> mover objetos hacia abajo
       -> SI colision con mapache:
           -> SI comida: puntuacion++, sonido comer
           -> SI basura: vidas--, sonido error
           -> SI corazon: vidas++ (max 3), sonido vida
           -> SI puntuacion == 50 Y notificacionEnviada == false:
               -> enviar notificacion "Machape aburrido"
               -> notificacionEnviada = true
       -> SI vidas == 0:
           -> sonido gameover
           -> mostrar pantalla game over
           -> SI "Jugar otra vez": reiniciar partida
           -> SI "Salir": volver a pantalla principal
  -> FIN
```

Casos límite tratados aquí:
- Mapache no se mueve fuera de la pantalla
- Objetos que caen fuera de la pantalla desaparecen sin penalización
- Puntuación nunca es negativa
- Vidas nunca son negativas ni superan 3
- Notificación solo se envía una vez por partida

---

## 5. Interfaz

| Elemento | Comportamiento esperado | RF |
|---|---|---|
| Pestaña "Juego" | Navega a la pantalla de juego | RF-13 |
| Mapache | Se mueve horizontalmente al tocar la pantalla | RF-1 |
| Objetos cayendo | Caen desde arriba con velocidad progresiva | RF-7, RF-8 |
| Puntuación | Muestra "Puntos: X" en la parte superior | RF-14 |
| Vidas | Muestra 3 corazones en la parte superior | RF-15 |
| Game Over | Muestra "Game Over" con botones "Jugar otra vez" y "Salir" | RF-6, RF-11, RF-12, RF-13 |

Accesibilidad: Contraste de colores WCAG 2.2 AA, objetivos táctiles mínimo 48dp
Responsive: Verificado a 375px de ancho mínimo

---

## 6. Decisiones técnicas

### 6.1 Arquitectura del juego

- **Elegido:** Todo en un solo Composable (`GameScreen.kt`) con lógica pura separada en `GameLogic.kt`
- **Alternativa descartada:** ViewModels + Clean Architecture
- **Por qué:** Proyecto simple, el juego es una funcionalidad aislada
- **Coste que aceptamos:** Lógica de juego mezclada con UI en el Composable

### 6.2 Motor de juego

- **Elegido:** `LaunchedEffect` con `while` loop y `delay` para el game loop
- **Alternativa descartada:** `SurfaceView` o `Canvas` con hilo separado
- **Por qué:** Jetpack Compose ya está integrado, no requiere dependencias adicionales
- **Coste que aceptamos:** El juego corre en el hilo de UI, puede haber jank en dispositivos lentos

### 6.3 Colisiones

- **Elegido:** Colisión circular simple (distancia entre centros < radio)
- **Alternativa descartada:** Colisión por rectángulos (AABB)
- **Por qué:** Los objetos son imágenes circulares, la colisión circular es más natural
- **Coste que aceptamos:** Menos precisa para objetos no circulares

### 6.4 Sonidos

- **Elegido:** `SoundPool` para efectos cortos
- **Alternativa descartada:** `MediaPlayer` o `ToneGenerator`
- **Por qué:** SoundPool está diseñado para efectos de juego cortos y rápidos
- **Coste que acceptamos:** SoundPool tiene límite de sonidos simultáneos (no es problema aquí)

### 6.5 Notificación de pareja

- **Elegido:** Enviar notificación push vía Firebase Realtime Database (escribir en `touch/$roomCode`)
- **Alternativa descartada:** Llamar directamente al servidor desde el dispositivo
- **Por qué:** El servidor ya escucha `/touch` y envía notificaciones, no requiere código adicional
- **Coste que acceptamos:** Depende de que el servidor esté corriendo

---

## 7. Estrategia de pruebas

| Nivel | Qué cubre | Comando | RF |
|---|---|---|---|
| Unitario | Lógica pura: colisiones, puntuación, vidas, velocidad | `./gradlew test` | RF-1 a RF-10, RF-12 |
| Integración | Firebase: notificación de aburrido | `./gradlew connectedAndroidTest` | RF-9, RF-10 |
| Manual | Game Over, controles táctiles, dificultad progresiva | Pasos manuales en dispositivo | RF-1, RF-6, RF-7, RF-8, RF-11, RF-12, RF-13 |

**Test-first:** el test se escribe antes que la implementación y se ve fallar.
Si no lo viste fallar, no sabes que lo prueba.

---

## 8. Cumplimiento de la constitución

| Principio | Cómo lo respeta este plan |
|---|---|
| Tipado estricto | Funciones puras con tipos explícitos, sin `any` |
| Test-first | Tests unitarios antes de la implementación |
| Cero secretos | No hay secretos en el código |
| Errores explícitos | Manejo de errores en colisiones y Firebase |
| Sin lógica de negocio en UI | `GameLogic.kt` separa la lógica pura |

---

## 9. Riesgos del plan

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Jank en dispositivos lentos | Mala experiencia de juego | Optimizar el game loop, reducir objetos simultáneos |
| Notificación no llega | Usuario no recibe alerta | Verificar que el servidor esté corriendo, reintentar |
| Imágenes no disponibles | Juego sin objetos visuales | Usar emojis como placeholder temporal |
| Batería consume mucho | Cierre de la app por el sistema | Optimizar el game loop, pausar cuando no está en primer plano |
