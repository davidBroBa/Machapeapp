# Plan 002 — Fixes del Juego Pou

- **Spec:** [`spec.md`](spec.md) — **Estado:** aprobada
- **Constitución:** [`../../constitution.md`](../../constitution.md)
- **Fecha:** 2026-10-06

> El plan responde al **CÓMO**. El QUÉ y el POR QUÉ viven en `spec.md`.

---

## 1. Qué RF cubre este plan

| RF | Dónde se cubre en este plan |
|---|---|
| RF-1 | §3 `EstadoInicio`, §5 Pantalla de inicio |
| RF-2 | §3 `MoverPersonaje`, §5 Controles |
| RF-3 | §3 `MoverPersonaje`, §5 Controles |
| RF-4 | §3 `GenerarObjetosOrdenados`, §4 Algoritmo de generación |
| RF-5 | §3 `VerificarSeparacion`, §4 Algoritmo de separación |
| RF-6 | §3 `VerificarSeparacion`, §4 Algoritmo de separación |
| RF-7 | §3 `EstadoInicio`, §5 Pantalla de inicio |
| RF-8 | §3 `EmpezarJuego`, §5 Pantalla de inicio |
| RF-9 | §3 `PausarJuego`, §5 Pausa |
| RF-10 | §3 `ContinuarJuego`, §5 Pausa |
| RF-11 | §3 `MostrarDialogoReintentar`, §5 Game Over |
| RF-12 | §3 `ReiniciarPartida`, §5 Game Over |
| RF-13 | §3 `VolverAlInicio`, §5 Game Over |
| RF-14 | §5 Interfaz |
| RF-15 | §3 `MoverIzquierda`, §5 Controles |
| RF-16 | §3 `MoverDerecha`, §5 Controles |

---

## 2. Archivos creados o modificados

| Archivo | Acción | Responsabilidad (una frase) | RF |
|---|---|---|---|
| `GameScreen.kt` | modificar | Agregar estados de juego, controles con flechas, pausa y diálogo de reintentar | RF-1 a RF-16 |
| `GameLogic.kt` | modificar | Agregar funciones para movimiento con flechas y generación ordenada | RF-4, RF-5, RF-6, RF-15, RF-16 |

---

## 3. Lógica de negocio (funciones puras)

| Función | Archivo | Entrada | Salida | RF |
|---|---|---|---|---|
| `moverIzquierda` | `GameLogic.kt` | `posicionActual: Float, velocidad: Float, limite: Float` | `nuevaPosicion: Float` | RF-15 |
| `moverDerecha` | `GameLogic.kt` | `posicionActual: Float, velocidad: Float, limite: Float` | `nuevaPosicion: Float` | RF-16 |
| `generarObjetosOrdenados` | `GameLogic.kt` | `anchoPantalla: Float, cantidad: Int` | `List<Posicion>` | RF-4 |
| `verificarSeparacion` | `GameLogic.kt` | `posiciones: List<Posicion>, separacionMinima: Float` | `Boolean` | RF-5, RF-6 |

---

## 4. Algoritmo

```
INICIO
  -> mostrar pantalla de inicio con botón "Empezar"
  -> SI usuario presiona "Empezar":
      -> inicializar partida (puntuacion=0, vidas=3)
      -> mostrar personaje en la parte inferior
      -> mostrar flechas de dirección
      -> MIENTRAS partida activa:
          -> SI usuario presiona flecha izquierda: mover personaje izquierda
          -> SI usuario presiona flecha derecha: mover personaje derecha
          -> SI usuario presiona "Pausa": detener game loop
          -> SI usuario presiona "Continuar": reanudar game loop
          -> generar objetos en columnas separadas
          -> mover objetos hacia abajo
          -> verificar colisiones
          -> SI vidas == 0:
              -> mostrar diálogo "¿Quieres intentar de nuevo?"
              -> SI "Sí": reiniciar partida
              -> SI "No": volver a pantalla de inicio
  -> FIN
```

---

## 5. Interfaz

| Elemento | Comportamiento esperado | RF |
|---|---|---|
| Pantalla de inicio | Muestra "Empezar" antes de comenzar | RF-1, RF-7, RF-8 |
| Personaje | Visible en la parte inferior | RF-1 |
| Flecha izquierda | Mueve personaje a la izquierda | RF-14, RF-15 |
| Flecha derecha | Mueve personaje a la derecha | RF-14, RF-16 |
| Botón Pausa | Detiene el juego | RF-9 |
| Botón Continuar | Reanuda el juego | RF-10 |
| Diálogo Game Over | Pregunta "¿Quieres intentar de nuevo?" | RF-11, RF-12, RF-13 |

Accesibilidad: Contraste de colores WCAG 2.2 AA, objetivos táctiles mínimo 48dp
Responsive: Verificado a 375px de ancho mínimo

---

## 6. Decisiones técnicas

### 6.1 Estados del juego

- **Elegido:** Enum `EstadoJuego` con valores `INICIO`, `JUGANDO`, `PAUSADO`, `GAME_OVER`
- **Alternativa descartada:** Booleanos separados (`estaPausado`, `estaEnInicio`)
- **Por qué:** Un solo estado es más claro y evita combinaciones inválidas
- **Coste que acceptamos:** Ninguno

### 6.2 Controles con flechas

- **Elegido:** Dos botones con flechas (← →) en la parte inferior
- **Alternativa descartada:** Arrastrar el dedo o tocar la pantalla
- **Por qué:** Más preciso y fácil de usar
- **Coste que acceptamos:** Ocupa espacio en la pantalla

### 6.3 Generación ordenada

- **Elegido:** Generar objetos en columnas fijas con separación mínima
- **Alternativa descartada:** Generar aleatoriamente y verificar separación después
- **Por qué:** Más eficiente y garantiza separación desde el inicio
- **Coste que acceptamos:** Menos variedad en la posición de los objetos

---

## 7. Estrategia de pruebas

| Nivel | Qué cubre | Comando | RF |
|---|---|---|---|
| Unitario | Lógica pura: movimiento, separación | `./gradlew testDebugUnitTest` | RF-4, RF-5, RF-6, RF-15, RF-16 |
| Manual | Interfaz: flechas, pausa, game over | Pasos manuales en dispositivo | RF-1 a RF-3, RF-7 a RF-14 |

---

## 8. Cumplimiento de la constitución

| Principio | Cómo lo respeta este plan |
|---|---|
| Tipado estricto | Enum `EstadoJuego` con valores tipados |
| Test-first | Tests unitarios antes de la implementación |
| Cero secretos | No hay secretos en el código |
| Errores explícitos | Manejo de estados inválidos |
| Sin lógica de negocio en UI | `GameLogic.kt` separa la lógica pura |

---

## 9. Riesgos del plan

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Flechas ocupan espacio | Menos espacio para el juego | Tamaño de flechas 48dp, colocadas en la parte inferior |
| Generación ordenada menos variedad | Juego menos dinámico | Variar la columna de cada objeto generado |
