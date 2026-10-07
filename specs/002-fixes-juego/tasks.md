# Tareas 002 — Fixes del Juego Pou

- **Spec:** [`spec.md`](spec.md) · **Plan:** [`plan.md`](plan.md)
- **Criterio de tamaño:** máximo 20-30 minutos por tarea.

---

## Tareas

- [x] **T1. Agregar enum EstadoJuego y funciones de movimiento con flechas.** · RF-15, RF-16
  - Archivo: `app/src/main/java/com/example/touchapp/GameLogic.kt`
  - **Hecho cuando:** Tests unitarios pasan
  - Verificación: `./gradlew testDebugUnitTest --tests "com.example.touchapp.GameLogicTest" → BUILD SUCCESSFUL`

- [x] **T2. Agregar tests unitarios para movimiento con flechas.** · RF-15, RF-16
  - Archivo: `app/src/test/java/com/example/touchapp/GameLogicTest.kt`
  - **Hecho cuando:** Tests cubren moverIzquierda y moverDerecha
  - Verificación: `./gradlew testDebugUnitTest --tests "com.example.touchapp.GameLogicTest" → BUILD SUCCESSFUL`

- [x] **T3. Agregar funciones de generación ordenada y separación.** · RF-4, RF-5, RF-6
  - Archivo: `app/src/main/java/com/example/touchapp/GameLogic.kt`
  - **Hecho cuando:** Tests unitarios pasan
  - Verificación: `./gradlew testDebugUnitTest --tests "com.example.touchapp.GameLogicTest" → BUILD SUCCESSFUL`

- [x] **T4. Agregar tests unitarios para generación ordenada.** · RF-4, RF-5, RF-6
  - Archivo: `app/src/test/java/com/example/touchapp/GameLogicTest.kt`
  - **Hecho cuando:** Tests cubren generarObjetosOrdenados y verificarSeparacion
  - Verificación: `./gradlew testDebugUnitTest --tests "com.example.touchapp.GameLogicTest" → BUILD SUCCESSFUL`

- [x] **T5. Implementar pantalla de inicio con botón "Empezar".** · RF-1, RF-7, RF-8
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** El juego muestra pantalla de inicio antes de comenzar
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T6. Implementar controles con flechas.** · RF-14, RF-15, RF-16
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Flechas mueven el personaje izquierda/derecha
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T7. Implementar pausa y continuar.** · RF-9, RF-10
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Pausa detiene el juego y continuar lo reanuda
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [x] **T8. Implementar diálogo de reintentar.** · RF-11, RF-12, RF-13
  - Archivo: `app/src/main/java/com/example/touchapp/GameScreen.kt`
  - **Hecho cuando:** Game Over muestra diálogo con opciones Sí/No
  - Verificación: `./gradlew assembleDebug → BUILD SUCCESSFUL`

- [ ] **T9. Verificación manual en dispositivo.** · RF-1 a RF-16
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

**Completadas: 0 de 9**
