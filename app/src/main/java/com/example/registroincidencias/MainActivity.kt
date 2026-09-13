package com.example.registroincidencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RegistroIncidencias()
        }
    }
}

@Composable
fun RegistroIncidencias() {

    var descripcion by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Registro de Incidencias",
            fontSize = 28.sp
        )

        Text(
            text = "Describe la incidencia que deseas registrar."
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                mensaje = ""
            },
            label = {
                Text("Descripción de la incidencia")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensaje = if (descripcion.isNotBlank()) {
                    "Incidencia registrada correctamente."
                } else {
                    "Ingresa una descripción antes de registrar."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar incidencia")
        }

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                fontSize = 16.sp
            )
        }
    }
}