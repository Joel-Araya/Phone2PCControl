package com.example.pckeyboardmousecontroller

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.OutputStream
import java.net.Socket

sealed class MouseMessage {
    data class Move(val dx: Float, val dy: Float): MouseMessage()
    data class Click(val button: String): MouseMessage()
    data class Key(val key: String): MouseMessage()
}

object MouseClient {
    private var output: OutputStream? = null
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val moveChannel = Channel<Pair<Float, Float>>(Channel.CONFLATED)
    private val scrollChannel = Channel<Float>(Channel.CONFLATED) // NUEVO: canal para scroll

    fun connect(ip: String, port: Int = 5050) {
        scope.launch {
            try {
                socket = Socket(ip, port)
                output = socket?.getOutputStream()
                Log.d("MouseClient", "Conectado a $ip:$port")

                // Movimiento del mouse
                launch {
                    for ((dx, dy) in moveChannel) {
                        sendMoveInternal(dx, dy)
                    }
                }

                // Scroll del mouse
                launch {
                    for (dy in scrollChannel) {
                        sendScrollInternal(dy)
                    }
                }

            } catch (e: Exception) {
                Log.e("MouseClient", "Error al conectar: ${e.message}")
            }
        }
    }

    private fun sendMoveInternal(dx: Float, dy: Float) {
        try {
            val json = """{"type":"move","dx":$dx,"dy":$dy}""" + "\n"
            output?.write(json.toByteArray())
            output?.flush()
            Log.d("MouseClient", "Enviado movimiento dx=$dx dy=$dy")
        } catch (e: Exception) {
            Log.e("MouseClient", "Error enviando movimiento: ${e.message}")
        }
    }

    private fun sendScrollInternal(dy: Float) {
        try {
            val json = """{"type":"scroll","dy":$dy}""" + "\n"
            output?.write(json.toByteArray())
            output?.flush()
            Log.d("MouseClient", "Enviado scroll dy=$dy")
        } catch (e: Exception) {
            Log.e("MouseClient", "Error enviando scroll: ${e.message}")
        }
    }

    fun sendMove(dx: Float, dy: Float) {
        scope.launch {
            moveChannel.trySend(dx to dy).isSuccess
        }
    }

    fun sendScroll(dy: Float) {
        scope.launch {
            scrollChannel.trySend(dy).isSuccess
        }
    }
}
