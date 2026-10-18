package com.example.registroincidencias.sensores

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect

private const val TAG = "DiagnosticoSensores"

/** Estado observable de un sensor: última lectura, si el listener está activo y cuántos eventos llegaron. */
@Stable
class LecturaSensorState {
    var valores by mutableStateOf<List<Float>?>(null)
        internal set
    var escuchando by mutableStateOf(false)
        internal set
    var eventosRecibidos by mutableIntStateOf(0)
        internal set
}

/**
 * Escucha [sensor] solo mientras [activo] sea verdadero y la pantalla esté en primer plano.
 *
 * [LifecycleResumeEffect] registra el listener en ON_RESUME y lo libera en ON_PAUSE o cuando
 * el efecto sale de la composición (la persona pausa el monitoreo o cambia de pestaña). Así el
 * sensor nunca queda escuchando en segundo plano ni consumiendo batería.
 */
@Composable
fun rememberLecturaSensor(
    sensorManager: SensorManager?,
    sensor: Sensor?,
    activo: Boolean
): LecturaSensorState {
    val estado = remember(sensor) { LecturaSensorState() }

    if (sensorManager != null && sensor != null && activo) {
        LifecycleResumeEffect(sensorManager, sensor) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    estado.valores = event.values.toList()
                    estado.eventosRecibidos++
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }

            val registrado = sensorManager.registerListener(
                listener,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            estado.escuchando = registrado
            Log.d(TAG, "Listener registrado (${sensor.name}): $registrado")

            onPauseOrDispose {
                sensorManager.unregisterListener(listener)
                estado.escuchando = false
                Log.d(TAG, "Listener liberado (${sensor.name})")
            }
        }
    }

    return estado
}
