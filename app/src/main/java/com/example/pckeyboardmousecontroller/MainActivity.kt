package com.example.pckeyboardmousecontroller

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        val context = applicationContext

        setContent {
            PCKeyboardMouseControllerTheme {
                var currentScreen by remember { mutableStateOf("menu") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "menu" -> ConnectionSelectionScreen(
                            modifier = Modifier.padding(innerPadding),
                            onManualClick = { currentScreen = "manual" },
                            onLastClick = {
                                if (connectToLastKnown(context)) {
                                    currentScreen = "control"
                                }
                            },
                            onConnectToServer = { ip, port ->
                                MouseClient.connect(ip, port)
                                PreferencesHelper.saveLastConnection(context, ip, port)
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
    onLastClick: () -> Unit,
    onConnectToServer: (String, Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var status by remember { mutableStateOf("Buscando...") }

        Button(
            onClick = {
                status = "Buscando..."
                coroutineScope.launch {
                    val found = discoverServersUDP()
                    if (found.isNotEmpty()) {
                        val (ip, port) = found.first()
                        onConnectToServer(ip, port)
                    } else {
                        status = "No se encontró ningún servidor."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar Servidor")
        }

        Text(
            text = status,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )


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
    val context = LocalContext.current


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
                val portInt = port.toIntOrNull() ?: 5050
                MouseClient.connect(ip, portInt)
                PreferencesHelper.saveLastConnection(context, ip, portInt)
                onConnect()
            }
        ) {
            Text("Conectar")
        }


    }
}


fun connectToLastKnown(context: Context): Boolean {
    val last = PreferencesHelper.getLastConnection(context)
    return if (last != null) {
        val (savedIp, savedPort) = last
        MouseClient.connect(savedIp, savedPort)
        true
    } else {
        false
    }
}
