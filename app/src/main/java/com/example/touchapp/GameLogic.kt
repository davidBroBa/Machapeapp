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
 * Puntos en los que termina cada tramo de dificultad.
 * El último es el tope: a partir de ahí la velocidad ya no sube.
 */
private val ESCALONES_DIFICULTAD = floatArrayOf(0f, 20f, 30f, 40f, 50f)

/**
 * Velocidad de caída en dp por frame a 60 FPS, en cada escalón.
 *
 * Recorren unos 880 dp de pantalla, así que 2.6 dp/frame tardan unos 5,6 s
 * en cruzar y 9.0 dp/frame unos 1,6 s.
 */
private val VELOCIDADES_TRAMO = floatArrayOf(2.6f, 5.4f, 6.6f, 7.8f, 9.0f)

/**
 * Velocidad de caída de los objetos, en dp por frame a 60 FPS.
 *
 * Sube por tramos en vez de con una rampa recta: de 0 a 20 puntos va
 * suave, y a partir de 20 cada 10 puntos hay un escalón. Desde 50 puntos
 * se queda en el tope, para que el juego no llegue a ser imposible.
 */
fun velocidadCaida(puntuacion: Int): Float {
    val ultimo = ESCALONES_DIFICULTAD.lastIndex

    if (puntuacion <= 0f) return VELOCIDADES_TRAMO[0]
    if (puntuacion >= ESCALONES_DIFICULTAD[ultimo]) return VELOCIDADES_TRAMO[ultimo]

    for (i in 0 until ultimo) {
        if (puntuacion < ESCALONES_DIFICULTAD[i + 1]) {
            val desde = ESCALONES_DIFICULTAD[i]
            val hasta = ESCALONES_DIFICULTAD[i + 1]
            val t = (puntuacion - desde) / (hasta - desde)
            return VELOCIDADES_TRAMO[i] +
                t * (VELOCIDADES_TRAMO[i + 1] - VELOCIDADES_TRAMO[i])
        }
    }

    return VELOCIDADES_TRAMO[ultimo]
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
 * Esquina superior izquierda de la imagen de un objeto, para dibujarla centrada.
 *
 * `Modifier.offset` coloca la esquina superior izquierda, pero la colision
 * usa el centro del objeto. Sin esta conversion el objeto se dibuja media
 * caja a la izquierda y media caja por encima de donde colisiona.
 */
fun esquinaObjeto(centro: Float, lado: Float): Float {
    return centro - lado / 2f
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
 * Puntuacion a la que se dispara la notificacion "Machape aburrido".
 * Coincide con el tope de dificultad: a partir de aqui ya no queda mas
 * dificultad que ganar y se avisa a la otra persona.
 */
const val PUNTOS_NOTIFICACION_ABURRIDO = 50

/**
 * Verifica si se debe enviar la notificación "Machape aburrido".
 * Solo se envía una vez por partida, al llegar al umbral.
 * RF-9, RF-10
 */
fun verificarNotificacionAburrido(
    puntuacion: Int,
    notificacionEnviada: Boolean
): Boolean {
    return puntuacion >= PUNTOS_NOTIFICACION_ABURRIDO && !notificacionEnviada
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
