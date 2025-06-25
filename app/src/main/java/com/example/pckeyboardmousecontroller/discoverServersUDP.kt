package com.example.pckeyboardmousecontroller

import android.util.Log
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

suspend fun discoverServersUDP(
    broadcastPort: Int = 5051,
    timeoutMs: Int = 3000
): List<Pair<String, Int>> {
    val foundServers = mutableListOf<Pair<String, Int>>()

    withContext(Dispatchers.IO) {
        val socket = DatagramSocket()
        socket.broadcast = true
        socket.soTimeout = timeoutMs

        val sendData = "DISCOVER_REQUEST".toByteArray()
        val broadcastAddress = InetAddress.getByName("255.255.255.255")

        val sendPacket = DatagramPacket(sendData, sendData.size, broadcastAddress, broadcastPort)
        socket.send(sendPacket)

        val buffer = ByteArray(1500)
        val receivePacket = DatagramPacket(buffer, buffer.size)

        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                socket.receive(receivePacket)
                val message = String(receivePacket.data, 0, receivePacket.length)

                // Asumiendo que el servidor responde con: "DISCOVER_RESPONSE:<ip>:<port>"
                if (message.startsWith("DISCOVER_RESPONSE:")) {
                    val parts = message.split(":")
                    val ip = parts[1]
                    val port = parts[2].toIntOrNull() ?: 5050

                    //Hacemos un log de la respuesta recibida
                    Log.d("UDP Discovery", "Servidor encontrado: $ip:$port")
                    // Agregamos el servidor encontrado a la lista


                    if (parts.size >= 3) {
                        val ip = parts[1]
                        val port = parts[2].toIntOrNull() ?: 5050
                        foundServers.add(ip to port)
                    }
                }
            } catch (e: SocketTimeoutException) {
                // Timeout: no más respuestas, salimos
                break
            }
        }

        socket.close()
    }

    return foundServers
}
