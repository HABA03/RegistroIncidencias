package com.example.registroincidencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.registroincidencias.model.Incidencia
import com.example.registroincidencias.ui.theme.RegistroIncidenciasTheme
import com.example.registroincidencias.ui.theme.SuccessGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroIncidenciasScreen() {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    val incidencias = remember { mutableStateListOf<Incidencia>() }

    val errorTitulo = stringResource(R.string.error_titulo_vacio)
    val errorDescripcion = stringResource(R.string.error_descripcion_vacia)
    val mensajeExito = stringResource(R.string.success_registrado)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.subtitle_screen),
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    mensaje = ""
                },
                label = { Text(stringResource(R.string.label_titulo)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    mensaje = ""
                },
                label = { Text(stringResource(R.string.label_descripcion)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    when {
                        titulo.isBlank() -> {
                            mensaje = errorTitulo
                            esError = true
                        }
                        descripcion.isBlank() -> {
                            mensaje = errorDescripcion
                            esError = true
                        }
                        else -> {
                            incidencias.add(0, Incidencia(titulo.trim(), descripcion.trim()))
                            mensaje = mensajeExito
                            esError = false
                            titulo = ""
                            descripcion = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_registrar))
            }

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    color = if (esError) MaterialTheme.colorScheme.error else SuccessGreen
                )
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.section_incidencias, incidencias.size),
                style = MaterialTheme.typography.titleMedium
            )

            if (incidencias.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_state),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    incidencias.forEach { incidencia ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = incidencia.titulo,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = incidencia.descripcion,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
