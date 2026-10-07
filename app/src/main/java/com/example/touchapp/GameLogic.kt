package com.example.touchapp

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
 * Verifica si hay colisión elíptica entre el mapache y un objeto.
 *
 * No usa un círculo porque las siluetas reales de las imágenes son elípticas
 * y más anchas que altas. Los radios vienen de medir los píxeles opacos de
 * cada PNG, así la hitbox coincide con lo que el jugador ve.
 *
 * @param dx diferencia horizontal entre centros, en dp.
 * @param dy diferencia vertical entre centros, en dp.
 * @param radioX semieje horizontal de la silueta del mapache, en dp.
 * @param radioY semieje vertical de la silueta del mapache, en dp.
 * @param objetoX semieje horizontal de la silueta del objeto, en dp.
 * @param objetoY semieje vertical de la silueta del objeto, en dp.
 */
fun colisionObjeto(
    dx: Float,
    dy: Float,
    radioX: Float,
    radioY: Float,
    objetoX: Float,
    objetoY: Float
): Boolean {
    // Una silueta sin area (semieje en cero o negativo) no colisiona nunca.
    // Ademas evita division por cero al normalizar.
    if (radioX <= 0f || radioY <= 0f || objetoX <= 0f || objetoY <= 0f) return false

    val ejeX = radioX + objetoX
    val ejeY = radioY + objetoY

    // Normaliza el vector en el espacio elíptico: 1.0 es justo el borde.
    val normalizadoX = dx / ejeX
    val normalizadoY = dy / ejeY
    val distancia = sqrt(normalizadoX * normalizadoX + normalizadoY * normalizadoY)

    return distancia < 1f
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
