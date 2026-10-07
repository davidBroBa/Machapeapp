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

    // RF-2: Colisión elíptica
    // Semiejes del mapache medidos: 29 x 28. Comida: 24 x 22.
    @Test
    fun `colisionObjeto detecta colision en el centro`() {
        val resultado = colisionObjeto(0f, 0f, 29f, 28f, 24f, 22f)
        assertTrue(resultado)
    }

    @Test
    fun `colisionObjeto detecta colision justo en el borde horizontal`() {
        // 29 + 24 = 53 es el borde exacto: todavia colisiona.
        val resultado = colisionObjeto(52f, 0f, 29f, 28f, 24f, 22f)
        assertTrue(resultado)
    }

    @Test
    fun `colisionObjeto no detecta colision pasado el borde horizontal`() {
        val resultado = colisionObjeto(54f, 0f, 29f, 28f, 24f, 22f)
        assertFalse(resultado)
    }

    @Test
    fun `colisionObjeto no detecta colision pasado el borde vertical`() {
        // 28 + 22 = 50 es el borde exacto: mas alla ya no colisiona.
        val resultado = colisionObjeto(0f, 51f, 29f, 28f, 24f, 22f)
        assertFalse(resultado)
    }

    @Test
    fun `colisionObjeto la elipse es mas ancha que alta`() {
        // Eje horizontal 53dp, vertical 50dp. A 51dp solo cabe en horizontal.
        val horizontal = colisionObjeto(51f, 0f, 29f, 28f, 24f, 22f)
        val vertical = colisionObjeto(0f, 51f, 29f, 28f, 24f, 22f)
        assertTrue(horizontal)
        assertFalse(vertical)
    }

    @Test
    fun `colisionObjeto devuelve false con semiejes degenerados`() {
        // Silueta del mapache sin area.
        assertFalse(colisionObjeto(0f, 0f, 0f, 0f, 24f, 22f))
        // Silueta del objeto sin area.
        assertFalse(colisionObjeto(0f, 0f, 29f, 28f, 0f, 0f))
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
