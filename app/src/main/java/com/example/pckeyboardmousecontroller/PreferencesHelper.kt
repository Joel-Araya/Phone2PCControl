package com.example.pckeyboardmousecontroller

import android.content.Context

object PreferencesHelper {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_IP = "last_ip"
    private const val KEY_PORT = "last_port"

    fun saveLastConnection(context: Context, ip: String, port: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_IP, ip)
            putInt(KEY_PORT, port)
            apply()
        }
    }

    fun getLastConnection(context: Context): Pair<String, Int>? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val ip = prefs.getString(KEY_IP, null)
        val port = prefs.getInt(KEY_PORT, -1)
        return if (ip != null && port != -1) Pair(ip, port) else null
    }
}
