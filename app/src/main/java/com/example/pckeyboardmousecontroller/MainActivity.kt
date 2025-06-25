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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            PCKeyboardMouseControllerTheme {
                var currentScreen by remember { mutableStateOf("menu") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "menu" -> ConnectionSelectionScreen(
                            modifier = Modifier.padding(innerPadding),
                            onManualClick = { currentScreen = "manual" },
                            onLastClick = {
                                MouseClient.connect("192.168.18.8", 5050)
                                currentScreen = "control"
                            }
                        )
                        "manual" -> ManualConnectionScreen {
                            currentScreen = "control"
                        }
                        "control" -> TouchpadAreaWithDisplay(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}


@Composable
fun ConnectionSelectionScreen(
    modifier: Modifier = Modifier,
    onManualClick: () -> Unit,
    onLastClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { /* luego */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Buscar Servidor")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onManualClick, modifier = Modifier.fillMaxWidth()) {
            Text("Conexión Manual")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLastClick, modifier = Modifier.fillMaxWidth()) {
            Text("Última Conexión")
        }
    }
}




@Composable
fun ManualConnectionScreen(onConnect: () -> Unit) {
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("5050") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            label = { Text("IP del servidor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Puerto") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                MouseClient.connect(ip, port.toIntOrNull() ?: 5050)
                onConnect()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Conectar")
        }
    }
}
