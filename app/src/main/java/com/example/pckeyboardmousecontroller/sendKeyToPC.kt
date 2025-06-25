package com.example.pckeyboardmousecontroller

import kotlinx.coroutines.*
import java.net.Socket

fun sendKeyToPC(ip: String = "192.168.18.8", port: Int = 5050, key: String, type: String, action: String? = null) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = Socket(ip, port)
            val out = socket.getOutputStream()
            val json = """{"type":"$type","key":"$key", "action":"$action"}""" + "\n" // Aseguramos que el mensaje termine con un salto de línea
            out.write(json.toByteArray())
            out.flush()
            socket.close()
            println("✅ Mensaje enviado al servidor: $json")
        } catch (e: Exception) {
            println("❌ Error al conectar: ${e.message}")
        }
    }
}
