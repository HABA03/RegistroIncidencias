package com.example.registroincidencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.registroincidencias.model.Incidencia
import com.example.registroincidencias.model.Prioridad
import com.example.registroincidencias.ui.components.EtiquetaPrioridad
import com.example.registroincidencias.ui.components.PrioridadSelector
import com.example.registroincidencias.ui.diagnostico.DiagnosticoDispositivoScreen
import com.example.registroincidencias.ui.theme.RegistroIncidenciasTheme
import com.example.registroincidencias.ui.theme.SuccessGreen

private const val MAX_CARACTERES_TITULO = 60

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasApp()
            }
        }
    }
}

enum class Seccion(@StringRes val pestana: Int, @StringRes val titulo: Int) {
    REGISTRO(R.string.tab_registro, R.string.app_name),
    DIAGNOSTICO(R.string.tab_diagnostico, R.string.diagnostico_titulo)
}

/**
 * Estructura principal: barra superior con pestañas para alternar entre el registro de
 * incidencias y el diagnóstico del dispositivo. El [rememberSaveableStateHolder] conserva el
 * formulario y la lista mientras la persona consulta el diagnóstico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroIncidenciasApp() {
    var seccion by rememberSaveable { mutableStateOf(Seccion.REGISTRO) }
    val estadoSecciones = rememberSaveableStateHolder()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(stringResource(seccion.titulo)) })
                PrimaryTabRow(selectedTabIndex = seccion.ordinal) {
                    Seccion.entries.forEach { opcion ->
                        Tab(
                            selected = opcion == seccion,
                            onClick = { seccion = opcion },
                            text = { Text(stringResource(opcion.pestana)) },
                            modifier = Modifier.testTag("tab_${opcion.name}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        estadoSecciones.SaveableStateProvider(seccion.name) {
            when (seccion) {
                Seccion.REGISTRO -> RegistroIncidenciasScreen(modifier)
                Seccion.DIAGNOSTICO -> DiagnosticoDispositivoScreen(modifier)
            }
        }
    }
}

private val IncidenciasSaver = listSaver<SnapshotStateList<Incidencia>, String>(
    save = { lista -> lista.flatMap { listOf(it.titulo, it.descripcion, it.prioridad.name) } },
    restore = { datos ->
        datos.chunked(3)
            .map { (titulo, descripcion, prioridad) ->
                Incidencia(titulo, descripcion, Prioridad.valueOf(prioridad))
            }
            .toMutableStateList()
    }
)

@Composable
fun RegistroIncidenciasScreen(modifier: Modifier = Modifier) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var prioridad by rememberSaveable { mutableStateOf<Prioridad?>(null) }
    var mensaje by rememberSaveable { mutableStateOf("") }
    var esError by rememberSaveable { mutableStateOf(false) }
    val incidencias = rememberSaveable(saver = IncidenciasSaver) { SnapshotStateList() }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val errorTitulo = stringResource(R.string.error_titulo_vacio)
    val errorDescripcion = stringResource(R.string.error_descripcion_vacia)
    val errorPrioridad = stringResource(R.string.error_prioridad_vacia)
    val mensajeExito = prioridad?.let {
        stringResource(R.string.success_registrado, stringResource(it.etiqueta))
    }.orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.subtitle_screen),
            style = MaterialTheme.typography.bodyMedium
        )

        // Título: texto corto en lenguaje natural. Se capitaliza la primera letra de cada
        // oración y la acción IME "Siguiente" lleva el foco a la descripción.
        OutlinedTextField(
            value = titulo,
            onValueChange = {
                if (it.length <= MAX_CARACTERES_TITULO) {
                    titulo = it
                    mensaje = ""
                }
            },
            label = { Text(stringResource(R.string.label_titulo)) },
            placeholder = { Text(stringResource(R.string.placeholder_titulo)) },
            supportingText = {
                Text(
                    text = stringResource(
                        R.string.contador_caracteres,
                        titulo.length,
                        MAX_CARACTERES_TITULO
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("campo_titulo")
        )

        // Descripción: último campo de texto del formulario. La acción IME "Listo" cierra
        // el teclado para dejar a la vista el selector de prioridad y el botón principal.
        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                mensaje = ""
            },
            label = { Text(stringResource(R.string.label_descripcion)) },
            placeholder = { Text(stringResource(R.string.placeholder_descripcion)) },
            minLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("campo_descripcion")
        )

        PrioridadSelector(
            seleccionada = prioridad,
            onSeleccionar = {
                prioridad = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val seleccionada = prioridad
                when {
                    titulo.isBlank() -> {
                        mensaje = errorTitulo
                        esError = true
                    }
                    descripcion.isBlank() -> {
                        mensaje = errorDescripcion
                        esError = true
                    }
                    seleccionada == null -> {
                        mensaje = errorPrioridad
                        esError = true
                    }
                    else -> {
                        incidencias.add(
                            0,
                            Incidencia(titulo.trim(), descripcion.trim(), seleccionada)
                        )
                        mensaje = mensajeExito
                        esError = false
                        titulo = ""
                        descripcion = ""
                        prioridad = null
                        focusManager.clearFocus()
                    }
                }
                haptic.performHapticFeedback(
                    if (esError) HapticFeedbackType.Reject else HapticFeedbackType.Confirm
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("boton_registrar")
        ) {
            Text(stringResource(R.string.button_registrar))
        }

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (esError) MaterialTheme.colorScheme.error else SuccessGreen,
                modifier = Modifier.testTag("mensaje_estado")
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
                    IncidenciaCard(incidencia)
                }
            }
        }
    }
}

@Composable
private fun IncidenciaCard(incidencia: Incidencia) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = incidencia.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaPrioridad(incidencia.prioridad)
            }
            Text(
                text = incidencia.descripcion,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
