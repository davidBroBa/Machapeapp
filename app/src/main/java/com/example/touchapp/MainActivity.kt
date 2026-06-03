package com.example.touchapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance()
            .signInAnonymously()

        createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= 33) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    1
                )
            }
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {

                    Log.e(
                        "FCM",
                        "Error token"
                    )

                    return@addOnCompleteListener
                }

                val token = task.result

                val prefs =
                    getSharedPreferences(
                        "machapeapp",
                        MODE_PRIVATE
                    )

                val roomCode =
                    prefs.getString(
                        "roomCode",
                        ""
                    )

                if (!roomCode.isNullOrEmpty()) {

                    var myId =
                        prefs.getString(
                            "myId",
                            null
                        )

                    if (myId == null) {

                        myId =
                            System.currentTimeMillis()
                                .toString()

                        prefs.edit()
                            .putString(
                                "myId",
                                myId
                            )
                            .apply()
                    }

                    FirebaseDatabase
                        .getInstance()
                        .getReference(
                            "tokens/$roomCode/$myId"
                        )
                        .setValue(token)
                }
            }

        setContent {

            TouchApp(this)
        }
    }
}

fun createNotificationChannel(
    context: Context
) {

    if (
        Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.O
    ) {

        val channel =
            NotificationChannel(
                "touch_channel",
                "Machape Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }
}

fun showNotification(
    context: Context,
    mensaje: String
) {

    if (
        Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val intent =
        Intent(
            context,
            MainActivity::class.java
        )

    intent.flags =
        Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

    val builder =
        NotificationCompat.Builder(
            context,
            "touch_channel"
        )
            .setSmallIcon(
                R.mipmap.ic_launcher
            )
            .setContentTitle(
                "Tu machape te necesita 🥺"
            )
            .setContentText(
                mensaje
            )
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
            .setContentIntent(
                pendingIntent
            )

    NotificationManagerCompat
        .from(context)
        .notify(1, builder.build())
}

@Composable
fun TouchApp(
    context: Context
) {

    val prefs = remember {

        context.getSharedPreferences(
            "machapeapp",
            Context.MODE_PRIVATE
        )
    }

    val myId = remember {

        val savedId =
            prefs.getString(
                "myId",
                null
            )

        if (savedId != null) {

            savedId

        } else {

            val newId =
                System.currentTimeMillis()
                    .toString()

            prefs.edit()
                .putString(
                    "myId",
                    newId
                )
                .apply()

            newId
        }
    }

    var mensaje by remember {
        mutableStateOf(
            "Esperando atención..."
        )
    }

    var validTouches by remember {
        mutableStateOf(0)
    }

    var activar by remember {
        mutableStateOf(true)
    }

    var roomCode by remember {

        mutableStateOf(
            prefs.getString(
                "roomCode",
                ""
            ) ?: ""
        )
    }

    var conectado by remember {
        mutableStateOf(false)
    }

    var currentImage by remember {
        mutableStateOf(
            R.drawable.me
        )
    }

    var llorando by remember {
        mutableStateOf(false)
    }

    var esperandoAtencion by remember {
        mutableStateOf(false)
    }

    var validarAtencion by remember {
        mutableStateOf(false)
    }

    val vibrator =
        context.getSystemService(
            Context.VIBRATOR_SERVICE
        ) as Vibrator

    LaunchedEffect(Unit) {

        conectado =
            roomCode.isNotEmpty()
    }

    // 🔥 ESCUCHAR FIREBASE
    LaunchedEffect(conectado) {

        if (conectado) {

            // 💜 CONTADOR
            FirebaseDatabase
                .getInstance()
                .getReference(
                    "counter/$roomCode"
                )
                .addValueEventListener(

                    object : ValueEventListener {

                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {

                            val value =
                                snapshot.getValue(
                                    Int::class.java
                                )

                            if (value != null) {

                                validTouches = value
                            }
                        }

                        override fun onCancelled(
                            error: DatabaseError
                        ) {}
                    }
                )

            // 🦝 ESTADO MAPACHE
            FirebaseDatabase
                .getInstance()
                .getReference(
                    "state/$roomCode"
                )
                .addValueEventListener(

                    object : ValueEventListener {

                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {

                            val state =
                                snapshot.getValue(
                                    String::class.java
                                )

                            when (state) {

                                "idle" -> {

                                    llorando = false
                                    esperandoAtencion = false

                                    currentImage =
                                        R.drawable.me

                                    mensaje =
                                        "Esperando atención..."
                                }

                                "crying" -> {

                                    llorando = true
                                    esperandoAtencion = true

                                    mensaje =
                                        "Tu machape necesita atención 🥺"
                                }

                                "happy" -> {

                                    llorando = false
                                    esperandoAtencion = false

                                    currentImage =
                                        R.drawable.feliz

                                    mensaje =
                                        "Atención validada 💜"
                                }
                            }
                        }

                        override fun onCancelled(
                            error: DatabaseError
                        ) {}
                    }
                )

            // 📨 EVENTOS
            FirebaseDatabase
                .getInstance()
                .getReference(
                    "touch/$roomCode"
                )
                .addValueEventListener(

                    object : ValueEventListener {

                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {

                            val sender =
                                snapshot.child(
                                    "sender"
                                ).getValue(
                                    String::class.java
                                )

                            val text =
                                snapshot.child(
                                    "text"
                                ).getValue(
                                    String::class.java
                                )

                            if (
                                sender != null &&
                                sender != myId &&
                                text != null
                            ) {

                                // 🥺 PEDIR
                                if (
                                    text ==
                                    "need_attention"
                                ) {

                                    validarAtencion = true

                                    if (activar) {

                                        if (
                                            Build.VERSION.SDK_INT >=
                                            Build.VERSION_CODES.O
                                        ) {

                                            vibrator.vibrate(
                                                VibrationEffect.createOneShot(
                                                    500,
                                                    VibrationEffect.DEFAULT_AMPLITUDE
                                                )
                                            )

                                        } else {

                                            vibrator.vibrate(
                                                500
                                            )
                                        }

                                        showNotification(
                                            context,
                                            "Tu machape necesita atención 🥺"
                                        )
                                    }
                                }

                                // 💜 VALIDADA
                                if (
                                    text ==
                                    "validated_attention"
                                ) {

                                    validarAtencion = false

                                    currentImage =
                                        R.drawable.feliz
                                }

                                // ❌ INVALIDADA
                                if (
                                    text ==
                                    "invalid_attention"
                                ) {

                                    validarAtencion = false

                                    currentImage =
                                        R.drawable.me
                                }
                            }
                        }

                        override fun onCancelled(
                            error: DatabaseError
                        ) {}
                    }
                )
        }
    }

    // 😭 ANIMACIÓN
    LaunchedEffect(llorando) {

        while (llorando) {

            currentImage =
                R.drawable.mt1

            delay(350)

            currentImage =
                R.drawable.mt2

            delay(350)
        }
    }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFE5EC),
                                Color(0xFFFFD6FF),
                                Color(0xFFE7C6FF)
                            )
                        )
                )
                .padding(padding)
                .padding(24.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Box(
                modifier =
                    Modifier.size(260.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .background(
                                brush =
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x66FFB6F9),
                                            Color.Transparent
                                        )
                                    )
                            )
                )

                Image(
                    painter =
                        painterResource(
                            id = currentImage
                        ),

                    contentDescription = null,

                    modifier =
                        Modifier.size(220.dp),

                    contentScale =
                        ContentScale.Fit
                )
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            if (!conectado) {

                OutlinedTextField(
                    value = roomCode,

                    onValueChange = {
                        roomCode = it
                    },

                    label = {

                        Text(
                            "Código de vínculo 💜"
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Button(
                    onClick = {

                        if (
                            roomCode.trim()
                                .isNotEmpty()
                        ) {

                            roomCode =
                                roomCode.trim()

                            prefs.edit()
                                .putString(
                                    "roomCode",
                                    roomCode
                                )
                                .apply()

                            conectado = true

                            mensaje =
                                "Conectados emocionalmente 💜"
                        }
                    }
                ) {

                    Text(
                        "Conectar 🦝"
                    )
                }

            } else {

                // 🥺 PEDIR
                Button(
                    onClick = {

                        FirebaseDatabase
                            .getInstance()
                            .getReference(
                                "state/$roomCode"
                            )
                            .setValue(
                                "crying"
                            )

                        val data =
                            mapOf(
                                "text" to
                                        "need_attention",

                                "sender" to
                                        myId
                            )

                        FirebaseDatabase
                            .getInstance()
                            .getReference(
                                "touch/$roomCode"
                            )
                            .setValue(data)

                        mensaje =
                            "Pediste atención 🥺"
                    },

                    modifier =
                        Modifier
                            .width(240.dp)
                            .height(58.dp)
                ) {

                    Text(
                        "Necesito atención 🥺"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                // 💜 VALIDAR
                if (validarAtencion) {

                    Button(
                        onClick = {

                            validarAtencion = false

                            FirebaseDatabase
                                .getInstance()
                                .getReference(
                                    "state/$roomCode"
                                )
                                .setValue(
                                    "happy"
                                )

                            val data =
                                mapOf(
                                    "text" to
                                            "validated_attention",

                                    "sender" to
                                            myId
                                )

                            FirebaseDatabase
                                .getInstance()
                                .getReference(
                                    "touch/$roomCode"
                                )
                                .setValue(data)

                            FirebaseDatabase
                                .getInstance()
                                .getReference(
                                    "counter/$roomCode"
                                )
                                .runTransaction(

                                    object : Transaction.Handler {

                                        override fun doTransaction(
                                            currentData: MutableData
                                        ): Transaction.Result {

                                            var value =
                                                currentData.getValue(
                                                    Int::class.java
                                                ) ?: 0

                                            value++

                                            if (value >= 30) {

                                                value = 0
                                            }

                                            currentData.value = value

                                            return Transaction.success(
                                                currentData
                                            )
                                        }

                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            snapshot: DataSnapshot?
                                        ) {

                                            validTouches =
                                                snapshot?.getValue(
                                                    Int::class.java
                                                ) ?: 0

                                            if (
                                                validTouches == 0
                                            ) {

                                                mensaje =
                                                    "🎁 Premio desbloqueado 💜"
                                            }
                                        }
                                    }
                                )

                            currentImage =
                                R.drawable.feliz
                        },

                        modifier =
                            Modifier
                                .width(240.dp)
                                .height(58.dp)
                    ) {

                        Text(
                            "Sí fue válida 💜"
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    OutlinedButton(
                        onClick = {

                            validarAtencion = false

                            FirebaseDatabase
                                .getInstance()
                                .getReference(
                                    "state/$roomCode"
                                )
                                .setValue(
                                    "idle"
                                )

                            val data =
                                mapOf(
                                    "text" to
                                            "invalid_attention",

                                    "sender" to
                                            myId
                                )

                            FirebaseDatabase
                                .getInstance()
                                .getReference(
                                    "touch/$roomCode"
                                )
                                .setValue(data)

                            currentImage =
                                R.drawable.me
                        },

                        modifier =
                            Modifier
                                .width(240.dp)
                                .height(58.dp)
                    ) {

                        Text(
                            "No 😅"
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text = mensaje
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                "💜 Atenciones válidas: $validTouches / 30"
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    "Sonido y vibración"
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Switch(
                    checked = activar,

                    onCheckedChange = {
                        activar = it
                    }
                )
            }
        }
    }
}