package com.example.pckeyboardmousecontroller

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pckeyboardmousecontroller.ui.theme.PCKeyboardMouseControllerTheme

class area_pc_controller {
}


@Composable
fun TouchpadAreaWithDisplay(modifier: Modifier = Modifier) {
    var dx by remember { mutableStateOf(0f) }
    var dy by remember { mutableStateOf(0f) }
    var isDragMode by remember { mutableStateOf(false) }


    // Estado para las teclas especiales activas (ctrl, alt, shift, win)
    var activeSpecialKeys by remember { mutableStateOf(setOf<String>()) }

    Row(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxHeight()
            .width(30.dp)
            .background(Color.Gray)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    MouseClient.sendScroll(-dragAmount / 50f) // Ajustá el divisor según la sensibilidad
                }
            }
        )


        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        dx = dragAmount.x
                        dy = dragAmount.y
                        MouseClient.sendMove(dx, dy)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Aquí envías un click izquierdo cuando toquen en espacio vacío
                            sendKeyToPC(type = "click", key = "left")
                        }
                    )
                }
        ) {
            Row(modifier = Modifier.padding(5.dp)) {
                val actionText = if (isDragMode) "Arrastre" else "Click"

                Button(onClick = { sendKeyToPC(type = "click", key = "left", action = actionText ) }) {
                    Text("Click\nIzq", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(5.dp))

                Button(onClick = { sendKeyToPC(type = "click", key = "middle", action = actionText) }) {
                    Text("Click\nMed", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(5.dp))

                Button(onClick = { sendKeyToPC(type = "click", key = "right", action = actionText) }) {
                    Text("Click\nDer", fontSize = 10.sp)
                }


                Spacer(modifier = Modifier.width(5.dp))


                Button(onClick = {isDragMode = !isDragMode},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDragMode) Color.Red else Color.Green)) {
                    Text(if (isDragMode) "Arrastre" else "Click", fontSize = 10.sp)
                }
            }

            Row(modifier = Modifier.padding(horizontal = 1.dp, vertical = 8.dp)) {
                val keys = listOf(
                    Triple("Ctrl", "ctrl", "skey"),
                    Triple("Shift", "shift", "skey"),
                    Triple("Win", "win", "skey"),
                    Triple("Alt", "alt", "skey"),
                    Triple("Gr", "altgr", "skey"),
                )

                keys.forEach { (label, key, type) ->
                    val isActive = activeSpecialKeys.contains(key)
                    Button(
                        onClick = {
                            if (isActive) {
                                // Si estaba activo, lo desactivamos y enviamos release
                                activeSpecialKeys = activeSpecialKeys - key
                                sendKeyToPC(type = type, key = key, action = "release")
                            } else {
                                // Si estaba desactivado, lo activamos y enviamos press
                                activeSpecialKeys = activeSpecialKeys + key
                                sendKeyToPC(type = type, key = key, action = "press")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color.DarkGray else Color.LightGray
                        ),
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(label, fontSize = 10.sp)
                    }
                }
            }

            // Row in 2 columns, 1. Esc, Tab, Enter, 2. up, down, left, right
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column (modifier = Modifier.padding(1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { sendKeyToPC(type = "skey", key = "esc") }) {
                        Text("Esc")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { sendKeyToPC(type = "skey", key = "tab") }) {
                        Text("Tab")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { sendKeyToPC(type = "skey", key = "enter") }) {
                        Text("Enter")
                    }
                }


                Column (horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Button(onClick = { sendKeyToPC(type = "skey", key = "up") }) {
                            Text("↑")
                        }
                    }


                    Row() {
                        Button(onClick = { sendKeyToPC(type = "skey", key = "left") }) {
                            Text("←")
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Button(onClick = { sendKeyToPC(type = "skey", key = "down") }) {
                            Text("↓")
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Button(onClick = { sendKeyToPC(type = "skey", key = "right") }) {
                            Text("→")
                        }
                    }

                }

            }


            // Ahora el teclado normal
            Row(modifier = Modifier.padding(16.dp)) {
                KeyboardInputToPC()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TouchpadPreview() {
    PCKeyboardMouseControllerTheme {
        TouchpadAreaWithDisplay()
    }
}

@Composable
fun KeyboardInputToPC() {
    var text by remember { mutableStateOf("") }
    var lastSentText by remember { mutableStateOf("") }

    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = text,
            onValueChange = { newText ->
                if (newText.length < lastSentText.length) {
                    sendKeyToPC(type = "skey", key = "backspace")
                } else if (newText.length > lastSentText.length) {
                    val newChar = newText.last()
                    sendKeyToPC(type = "key", key = newChar.toString())
                }
                text = newText
                lastSentText = newText
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = {
            sendKeyToPC(type = "skey", key = "backspace")
            if (text.isNotEmpty()) {
                text = text.dropLast(1)
                lastSentText = text
            }
        }) {
            Text("⌫") // símbolo para backspace
        }
    }
}
