package com.example.pckeyboardmousecontroller

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.OutputStream
import java.net.Socket

object MouseClient {
    private var output: OutputStream? = null
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val moveChannel = Channel<Pair<Float, Float>>(Channel.CONFLATED)
    private val scrollChannel = Channel<Float>(Channel.CONFLATED)

    private var ipAddress: String = ""
    private var portNumber: Int = 5050
    private var isConnected = false

    fun connect(ip: String, port: Int = 5050) {
        ipAddress = ip
        portNumber = port

        scope.launch {
            while (true) {
                try {
                    if (!isConnected) {
                        Log.d("MouseClient", "Intentando conectar a $ip:$port...")
                        socket = Socket(ip, port)
                        output = socket?.getOutputStream()
                        isConnected = true
                        Log.d("MouseClient", "Conectado a $ip:$port")
                    }

                    // Start listeners solo si acaba de conectar
                    launch {
                        for ((dx, dy) in moveChannel) {
                            sendMoveInternal(dx, dy)
                        }
                    }

                    launch {
                        for (dy in scrollChannel) {
                            sendScrollInternal(dy)
                        }
                    }

                    // Ya conectado, salimos del ciclo de reconexión
                    break

                } catch (e: Exception) {
                    isConnected = false
                    Log.e("MouseClient", "Error al conectar: ${e.message}")
                    delay(3000) // Esperar antes de reintentar 3 segundos
                }
            }
        }
    }

    private fun resetConnection() {
        try {
            output?.close()
            socket?.close()
        } catch (_: Exception) {}
        output = null
        socket = null
        isConnected = false
        connect(ipAddress, portNumber) // reintenta conexión
    }

    private fun sendMoveInternal(dx: Float, dy: Float) {
        try {
            if (isConnected) {
                val json = """{"type":"move","dx":$dx,"dy":$dy}""" + "\n"
                output?.write(json.toByteArray())
                output?.flush()
                Log.d("MouseClient", "Enviado movimiento dx=$dx dy=$dy")
            }
        } catch (e: Exception) {
            Log.e("MouseClient", "Error enviando movimiento: ${e.message}")
            resetConnection()
        }
    }

    private fun sendScrollInternal(dy: Float) {
        try {
            if (isConnected) {
                val json = """{"type":"scroll","dy":$dy}""" + "\n"
                output?.write(json.toByteArray())
                output?.flush()
                Log.d("MouseClient", "Enviado scroll dy=$dy")
            }
        } catch (e: Exception) {
            Log.e("MouseClient", "Error enviando scroll: ${e.message}")
            resetConnection()
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
