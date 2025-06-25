package com.example.pckeyboardmousecontroller

import android.content.Context

object PreferencesHelper {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_IP = "last_ip"
    private const val KEY_PORT = "last_port"

    // Guarda la última IP y puerto usados
    fun saveLastConnection(context: Context, ip: String, port: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_IP, ip)
            putInt(KEY_PORT, port)
            apply()
        }
    }

    // Devuelve la última IP y puerto, o null si no existen
    fun getLastConnection(context: Context): Pair<String, Int>? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val ip = prefs.getString(KEY_IP, null)
        val port = prefs.getInt(KEY_PORT, -1)
        return if (ip != null && port != -1) Pair(ip, port) else null
    }

    // Opcional: obtener IP o puerto por separado si lo preferís
    fun getLastIP(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_IP, null)
    }

    fun getLastPort(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PORT, -1)
    }
}
