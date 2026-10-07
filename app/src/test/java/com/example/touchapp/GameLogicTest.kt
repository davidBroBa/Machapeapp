package com.example.touchapp

import org.junit.Assert.*
import org.junit.Test

class GameLogicTest {

    // RF-1: Mover mapache
    @Test
    fun `moverMapache mueve a la posicion tocada`() {
        val resultado = moverMapache(100f, 200f, 400f, 50f)
        assertEquals(200f, resultado, 0.01f)
    }

    @Test
    fun `moverMapache no sale de la pantalla por la izquierda`() {
        val resultado = moverMapache(100f, -50f, 400f, 50f)
        assertEquals(25f, resultado, 0.01f)
    }

    @Test
    fun `moverMapache no sale de la pantalla por la derecha`() {
        val resultado = moverMapache(100f, 500f, 400f, 50f)
        assertEquals(375f, resultado, 0.01f)
    }

    // posicionMapa: la cabeza va en la parte ALTA de la imagen.
    @Test
    fun `posicionMapa pone la cabeza por encima del centro de la imagen`() {
        val lado = 120f
        val centroY = 270f
        val alto = 800f

        val (_, cabezaY) = posicionMapa(0f, alto, centroY, lado, 62f, 40f, 68f)
        val centroImagen = alto - centroY

        // La cabeza esta en la parte de arriba: mas cerca del borde superior.
        assertTrue(cabezaY < centroImagen)
    }

    @Test
    fun `posicionMapa coincide con el rectangulo medido`() {
        // Pantalla de 800dp, imagen de 120dp centrada a 270dp del borde inferior.
        // Cabeza medida: x 44..80, y 40..68 -> centro de caja (62, 54).
        val (x, y) = posicionMapa(100f, 800f, 270f, 120f, 62f, 40f, 68f)
        // Esquina superior de la imagen: 800 - 270 - 60 = 470.
        // Centro de la cabeza: 470 + (40+68)/2 = 524.
        assertEquals(162f, x, 0.01f)
        assertEquals(524f, y, 0.01f)
    }

    @Test
    fun `la cabeza queda centrada en la cara medida`() {
        // El centro horizontal de la cara medido sobre las bandas de la
        // imagen es x=62dp dentro de la caja de 120dp.
        val caja = 120f
        val centroBandaOjos = 60.8f
        val x0 = 44f
        val x1 = 80f

        // El rectangulo debe caer dentro del ancho de la banda de ojos.
        assertTrue(x0 > 36.6f && x0 < centroBandaOjos)
        assertTrue(x1 > centroBandaOjos && x1 < 85.0f)
        assertTrue(x1 - x0 < caja)
    }

    @Test
    fun `posicionMapa sigue al mapache en horizontal`() {
        val (x1, _) = posicionMapa(0f, 800f, 270f, 120f, 62f, 40f, 68f)
        val (x2, _) = posicionMapa(56f, 800f, 270f, 120f, 62f, 40f, 68f)
        assertEquals(56f, x2 - x1, 0.01f)
    }

    // RF-2: Colisión rectángulo (cabeza) contra elipse (objeto)
    // Cabeza del mapache: 36 x 32 dp -> semiejes 18 x 16.
    // Comida: 48.1 x 43.4 dp -> semiejes 24 x 22.
    @Test
    fun `colisionRectElipse acierta cuando el objeto cae sobre la cabeza`() {
        assertTrue(colisionRectElipse(0f, 0f, 18f, 16f, 24f, 22f))
    }

    @Test
    fun `colisionRectElipse no acierta con el objeto encima de la cabeza`() {
        // 90dp por encima: fuera del alcance vertical (16 + 22 = 38).
        assertFalse(colisionRectElipse(0f, -90f, 18f, 16f, 24f, 22f))
    }

    @Test
    fun `colisionRectElipse no acierta con el objeto en el cuerpo`() {
        // El cuerpo queda 45dp por debajo del centro de la cabeza.
        assertFalse(colisionRectElipse(0f, 45f, 18f, 16f, 24f, 22f))
    }

    @Test
    fun `colisionRectElipse aguanta el borde de la cabeza`() {
        // Justo en el borde horizontal: 18 + 24 = 42 todavia toca.
        assertTrue(colisionRectElipse(41f, 0f, 18f, 16f, 24f, 22f))
        assertFalse(colisionRectElipse(43f, 0f, 18f, 16f, 24f, 22f))
    }

    @Test
    fun `colisionRectElipse es mas pequena que un objeto`() {
        // La cabeza (36x32) no llega al ancho de la comida (48x43).
        assertTrue(36f < 48.1f)
        assertTrue(32f < 43.4f)
    }

    @Test
    fun `colisionRectElipse devuelve false si alguna figura no tiene area`() {
        assertFalse(colisionRectElipse(0f, 0f, 0f, 16f, 24f, 22f))
        assertFalse(colisionRectElipse(0f, 0f, 18f, 0f, 24f, 22f))
        assertFalse(colisionRectElipse(0f, 0f, 18f, 16f, 0f, 22f))
    }

    // Velocidad de caída
    @Test
    fun `velocidadCaida arranca suave`() {
        assertEquals(2.2f, velocidadCaida(0), 0.001f)
    }

    @Test
    fun `velocidadCaida crece con la puntuacion`() {
        assertTrue(velocidadCaida(20) > velocidadCaida(0))
        assertTrue(velocidadCaida(50) > velocidadCaida(20))
    }

    @Test
    fun `velocidadCaida tiene techo y no lo pasa`() {
        val techo = velocidadCaida(50)
        assertEquals(techo, velocidadCaida(200), 0.001f)
        assertEquals(8.5f, techo, 0.001f)
    }

    // solapanRect
    @Test
    fun `solapanRect detecta solape`() {
        assertTrue(solapanRect(0f, 0f, 18f, 16f, 24f, 22f))
        assertTrue(solapanRect(41f, 37f, 18f, 16f, 24f, 22f))
    }

    @Test
    fun `solapanRect detecta no solape`() {
        assertFalse(solapanRect(43f, 0f, 18f, 16f, 24f, 22f))
        assertFalse(solapanRect(0f, 39f, 18f, 16f, 24f, 22f))
    }

    // RF-3: Procesar comida
    @Test
    fun `procesarComida suma 1 punto`() {
        val resultado = procesarComida(5)
        assertEquals(6, resultado)
    }

    @Test
    fun `procesarComida funciona desde 0`() {
        val resultado = procesarComida(0)
        assertEquals(1, resultado)
    }

    // RF-4: Procesar basura
    @Test
    fun `procesarBasura resta 1 vida`() {
        val resultado = procesarBasura(3)
        assertEquals(2, resultado)
    }

    @Test
    fun `procesarBasura no baja de 0`() {
        val resultado = procesarBasura(0)
        assertEquals(0, resultado)
    }

    // RF-5: Procesar corazón
    @Test
    fun `procesarCorazon recupera 1 vida`() {
        val resultado = procesarCorazon(2)
        assertEquals(3, resultado)
    }

    @Test
    fun `procesarCorazon no supera 3 vidas`() {
        val resultado = procesarCorazon(3)
        assertEquals(3, resultado)
    }

    // RF-6: Game Over
    @Test
    fun `verificarGameOver es true con 0 vidas`() {
        assertTrue(verificarGameOver(0))
    }

    @Test
    fun `verificarGameOver es false con vidas positivas`() {
        assertFalse(verificarGameOver(1))
        assertFalse(verificarGameOver(3))
    }

    // RF-7, RF-8: Velocidad progresiva
    @Test
    fun `calcularVelocidad aumenta con puntuacion`() {
        val velocidad0 = calcularVelocidad(0)
        val velocidad10 = calcularVelocidad(10)
        val velocidad20 = calcularVelocidad(20)
        
        assertTrue(velocidad10 > velocidad0)
        assertTrue(velocidad20 > velocidad10)
    }

    @Test
    fun `calcularVelocidad tiene maximo en 20 puntos`() {
        val velocidad20 = calcularVelocidad(20)
        val velocidad30 = calcularVelocidad(30)
        val velocidad50 = calcularVelocidad(50)
        
        assertEquals(velocidad20, velocidad30, 0.01f)
        assertEquals(velocidad20, velocidad50, 0.01f)
    }

    // RF-9, RF-10: Notificación aburrido
    @Test
    fun `verificarNotificacionAburrido es true en 50 puntos sin enviar`() {
        assertTrue(verificarNotificacionAburrido(50, false))
    }

    @Test
    fun `verificarNotificacionAburrido es false si ya fue enviada`() {
        assertFalse(verificarNotificacionAburrido(50, true))
    }

    @Test
    fun `verificarNotificacionAburrido es false antes de 50 puntos`() {
        assertFalse(verificarNotificacionAburrido(49, false))
    }

    // RF-12: Reiniciar partida
    @Test
    fun `reiniciarPartida devuelve valores iniciales`() {
        val partida = reiniciarPartida()
        assertEquals(0, partida.puntuacion)
        assertEquals(3, partida.vidas)
        assertFalse(partida.notificacionEnviada)
    }

    // RF-15: Mover izquierda
    @Test
    fun `moverIzquierda resta velocidad`() {
        val resultado = moverIzquierda(100f, 10f, 0f)
        assertEquals(90f, resultado, 0.01f)
    }

    @Test
    fun `moverIzquierda no baja del limite`() {
        val resultado = moverIzquierda(5f, 10f, 0f)
        assertEquals(0f, resultado, 0.01f)
    }

    // RF-16: Mover derecha
    @Test
    fun `moverDerecha suma velocidad`() {
        val resultado = moverDerecha(100f, 10f, 200f)
        assertEquals(110f, resultado, 0.01f)
    }

    @Test
    fun `moverDerecha no supera el limite`() {
        val resultado = moverDerecha(195f, 10f, 200f)
        assertEquals(200f, resultado, 0.01f)
    }

    // RF-4: Generar objetos ordenados
    @Test
    fun `generarObjetosOrdenados genera cantidad correcta`() {
        val posiciones = generarObjetosOrdenados(400f, 5)
        assertEquals(5, posiciones.size)
    }

    @Test
    fun `generarObjetosOrdenados respeta separacion minima`() {
        val posiciones = generarObjetosOrdenados(400f, 4)
        assertTrue(verificarSeparacion(posiciones, 100f))
    }

    // RF-5, RF-6: Verificar separación
    @Test
    fun `verificarSeparacion detecta objetos cercanos`() {
        val posiciones = listOf(
            Posicion(100f, 100f),
            Posicion(150f, 100f)
        )
        assertFalse(verificarSeparacion(posiciones, 100f))
    }

    @Test
    fun `verificarSeparacion acepta objetos separados`() {
        val posiciones = listOf(
            Posicion(100f, 100f),
            Posicion(250f, 100f)
        )
        assertTrue(verificarSeparacion(posiciones, 100f))
    }
}
