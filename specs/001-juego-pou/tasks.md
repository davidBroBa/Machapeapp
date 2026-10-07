# Tareas 001 — Juego Pou

- **Spec:** [`spec.md`](spec.md) · **Plan:** [`plan.md`](plan.md)
- **Criterio de tamaño:** máximo 20-30 minutos por tarea.

> Cada tarea es una unidad de trabajo que se puede revisar por separado. Si una
> tarea necesita más de 30 minutos, divídela. Si salen más de 10 tareas, la spec
> es demasiado grande: propón dividirla.

---

## Tareas

- [x] **T1. Crear funciones puras de lógica del juego.** · RF-1 a RF-10, RF-12
  - Archivo: `app/src/main/java/com/example/touchapp/GameLogic.kt`
  - **Hecho cuando:** Todas las funciones puras están implementadas y los tests unitarios pasan
  - Verificación: `./gradlew test --tests "com.example.touchapp.GameLogicTest" → BUILD SUCCESSFUL`

- [x] **T2. Crear tests unitarios para la lógica del juego.** · RF-1 a RF-10, RF-12
  - Archivo: `app/src/test/java/com/example/touchapp/GameLogicTest.kt`
  - **Hecho cuando:** Tests cubren colisiones, puntuación, vidas, velocidad y game over
  - Verificación: `./gradlew test --tests "com.example.touchapp.GameLogicTest" → BUILD SUCCESSFUL`

- [x] **T3. Crear Composable GameScreen con renderizado básico.** · RF-1, RF-14, RF-15
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** El mapa se muestra y se mueve al tocar la pantalla
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T4. Implementar game loop con objetos cayendo.** · RF-2, RF-7, RF-8
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Los objetos caen desde arriba con velocidad progresiva
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T5. Implementar colisiones y procesamiento de objetos.** · RF-2, RF-3, RF-4, RF-5
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Comida suma puntos, basura resta vidas, corazón recupera vida
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T6. Implementar sonidos del juego.** · RF-3, RF-4, RF-5, RF-6
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Sonidos se reproducen al comer, error, vida y game over
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T7. Implementar pantalla de Game Over.** · RF-6, RF-11, RF-12, RF-13
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Game Over muestra botones "Jugar otra vez" y "Salir"
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T8. Implementar notificación "Machape aburrido".** · RF-9, RF-10
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Se envía notificación al llegar a 50 puntos
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T9. Agregar pestaña de navegación.** · RF-13
  - Archivo: `app/src/main/java/com/example/touchapp/MainActivity.kt`
  - **Hecho cuando:** La app tiene pestañas "Conectar" y "Juego"
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T10. Verificación manual en dispositivo.** · RF-1 a RF-15
  - **Hecho cuando:** El juego funciona correctamente en dispositivo real
  - Verificación: Pasos manuales en Samsung Galaxy S25 Ultra

---

## Reglas de estas tareas

| Regla | Por qué |
|---|---|
| **Una tarea por vez.** No se empieza T2 hasta cerrar T1 | Dos tareas a la vez significa dos contextos a la vez, y ninguno bien |
| **Test primero.** Se escribe el test, se ve fallar, luego el código | Un test escrito después pasa siempre la primera vez |
| **"Hecho cuando" es verificable** | Si no hay comando ni comprobación, la tarea no está bien definida |
| **Los tests en rojo bloquean** | No se cierra una tarea con tests en rojo. Se reporta y se para |
| **Todo RF de la spec aparece en alguna tarea** | Si sobra un RF, sobra una tarea o falta un requisito |
| **Marcar la tarea es parte de la tarea** | Sin marcar, el estado del proyecto es fiction |

## Anti-patrones

- ❌ "T1. Implementar la funcionalidad"
- ❌ Tarea sin "Hecho cuando"
- ❌ Marcar la tarea sin ejecutar su verificación
- ❌ "Hecho cuando: funciona correctamente"
- ❌ Saltarse a T3 porque T2 "era rápida"

## Progreso

**Completadas: 0 de 10**
