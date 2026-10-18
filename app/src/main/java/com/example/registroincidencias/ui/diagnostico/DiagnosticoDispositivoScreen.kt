package com.example.registroincidencias.ui.diagnostico

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.registroincidencias.R
import com.example.registroincidencias.model.RecursoSensor
import com.example.registroincidencias.sensores.InterpretacionSensor
import com.example.registroincidencias.sensores.LecturaSensorState
import com.example.registroincidencias.sensores.NivelLectura
import com.example.registroincidencias.sensores.rememberLecturaSensor
import com.example.registroincidencias.ui.theme.LecturaAtencion
import com.example.registroincidencias.ui.theme.LecturaNormal

/**
 * Diagnóstico del dispositivo: comprueba qué sensores existen, los escucha mientras la pantalla
 * está visible y el monitoreo activo, e interpreta cada lectura en lenguaje natural.
 *
 * [sensorManager] se recibe como parámetro para poder simular en pruebas un equipo sin sensores.
 */
@Composable
fun DiagnosticoDispositivoScreen(
    modifier: Modifier = Modifier,
    sensorManager: SensorManager? = LocalContext.current.getSystemService(SensorManager::class.java)
) {
    var monitoreoActivo by rememberSaveable { mutableStateOf(true) }

    val sensores = remember(sensorManager) {
        RecursoSensor.entries.associateWith { sensorManager?.getDefaultSensor(it.tipo) }
    }
    val lecturas = RecursoSensor.entries.associateWith { recurso ->
        key(recurso) { rememberLecturaSensor(sensorManager, sensores[recurso], monitoreoActivo) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.diagnostico_subtitulo),
            style = MaterialTheme.typography.bodyMedium
        )

        PanelMonitoreo(
            activo = monitoreoActivo,
            disponibles = sensores.values.count { it != null },
            escuchando = lecturas.values.count { it.escuchando },
            onAlternar = { monitoreoActivo = !monitoreoActivo }
        )

        RecursoSensor.entries.forEach { recurso ->
            TarjetaSensor(
                recurso = recurso,
                sensor = sensores[recurso],
                lectura = lecturas.getValue(recurso),
                monitoreoActivo = monitoreoActivo
            )
        }
    }
}

@Composable
private fun PanelMonitoreo(
    activo: Boolean,
    disponibles: Int,
    escuchando: Int,
    onAlternar: () -> Unit
) {
    val total = RecursoSensor.entries.size
    val hayEscucha = escuchando > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.diagnostico_monitoreo),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.diagnostico_disponibles, disponibles, total),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("resumen_disponibles")
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Indicador(if (hayEscucha) LecturaNormal else MaterialTheme.colorScheme.outline)
                Text(
                    text = when {
                        disponibles == 0 -> stringResource(R.string.diagnostico_sin_sensores)
                        hayEscucha -> pluralStringResource(
                            R.plurals.diagnostico_escuchando,
                            escuchando,
                            escuchando
                        )
                        activo -> stringResource(R.string.diagnostico_en_espera)
                        else -> stringResource(R.string.diagnostico_en_pausa)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("estado_monitoreo")
                )
            }
            Text(
                text = stringResource(R.string.diagnostico_nota_ciclo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            val modificadorBoton = Modifier
                .fillMaxWidth()
                .testTag("boton_monitoreo")
            if (activo) {
                FilledTonalButton(
                    onClick = onAlternar,
                    enabled = disponibles > 0,
                    modifier = modificadorBoton
                ) {
                    Text(stringResource(R.string.diagnostico_pausar))
                }
            } else {
                Button(
                    onClick = onAlternar,
                    enabled = disponibles > 0,
                    modifier = modificadorBoton
                ) {
                    Text(stringResource(R.string.diagnostico_reanudar))
                }
            }
        }
    }
}

@Composable
private fun TarjetaSensor(
    recurso: RecursoSensor,
    sensor: Sensor?,
    lectura: LecturaSensorState,
    monitoreoActivo: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sensor_${recurso.name}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(recurso.nombre),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaDisponibilidad(disponible = sensor != null)
            }
            Text(
                text = stringResource(recurso.proposito),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            if (sensor == null) {
                Text(
                    text = stringResource(R.string.sensor_no_disponible),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("estado_${recurso.name}")
                )
            } else {
                ContenidoLectura(recurso, sensor, lectura, monitoreoActivo)
            }
        }
    }
}

@Composable
private fun ContenidoLectura(
    recurso: RecursoSensor,
    sensor: Sensor,
    lectura: LecturaSensorState,
    monitoreoActivo: Boolean
) {
    val valores = lectura.valores
    val unidad = stringResource(recurso.unidad)

    if (valores == null) {
        Text(
            text = stringResource(
                if (monitoreoActivo) R.string.lectura_esperando else R.string.lectura_sin_datos_pausa
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("estado_${recurso.name}")
        )
    } else {
        val interpretacion = InterpretacionSensor.interpretar(recurso, valores, sensor.maximumRange)
        val color = interpretacion.nivel.color()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Indicador(color)
            Text(
                text = stringResource(interpretacion.mensaje),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.testTag("estado_${recurso.name}")
            )
        }
        Text(
            text = when (recurso) {
                RecursoSensor.ACELEROMETRO -> stringResource(
                    R.string.valor_aceleracion,
                    valores[0], valores[1], valores[2], unidad,
                    InterpretacionSensor.magnitud(valores[0], valores[1], valores[2])
                )
                RecursoSensor.LUZ -> stringResource(R.string.valor_luz, valores[0], unidad)
                RecursoSensor.PROXIMIDAD -> stringResource(
                    R.string.valor_proximidad, valores[0], unidad, sensor.maximumRange
                )
            },
            style = MaterialTheme.typography.bodyMedium
        )
        if (!monitoreoActivo) {
            Text(
                text = stringResource(R.string.lectura_ultima_antes_pausa),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Text(
        text = pluralStringResource(
            R.plurals.sensor_detalle_tecnico,
            lectura.eventosRecibidos,
            sensor.name,
            lectura.eventosRecibidos
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EtiquetaDisponibilidad(disponible: Boolean) {
    val color = if (disponible) LecturaNormal else MaterialTheme.colorScheme.error
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f),
        contentColor = color
    ) {
        Text(
            text = stringResource(
                if (disponible) R.string.sensor_disponible else R.string.sensor_no_disponible_corto
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun Indicador(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun NivelLectura.color(): Color = when (this) {
    NivelLectura.NORMAL -> LecturaNormal
    NivelLectura.INFORMATIVO -> MaterialTheme.colorScheme.primary
    NivelLectura.ATENCION -> LecturaAtencion
}
