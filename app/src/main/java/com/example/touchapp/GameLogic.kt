package com.example.touchapp

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Lógica pura del juego Pou.
 * Sin DOM, sin base de datos, sin red.
 */

data class Partida(
    val puntuacion: Int = 0,
    val vidas: Int = 3,
    val notificacionEnviada: Boolean = false
)

enum class EstadoJuego {
    INICIO, JUGANDO, PAUSADO, GAME_OVER
}

data class Posicion(
    val x: Float,
    val y: Float
)

/**
 * Mueve el mapache horizontalmente sin salir de la pantalla.
 * RF-1
 */
fun moverMapache(
    posicionActual: Float,
    posicionTocada: Float,
    anchoPantalla: Float,
    anchoMapache: Float
): Float {
    val mitadMapache = anchoMapache / 2
    val nuevaPosicion = posicionTocada.coerceIn(mitadMapache, anchoPantalla - mitadMapache)
    return nuevaPosicion
}

/**
 * Velocidad de caida de los objetos, en dp por frame a 60 FPS.
 *
 * Rampa suave y con techo: el juego tiene que volverse mas dificil sin
 * llegar a ser imposible. A 0 puntos un objeto tarda unos 6 s en cruzar
 * la pantalla; en el techo, unos 1.6 s.
 */
fun velocidadCaida(puntuacion: Int): Float {
    val base = 2.2f
    val incremento = 0.16f
    val techo = 8.5f
    return (base + puntuacion * incremento).coerceAtMost(techo)
}

/**
 * Posicion del mapa en la parte baja de la caja de 120dp.
 *
 * @param mapacheX esquina izquierda de la imagen del mapache, en dp.
 * @param altoPantalla alto de la pantalla, en dp.
 * @param centroY distancia del borde inferior al centro de la imagen, en dp.
 * @param lado lado de la imagen del mapache, en dp.
 * @param x0 semiancho del mapa en la caja, en dp.
 * @param y0 distancia de la esquina superior de la imagen a la cabeza, en dp.
 * @param y1 distancia de la esquina superior de la imagen al final de la cabeza, en dp.
 */
fun posicionMapa(
    mapacheX: Float,
    altoPantalla: Float,
    centroY: Float,
    lado: Float,
    x0: Float,
    y0: Float,
    y1: Float
): Pair<Float, Float> {
    // La esquina superior de la imagen es centro - lado/2.
    val arriba = altoPantalla - centroY - lado / 2f
    return mapacheX + x0 to arriba + (y0 + y1) / 2f
}

/**
 * Colisión entre un rectángulo (la cabeza del mapache) y una elipse (un objeto).
 *
 * Se mide la distancia desde el centro del objeto hasta el punto del
 * rectángulo más cercano, y se normaliza con los semiejes de la elipse.
 * Si el centro del objeto cae dentro del rectángulo, la distancia es 0 y
 * hay impacto directo.
 *
 * @param dx distancia horizontal del centro del objeto al del rectángulo, en dp.
 * @param dy distancia vertical del centro del objeto al del rectángulo, en dp.
 * @param semiX semiancho del rectángulo, en dp.
 * @param semiY semialtura del rectángulo, en dp.
 * @param radioX semieje horizontal de la elipse del objeto, en dp.
 * @param radioY semieje vertical de la elipse del objeto, en dp.
 */
fun colisionRectElipse(
    dx: Float,
    dy: Float,
    semiX: Float,
    semiY: Float,
    radioX: Float,
    radioY: Float
): Boolean {
    // Sin area no hay nada que tocar.
    if (semiX <= 0f || semiY <= 0f || radioX <= 0f || radioY <= 0f) return false

    // Cuanto sobresale el objeto por cada lado del rectangulo.
    val fueraX = (abs(dx) - semiX).coerceAtLeast(0f)
    val fueraY = (abs(dy) - semiY).coerceAtLeast(0f)

    val normalizadoX = fueraX / radioX
    val normalizadoY = fueraY / radioY

    return (normalizadoX * normalizadoX + normalizadoY * normalizadoY) < 1f
}

/**
 * Verifica si dos rectangulos se solapan. Ejes alineados.
 */
fun solapanRect(
    dx: Float,
    dy: Float,
    semiAX: Float,
    semiAY: Float,
    semiBX: Float,
    semiBY: Float
): Boolean {
    if (semiAX <= 0f || semiAY <= 0f || semiBX <= 0f || semiBY <= 0f) return false
    return abs(dx) < semiAX + semiBX && abs(dy) < semiAY + semiBY
}

/**
 * Procesa comida: suma 1 punto.
 * RF-3
 */
fun procesarComida(puntuacionActual: Int): Int {
    return puntuacionActual + 1
}

/**
 * Procesa basura: resta 1 vida (mínimo 0).
 * RF-4
 */
fun procesarBasura(vidasActuales: Int): Int {
    return (vidasActuales - 1).coerceAtLeast(0)
}

/**
 * Procesa corazón: recupera 1 vida (máximo 3).
 * RF-5
 */
fun procesarCorazon(vidasActuales: Int): Int {
    return (vidasActuales + 1).coerceAtMost(3)
}

/**
 * Verifica si es game over (0 vidas).
 * RF-6
 */
fun verificarGameOver(vidas: Int): Boolean {
    return vidas <= 0
}

/**
 * Calcula la velocidad de caída según la puntuación.
 * La velocidad aumenta progresivamente hasta 20 puntos, luego se mantiene constante.
 * RF-7, RF-8
 */
fun calcularVelocidad(puntuacion: Int): Float {
    val velocidadBase = 2f
    val incrementoPorPunto = 0.5f
    val velocidadMaxima = 12f
    
    val velocidad = velocidadBase + (puntuacion * incrementoPorPunto)
    return velocidad.coerceAtMost(velocidadMaxima)
}

/**
 * Verifica si se debe enviar la notificación "Machape aburrido".
 * Solo se envía una vez por partida, al llegar a 50 puntos.
 * RF-9, RF-10
 */
fun verificarNotificacionAburrido(
    puntuacion: Int,
    notificacionEnviada: Boolean
): Boolean {
    return puntuacion >= 50 && !notificacionEnviada
}

/**
 * Reinicia la partida con valores iniciales.
 * RF-12
 */
fun reiniciarPartida(): Partida {
    return Partida(puntuacion = 0, vidas = 3, notificacionEnviada = false)
}

/**
 * Mueve el personaje a la izquierda.
 * RF-15
 */
fun moverIzquierda(
    posicionActual: Float,
    velocidad: Float,
    limite: Float
): Float {
    return (posicionActual - velocidad).coerceAtLeast(limite)
}

/**
 * Mueve el personaje a la derecha.
 * RF-16
 */
fun moverDerecha(
    posicionActual: Float,
    velocidad: Float,
    limite: Float
): Float {
    return (posicionActual + velocidad).coerceAtMost(limite)
}

/**
 * Genera objetos en columnas ordenadas con separación mínima.
 * RF-4
 */
fun generarObjetosOrdenados(
    anchoPantalla: Float,
    cantidad: Int
): List<Posicion> {
    val separacionMinima = 100f
    val columnas = (anchoPantalla / separacionMinima).toInt().coerceAtLeast(1)
    val posiciones = mutableListOf<Posicion>()
    
    repeat(cantidad) {
        val columna = it % columnas
        val x = columna * separacionMinima + separacionMinima / 2
        posiciones.add(Posicion(x, -50f))
    }
    
    return posiciones
}

/**
 * Verifica que todos los objetos tengan separación mínima.
 * RF-5, RF-6
 */
fun verificarSeparacion(
    posiciones: List<Posicion>,
    separacionMinima: Float
): Boolean {
    for (i in posiciones.indices) {
        for (j in i + 1 until posiciones.size) {
            val dx = posiciones[i].x - posiciones[j].x
            val dy = posiciones[i].y - posiciones[j].y
            val distancia = sqrt(dx * dx + dy * dy)
            if (distancia < separacionMinima) {
                return false
            }
        }
    }
    return true
}
