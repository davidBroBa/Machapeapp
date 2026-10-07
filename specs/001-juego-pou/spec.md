# Spec 001 — Juego Pou

**Estado:** `borrador`
**Fecha:** 2026-10-06
**Constitución:** [`../../constitution.md`](../../constitution.md) · **SDD:** [`../../docs/SDD.md`](../../docs/SDD.md)

> La spec describe el **QUÉ** y el **POR QUÉ**. Nada de stack, arquitectura ni
> nombres de archivo: eso va en `plan.md`.

---

## 1. Contexto y objetivo

La app Machapeapp actualmente solo permite enviar señales de atención entre parejas. Se quiere agregar un juego estilo Pou para que los usuarios se entretengan mientras esperan atención, fortaleciendo el vínculo emocional con una experiencia divertida y compartida.

## 2. Usuarios / actores

| Actor | Qué necesita |
|---|---|
| Usuario individual | Jugar solo en su dispositivo, sin conexión con el otro |
| Pareja (indirecto) | Recibir notificación cuando el otro usuario alcanza 50 puntos |

## 3. Historias de usuario

- **HU-1.** Como usuario, quiero jugar un juego de atrapar comida para entretenerme mientras espero atención.
- **HU-2.** Como usuario, quiero recibir una notificación cuando mi pareja alcanza 50 puntos, para saber que está aburrida.
- **HU-3.** Como usuario, quiero ver mi puntuación y vidas durante el juego.
- **HU-4.** Como usuario, quiero poder reiniciar o salir del juego cuando pierdo.

## 4. Definiciones

| Término | Significado exacto |
|---|---|
| Comida | Objetos que suman puntos: sushi, lasaña, helado |
| Corazón | Objeto especial que recupera 1 vida (máximo 3) |
| Basura | Objetos que restan 1 vida: "ok", gorra, pesa, polilla |
| Puntuación | Puntos acumulados durante la partida |
| Vidas | 3 vidas iniciales; se pierde 1 al tocar basura |

---

## 5. Requisitos funcionales

### Mecánica del juego

- **RF-1.** CUANDO el usuario toca la pantalla, EL SISTEMA mueve el mapache horizontalmente a la posición tocada.
- **RF-2.** CUANDO un objeto cae y toca el mapache, EL SISTEMA determina si es comida o basura.
- **RF-3.** CUANDO el mapache toca comida (sushi, lasaña, helado), EL SISTEMA suma 1 punto y reproduce sonido de comer.
- **RF-4.** CUANDO el mapache toca basura ("ok", gorra, pesa, polilla), EL SISTEMA resta 1 vida y reproduce sonido de error.
- **RF-5.** CUANDO el mapache toca un corazón, EL SISTEMA recupera 1 vida (máximo 3) y reproduce sonido especial.
- **RF-6.** SI el mapa llega a 0 vidas, ENTONCES EL SISTEMA muestra pantalla de Game Over.

### Dificultad progresiva

- **RF-7.** MIENTRAS la puntuación está entre 0 y 20 puntos, EL SISTEMA aumenta gradualmente la velocidad de caída de los objetos.
- **RF-8.** SI la puntuación supera 20 puntos, ENTONCES EL SISTEMA mantiene la velocidad máxima constante.

### Notificación de pareja

- **RF-9.** CUANDO la puntuación alcanza 50 puntos, EL SISTEMA envía una notificación push al otro usuario de la sala con el mensaje "Machape aburrido".
- **RF-10.** SI el otro usuario no está conectado, ENTONCES EL SISTEMA no envía la notificación.

### Game Over

- **RF-11.** CUANDO se muestra Game Over, EL SISTEMA muestra dos botones: "Jugar otra vez" y "Salir".
- **RF-12.** CUANDO el usuario toca "Jugar otra vez", EL SISTEMA reinicia la partida con 3 vidas y 0 puntos.
- **RF-13.** CUANDO el usuario toca "Salir", EL SISTEMA regresa a la pantalla principal de la app.

### Puntuación

- **RF-14.** EL SISTEMA muestra la puntuación actual en la parte superior de la pantalla.
- **RF-15.** EL SISTEMA muestra las vidas restantes (3 corazones) en la parte superior.

---

## 6. Requisitos no funcionales

| Tipo | Requisito | Cómo se mide |
|---|---|---|
| Rendimiento | El juego corre a 60 FPS en dispositivos de gama media | Pruebas en Samsung Galaxy S25 Ultra |
| Accesibilidad | WCAG 2.2 AA | Contraste de colores, tamaño de objetivos táctiles |
| Compatibilidad | Android 7.0+ (minSdk 24) | Pruebas en diferentes versiones |

---

## 7. Casos límite

| Caso | Comportamiento esperado | RF que lo cubre |
|---|---|---|
| Usuario toca fuera del mapa | El mapa no se mueve fuera de la pantalla | RF-1 |
| Objeto cae fuera de la pantalla | El objeto desaparece sin penalización | RF-2 |
| Puntuación negativa | No se permite; mínimo 0 puntos | RF-3, RF-4 |
| Vidas negativas | No se permite; mínimo 0 vidas | RF-4, RF-5 |
| Notificación duplicada | Solo se envía una vez por partida | RF-9 |

---

## 8. Fuera de alcance

- No hay multijugador en tiempo real
- No hay leaderboard global
- No hay compras in-app
- No hay personalización de personajes
- No hay niveles ni escenarios diferentes

---

## 9. Criterios de finalización

- [ ] Todos los RF tienen al menos un test o una verificación manual, y pasan.
- [ ] Tests en verde: `./gradlew test`
- [ ] Sin errores de tipo: `./gradlew compileDebugKotlin`
- [ ] Gates sin fallos críticos
- [ ] RF de interfaz verificados a 375 px
- [ ] Documentación actualizada (`docs/SDD.md`, `MEMORY.md`)

---

## 10. Dudas abiertas

- [NECESITA ACLARACIÓN] ¿Qué imágenes usar para los objetos? El usuario agregará imágenes personalizadas al final.

> Mientras exista una duda `[NECESITA ACLARACIÓN]` marcada como bloqueante, la spec **no** pasa a `aprobada`.

---

## Registro de cambios de requisito

| Fecha | Cambio | RF afectados | Aprobado por |
|---|---|---|---|
| 2026-10-06 | Creación inicial | Todos | — |
