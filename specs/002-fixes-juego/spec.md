# Spec 002 — Fixes del Juego Pou

**Estado:** `borrador`
**Fecha:** 2026-10-06
**Constitución:** [`../../constitution.md`](../../constitution.md) · **SDD:** [`../../docs/SDD.md`](../../docs/SDD.md)

> La spec describes el **QUÉ** y el **POR QUÉ**. Nada de stack, arquitectura ni
> nombres de archivo: eso va en `plan.md`.

---

## 1. Contexto y objetivo

El juego Pou fue implementado en la spec 001 pero tiene varios bugs que afectan la experiencia de usuario. Esta spec corrige esos bugs para que el juego sea jugable y divertido.

## 2. Usuarios / actores

| Actor | Qué necesita |
|---|---|
| Usuario | Un juego fluido, con personaje visible y controles que funcionen |

## 3. Historias de usuario

- **HU-1.** Como usuario, quiero ver el personaje en la pantalla para saber dónde está.
- **HU-2.** Como usuario, quiero que los objetos caigan de forma ordenada y separada para poder esquivarlos.
- **HU-3.** Como usuario, quiero que el juego tenga un botón de "Empezar" para comenzar cuando esté listo.
- **HU-4.** Como usuario, quiero poder pausar el juego si necesito hacer algo más.
- **HU-5.** Como usuario, quiero que al perder me pregunte si quiero intentar de nuevo.

## 4. Definiciones

| Término | Significado exacto |
|---|---|
| Pantalla de inicio | Pantalla con botón "Empezar" antes de que comience el juego |
| Pausa | El game loop se detiene pero el estado se mantiene |
| Reintentar | Volver a jugar después de perder |

---

## 5. Requisitos funcionales

### Personaje visible y controlable

- **RF-1.** CUANDO el juego está en estado "inicio", EL SISTEMA muestra el personaje en la parte inferior de la pantalla.
- **RF-2.** CUANDO el usuario toca la pantalla, EL SISTEMA mueve el personaje horizontalmente a la posición tocada.
- **RF-3.** SI el usuario toca fuera de los límites de la pantalla, ENTONCES EL SISTEMA no mueve el personaje fuera de la pantalla.
- **RF-14.** EL SISTEMA muestra dos flechas (izquierda y derecha) en la parte inferior de la pantalla para indicar la dirección de movimiento.
- **RF-15.** CUANDO el usuario presiona la flecha izquierda, EL SISTEMA mueve el personaje a la izquierda.
- **RF-16.** CUANDO el usuario presiona la flecha derecha, EL SISTEMA mueve el personaje a la derecha.

### Objetos cayendo ordenadamente

- **RF-4.** CUANDO se generan objetos, EL SISTEMA los coloca en columnas separadas para que no se toquen.
- **RF-5.** MIENTRAS los objetos caen, EL SISTEMA mantiene una separación mínima de 100 píxeles entre ellos.
- **RF-6.** SI dos objetos están demasiado cerca, ENTONCES EL SISTEMA reposiciona uno de ellos.

### Pantalla de inicio

- **RF-7.** CUANDO el usuario entra a la pestaña "Juego", EL SISTEMA muestra una pantalla de inicio con el botón "Empezar".
- **RF-8.** CUANDO el usuario presiona "Empezar", EL SISTEMA comienza el juego con 3 vidas y 0 puntos.

### Pausa

- **RF-9.** CUANDO el usuario presiona el botón "Pausa", EL SISTEMA detiene el game loop pero mantiene el estado actual.
- **RF-10.** CUANDO el usuario presiona "Continuar", EL SISTEMA reanuda el game loop desde donde se detuvo.

### Game Over

- **RF-11.** CUANDO el usuario pierde todas las vidas, EL SISTEMA muestra un diálogo preguntando "¿Quieres intentar de nuevo?".
- **RF-12.** CUANDO el usuario selecciona "Sí", EL SISTEMA reinicia el juego con 3 vidas y 0 puntos.
- **RF-13.** CUANDO el usuario selecciona "No", EL SISTEMA regresa a la pantalla de inicio.

---

## 6. Requisitos no funcionales

| Tipo | Requisito | Cómo se mide |
|---|---|---|
| Rendimiento | El juego corre a 60 FPS | Pruebas en Samsung Galaxy S25 Ultra |
| Accesibilidad | WCAG 2.2 AA | Contraste de colores, objetivos táctiles mínimo 48dp |

---

## 7. Casos límite

| Caso | Comportamiento esperado | RF que lo cubre |
|---|---|---|
| Usuario toca fuera del mapa | El personaje no se mueve fuera de la pantalla | RF-3 |
| Objetos se superponen | Se reposicionan para mantener separación | RF-5, RF-6 |
| Pausa durante game over | No se puede pausar si ya perdió | RF-9 |
| Reintentar después de game over | El juego reinicia correctamente | RF-12 |

---

## 8. Fuera de alcance

- No hay niveles ni escenarios diferentes
- No hay power-ups adicionales
- No hay leaderboard

---

## 9. Criterios de finalización

- [ ] Todos los RF tienen al menos un test o una verificación manual, y pasan.
- [ ] Tests en verde: `./gradlew testDebugUnitTest`
- [ ] Sin errores de tipo: `./gradlew assembleDebug`
- [ ] RF de interfaz verificados a 375 px
- [ ] Documentación actualizada (`MEMORY.md`)

---

## 10. Dudas abiertas

- [NECESITA ACLARACIÓN] ¿El botón de pausa debe estar en la parte superior de la pantalla?

> Mientras exista una duda `[NECESITA ACLARACIÓN]` marcada como bloqueante, la spec **no** pasa a `aprobada`.

---

## Registro de cambios de requisito

| Fecha | Cambio | RF afectados | Aprobado por |
|---|---|---|---|
| 2026-10-06 | Creación inicial | Todos | — |
