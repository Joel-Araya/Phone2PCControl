package com.example.pckeyboardmousecontroller

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pckeyboardmousecontroller.ui.theme.PCKeyboardMouseControllerTheme
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // Mantener la pantalla encendida
        enableEdgeToEdge() // Habilitar modo inmersivo



        MouseClient.connect(ip = "192.168.18.8", port = 5050) // Pon tu IP aquí

        setContent {
            PCKeyboardMouseControllerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TouchpadAreaWithDisplay(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


