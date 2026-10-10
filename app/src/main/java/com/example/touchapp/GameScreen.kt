package com.example.touchapp

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

data class Objeto(
    val x: Float,
    val y: Float,
    val tipo: TipoObjeto,
    val imagen: Int
)

enum class TipoObjeto {
    COMIDA, BASURA, CORAZON
}

/**
 * Imagenes que caen y a que pertenecen.
 *
 *   COMIDA   sushi, lasana, helado   -> suman punto
 *   BASURA   ok, gorra, pesa, polilla -> quitan una vida
 *   CORAZON  corazon                  -> recupera una vida
 */
private val IMAGENES_COMIDA =
    listOf(R.drawable.sushi, R.drawable.lasana, R.drawable.helado)

private val IMAGENES_BASURA =
    listOf(R.drawable.ok, R.drawable.gorra, R.drawable.pesa, R.drawable.polilla)

private val IMAGENES_CORAZON = listOf(R.drawable.corazon)

/**
 * Semiejes de la hitbox de cada imagen, en dp.
 *
 * Medidos sobre los pixeles opacos de cada PNG (alfa >= 24) y escalados a
 * la caja de 80dp con ContentScale.Fit. No se puede usar un valor comun:
 * el helado es 26x60dp (alto y estrecho) y la pesa 64x34dp (ancho y bajo).
 *
 *   sushi     48.1 x 43.4   helado    26.2 x 60.5   pesa      64.1 x 34.1
 *   lasana    47.7 x 43.4   ok        56.4 x 49.8   polilla   64.1 x 47.8
 *   gorra     60.5 x 41.7   corazon   53.4 x 43.7
 */
private val HITBOX_POR_IMAGEN: Map<Int, Pair<Float, Float>> = mapOf(
    R.drawable.sushi to (24.1f to 21.7f),
    R.drawable.lasana to (23.8f to 21.7f),
    R.drawable.helado to (13.1f to 30.3f),
    R.drawable.ok to (28.2f to 24.9f),
    R.drawable.gorra to (30.3f to 20.9f),
    R.drawable.pesa to (32.0f to 17.0f),
    R.drawable.polilla to (32.0f to 23.9f),
    R.drawable.corazon to (26.7f to 21.8f)
)

/** Lado de la imagen del mapache, en dp. */
private const val MAPACHE_LADO = 120f

/**
 * Distancia del borde inferior al CENTRO de la imagen del mapache, en dp.
 *
 * Se usa el mismo valor para dibujar y para colisionar: antes el dibujo
 * colocaba la imagen arriba de una caja y la colision usaba el alto de esa
 * caja, con lo que la hitbox quedaba 60dp por encima del mapache.
 */
private const val MAPACHE_CENTRO_Y = 270f

/*
 * Hitbox del mapache: un rectangulo sobre la cara.
 *
 * Medido sobre me.png (1024x1536) dibujado en 120x120dp con
 * ContentScale.Fit. Escalando los pixeles de las bandas de la cara:
 *
 *   banda de ojos  px x 212..832 y 560..700 -> dp x 36.6..85.0, y 43.8..54.7
 *   banda baja     px x 223..831 y 700..780 -> dp x 37.4..84.9, y 54.7..60.9
 *
 * Esas bandas incluyen las orejas, que se salen por los lados. La cara sin
 * orejas va de x 44 a 80, y el hocico baja hasta y 68. Ese es el rectangulo:
 *
 *   x 44..80, y 40..68   ->  36 x 28 dp, centro (62, 54)
 *
 * El centro x=62 coincide con el centro real de la cara medido sobre las
 * bandas. Un rectangulo, no la silueta entera: solo cuenta acertar en la
 * cara, no rozarlo con el cuerpo.
 */
private const val CABEZA_X0 = 44f
private const val CABEZA_Y0 = 40f
private const val CABEZA_X1 = 80f
private const val CABEZA_Y1 = 68f

/**
 * Pinta el rectangulo de la hitbox sobre la pantalla.
 * Sirve para comprobar que el rectangulo coincide con la cara: se enciende
 * a true cuando hay que ajustar, y se apaga para la entrega.
 */
private const val DEBUG_VER_HITBOX = false

/** Lado del objeto que cae, en dp. */
private const val OBJETO_LADO = 80f

/** Separacion minima entre objetos al generarlos, en dp. */
private const val SEPARACION_MINIMA = 120f

/** Desplazamiento del mapache por frame mientras una flecha este pulsada, en dp. */
private const val VELOCIDAD_MAPACHE = 10f

/**
 * Paso minimo al tocar una flecha, en dp.
 * Un toque puede empezar y acabar dentro del mismo frame, asi que el paso
 * se aplica de golpe para que siempre se vea movimiento.
 */
private const val PASO_MINIMO = 56f

@Composable
fun GameScreen(
    roomCode: String,
    myId: String,
    onSalir: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Todo el juego trabaja en dp para no mezclar unidades.
    val anchoPantalla = configuration.screenWidthDp.toFloat()
    val altoPantalla = configuration.screenHeightDp.toFloat()

    var estadoJuego by remember { mutableStateOf(EstadoJuego.INICIO) }
    var puntuacion by remember { mutableStateOf(0) }
    var vidas by remember { mutableStateOf(3) }
    var notificacionEnviada by remember { mutableStateOf(false) }
    var objetos by remember { mutableStateOf(listOf<Objeto>()) }

    val prefs = remember {
        context.getSharedPreferences("machapeapp", Context.MODE_PRIVATE)
    }
    var mejorPuntuacion by remember { mutableStateOf(prefs.getInt("mejorPuntuacion", 0)) }

    // El mapache arranca centrado y siempre dentro de los limites.
    var mapacheX by remember {
        mutableStateOf((anchoPantalla - MAPACHE_LADO) / 2f)
    }

    // Direccion continua mientras la flecha este pulsada.
    var direccion by remember { mutableStateOf(0f) }

    val limiteMapa = remember(anchoPantalla) {
        (anchoPantalla - MAPACHE_LADO).coerceAtLeast(0f)
    }

    /**
     * Aplica direccion y da un paso inmediato para que un toque rapido
     * siempre produzca movimiento visible, ademas del deslizamiento continuo.
     */
    fun deslizar(multiplicador: Float) {
        direccion = multiplicador
        mapacheX = (mapacheX + multiplicador * PASO_MINIMO).coerceIn(0f, limiteMapa)
    }

    val sonido = rememberSonidosJuego(context)

    LaunchedEffect(estadoJuego) {
        if (estadoJuego != EstadoJuego.JUGANDO) return@LaunchedEffect

        var ultimoSpawn = 0L

        while (estadoJuego == EstadoJuego.JUGANDO) {
            // 60 FPS
            val ahora = System.currentTimeMillis()
            val velocidad = velocidadCaida(puntuacion)

            // Deslizar el mapache segun la direccion activa.
            if (direccion != 0f) {
                mapacheX = (mapacheX + direccion * VELOCIDAD_MAPACHE)
                    .coerceIn(0f, limiteMapa)
            }

            // Mover objetos hacia abajo y quitar los que salen.
            objetos = objetos
                .map { it.copy(y = it.y + velocidad) }
                .filter { it.y < altoPantalla + OBJETO_LADO }

            // Generar un objeto cada cierto tiempo, no cada frame.
            if (ahora - ultimoSpawn >= 800L) {
                ultimoSpawn = ahora

                val tipo = when (Random.nextInt(10)) {
                    in 0..5 -> TipoObjeto.COMIDA
                    in 6..8 -> TipoObjeto.BASURA
                    else -> TipoObjeto.CORAZON
                }

                val imagenes = when (tipo) {
                    TipoObjeto.COMIDA -> IMAGENES_COMIDA
                    TipoObjeto.BASURA -> IMAGENES_BASURA
                    TipoObjeto.CORAZON -> IMAGENES_CORAZON
                }
                val imagen = imagenes.random()

                val centroX = MAPACHE_LADO / 2f +
                    Random.nextFloat() * (anchoPantalla - MAPACHE_LADO)

                val candidatos = objetos.filter { it.y < 0f }
                val sinChoque = candidatos.all { existente ->
                    abs(existente.x - centroX) >= SEPARACION_MINIMA
                }

                if (sinChoque) {
                    objetos = objetos +
                        Objeto(x = centroX, y = -OBJETO_LADO, tipo = tipo, imagen = imagen)
                }
            }

            // Colision contra el rectangulo de la cabeza del mapache.
            // El rectangulo y el dibujo comparten la misma posicion, para
            // que la hitbox no se desvíe de la imagen.
            val (cabezaCentroX, cabezaCentroY) = posicionMapa(
                mapacheX = mapacheX,
                altoPantalla = altoPantalla,
                centroY = MAPACHE_CENTRO_Y,
                lado = MAPACHE_LADO,
                x0 = (CABEZA_X0 + CABEZA_X1) / 2f,
                y0 = CABEZA_Y0,
                y1 = CABEZA_Y1
            )
            val cabezaSemiX = (CABEZA_X1 - CABEZA_X0) / 2f
            val cabezaSemiY = (CABEZA_Y1 - CABEZA_Y0) / 2f

            val colisionados = objetos.filter { objeto ->
                val (objetoRx, objetoRy) = HITBOX_POR_IMAGEN[objeto.imagen]
                    ?: (OBJETO_LADO / 4f to OBJETO_LADO / 4f)

                colisionRectElipse(
                    dx = objeto.x - cabezaCentroX,
                    dy = objeto.y - cabezaCentroY,
                    semiX = cabezaSemiX,
                    semiY = cabezaSemiY,
                    radioX = objetoRx,
                    radioY = objetoRy
                )
            }

            if (colisionados.isNotEmpty()) {
                var puntosNuevos = puntuacion
                var vidasNuevas = vidas

                colisionados.forEach { objeto ->
                    when (objeto.tipo) {
                        TipoObjeto.COMIDA -> puntosNuevos = procesarComida(puntosNuevos)
                        TipoObjeto.BASURA -> vidasNuevas = procesarBasura(vidasNuevas)
                        TipoObjeto.CORAZON -> vidasNuevas = procesarCorazon(vidasNuevas)
                    }
                    sonido.reproducir(objeto.tipo)
                }

                puntuacion = puntosNuevos
                vidas = vidasNuevas
                objetos = objetos - colisionados.toSet()

                if (puntosNuevos > mejorPuntuacion) {
                    mejorPuntuacion = puntosNuevos
                    prefs.edit().putInt("mejorPuntuacion", mejorPuntuacion).apply()
                }

                if (verificarGameOver(vidasNuevas)) {
                    sonido.reproducirGameOver()
                    estadoJuego = EstadoJuego.GAME_OVER
                }
            }

            delay(16)
        }
    }

    // Notificacion "Machape aburrido" al llegar a 50 puntos.
    LaunchedEffect(puntuacion) {
        if (verificarNotificacionAburrido(puntuacion, notificacionEnviada)) {
            notificacionEnviada = true
            FirebaseDatabase.getInstance()
                .getReference("touch/$roomCode")
                .setValue(
                    mapOf(
                        "text" to "bored_attention",
                        "sender" to myId
                    )
                )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB))
    ) {
        when (estadoJuego) {
            EstadoJuego.INICIO -> {
                PantallaInicio(
                    mejorPuntuacion = mejorPuntuacion,
                    onEmpezar = { estadoJuego = EstadoJuego.JUGANDO }
                )
            }

            EstadoJuego.JUGANDO, EstadoJuego.PAUSADO -> {
                ObjetosEnPantalla(objetos)

                MapacheEnPantalla(mapacheX, altoPantalla)

                if (DEBUG_VER_HITBOX) {
                    VerHitbox(mapacheX, altoPantalla)
                }

                MarcadorSuperior(puntuacion, vidas, mejorPuntuacion)

                BotonPausa(
                    pausado = estadoJuego == EstadoJuego.PAUSADO,
                    onToggle = {
                        estadoJuego = if (estadoJuego == EstadoJuego.JUGANDO) {
                            EstadoJuego.PAUSADO
                        } else {
                            EstadoJuego.JUGANDO
                        }
                    }
                )

                FlechasDireccion(
                    onPresionarIzquierda = { deslizar(-1f) },
                    onPresionarDerecha = { deslizar(1f) },
                    onSoltar = { direccion = 0f }
                )

                if (estadoJuego == EstadoJuego.PAUSADO) {
                    OverlayPausa()
                }
            }

            EstadoJuego.GAME_OVER -> {
                DialogoReintentar(
                    puntuacion = puntuacion,
                    onReintentar = {
                        puntuacion = 0
                        vidas = 3
                        notificacionEnviada = false
                        objetos = emptyList()
                        estadoJuego = EstadoJuego.JUGANDO
                    },
                    onSalir = { estadoJuego = EstadoJuego.INICIO }
                )
            }
        }
    }
}

/**
 * Dibuja los objetos que caen usando la imagen de su tipo.
 *
 * `objeto.x` y `objeto.y` son el CENTRO del objeto, que es donde la
 * colision los mira. Modifier.offset coloca la esquina superior izquierda,
 * asi que hay que restar media caja para que se dibujen centrados. Sin esa
 * resta el objeto se dibujaba 40dp a la izquierda y 40dp por encima de
 * donde colisionaba, y se comia cosas que no tocaba.
 */
@Composable
private fun ObjetosEnPantalla(objetos: List<Objeto>) {
    objetos.forEach { objeto ->
        Image(
            painter = painterResource(id = objeto.imagen),
            contentDescription = null,
            modifier = Modifier
                .size(OBJETO_LADO.dp)
                .offset(
                    x = esquinaObjeto(objeto.x, OBJETO_LADO).dp,
                    y = esquinaObjeto(objeto.y, OBJETO_LADO).dp
                )
        )
    }
}

/**
 * Dibuja el mapache.
 *
 * El offset sale de MAPACHE_CENTRO_Y, el mismo valor que usa la colision,
 * de modo que el rectangulo de la cabeza cae siempre sobre la cabeza.
 */
@Composable
private fun BoxScope.MapacheEnPantalla(mapacheX: Float, altoPantalla: Float) {
    Image(
        painter = painterResource(id = R.drawable.me),
        contentDescription = "Mapache",
        modifier = Modifier
            .size(MAPACHE_LADO.dp)
            .offset(
                x = mapacheX.dp,
                y = (altoPantalla - MAPACHE_CENTRO_Y - MAPACHE_LADO / 2f).dp
            )
    )
}

/** Pinta la hitbox de la cara para poder ver si coincide con la imagen. */
@Composable
private fun BoxScope.VerHitbox(mapacheX: Float, altoPantalla: Float) {
    val (centroX, centroY) = posicionMapa(
        mapacheX = mapacheX,
        altoPantalla = altoPantalla,
        centroY = MAPACHE_CENTRO_Y,
        lado = MAPACHE_LADO,
        x0 = (CABEZA_X0 + CABEZA_X1) / 2f,
        y0 = CABEZA_Y0,
        y1 = CABEZA_Y1
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        // El DrawScope trabaja en pixeles, y posicionMapa devuelve dp.
        val semiX = (CABEZA_X1 - CABEZA_X0) / 2f
        val semiY = (CABEZA_Y1 - CABEZA_Y0) / 2f

        drawRect(
            color = Color.Red,
            topLeft = Offset((centroX - semiX).dp.toPx(), (centroY - semiY).dp.toPx()),
            size = Size((semiX * 2f).dp.toPx(), (semiY * 2f).dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.Yellow,
            radius = 5.dp.toPx(),
            center = Offset(centroX.dp.toPx(), centroY.dp.toPx())
        )
    }
}

/**
 * Marcador: puntos, mejor marca y vidas.
 *
 * Va en columna pegada a la izquierda en vez de repartido en una fila: con
 * tres textos en SpaceBetween, "Vidas" caia justo debajo del boton de
 * pausa y no se veia en pantallas anchas. La zona de arriba a la derecha
 * queda libre para el boton.
 *
 * El statusBarsPadding no es cosmetico: sin el, "Puntos" queda pegado al
 * reloj de la barra de estado.
 */
@Composable
private fun BoxScope.MarcadorSuperior(
    puntuacion: Int,
    vidas: Int,
    mejorPuntuacion: Int
) {
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .statusBarsPadding()
            .padding(start = 16.dp, top = 8.dp, end = 140.dp)
    ) {
        Text(
            text = "Puntos: $puntuacion",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Mejor: $mejorPuntuacion",
            color = Color(0xFFFFF3B0),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "Vidas: $vidas",
            color = if (vidas <= 1) Color(0xFFFF8A80) else Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/** Boton de pausa arriba a la derecha. */
@Composable
private fun BoxScope.BotonPausa(pausado: Boolean, onToggle: () -> Unit) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
    ) {
        Text(if (pausado) "Continuar" else "Pausa")
    }
}

/** Flechas de direccion, en los lados, por encima de la barra de navegacion. */
@Composable
private fun BoxScope.FlechasDireccion(
    onPresionarIzquierda: () -> Unit,
    onPresionarDerecha: () -> Unit,
    onSoltar: () -> Unit
) {
    Flecha(
        etiqueta = "←",
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 24.dp, bottom = 150.dp),
        onPresionar = onPresionarIzquierda,
        onSoltar = onSoltar
    )

    Flecha(
        etiqueta = "→",
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 24.dp, bottom = 150.dp),
        onPresionar = onPresionarDerecha,
        onSoltar = onSoltar
    )
}

/**
 * Flecha que mantiene la direccion mientras esta pulsada.
 * Usa gest propio (no el click de Material) para no competir por el evento,
 * y cambia de color al pulsarse para que se vea el estado real.
 */
@Composable
private fun Flecha(
    etiqueta: String,
    modifier: Modifier = Modifier,
    onPresionar: () -> Unit,
    onSoltar: () -> Unit
) {
    var pulsada by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(width = 96.dp, height = 64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(if (pulsada) Color(0xFF4A2D8F) else Color(0xFF7B4FD4))
            .pointerInput(etiqueta) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pulsada = true
                    onPresionar()

                    // Mantiene la direccion hasta que no quede ningun dedo.
                    do {
                        val evento = awaitPointerEvent()
                        if (evento.changes.none { it.pressed }) break
                    } while (true)

                    pulsada = false
                    onSoltar()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = etiqueta,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

/** Capa oscura con la palabra PAUSA. */
@Composable
private fun OverlayPausa() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PAUSA",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

/** Pantalla de inicio con la mejor marca y el boton Empezar. */
@Composable
fun PantallaInicio(mejorPuntuacion: Int, onEmpezar: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MachapeJuego",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mejor marca: $mejorPuntuacion",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFF3B0)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onEmpezar) {
                Text("Empezar")
            }
        }
    }
}

/** Dialogo de Game Over que pregunta si se reintenta. */
@Composable
fun DialogoReintentar(
    puntuacion: Int,
    onReintentar: () -> Unit,
    onSalir: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(32.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Puntuación: $puntuacion",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¿Quieres intentar de nuevo?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onReintentar) {
                    Text("Sí")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onSalir) {
                    Text("No")
                }
            }
        }
    }
}

/** Carga los cuatro sonidos del juego una sola vez por pantalla. */
@Composable
private fun rememberSonidosJuego(context: android.content.Context): SonidosJuego {
    val pool = remember {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val sonidos = remember {
        SonidosJuego(
            pool = pool,
            comer = pool.load(context, R.raw.comer, 1),
            error = pool.load(context, R.raw.error, 1),
            vida = pool.load(context, R.raw.vida, 1),
            gameOver = pool.load(context, R.raw.gameover, 1)
        )
    }

    DisposableEffect(Unit) {
        onDispose { pool.release() }
    }

    return sonidos
}

/** Envoltorio de los sonidos del juego. */
private class SonidosJuego(
    private val pool: SoundPool,
    private val comer: Int,
    private val error: Int,
    private val vida: Int,
    private val gameOver: Int
) {
    /** Reproduce el sonido segun el tipo de objeto tocado. */
    fun reproducir(tipo: TipoObjeto) {
        val id = when (tipo) {
            TipoObjeto.COMIDA -> comer
            TipoObjeto.BASURA -> error
            TipoObjeto.CORAZON -> vida
        }
        if (id != 0) pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    /** Reproduce el sonido de Game Over. */
    fun reproducirGameOver() {
        if (gameOver != 0) pool.play(gameOver, 1f, 1f, 1, 0, 1f)
    }
}
