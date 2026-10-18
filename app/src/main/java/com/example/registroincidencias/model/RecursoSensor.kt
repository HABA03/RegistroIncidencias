package com.example.registroincidencias.model

import android.hardware.Sensor
import androidx.annotation.StringRes
import com.example.registroincidencias.R

/**
 * Sensores que revisa la pantalla de diagnóstico. [tipo] es la constante de [Sensor] que se
 * solicita a SensorManager y [unidad] la unidad en la que el sensor entrega sus valores.
 */
enum class RecursoSensor(
    val tipo: Int,
    @StringRes val nombre: Int,
    @StringRes val proposito: Int,
    @StringRes val unidad: Int
) {
    ACELEROMETRO(
        Sensor.TYPE_ACCELEROMETER,
        R.string.sensor_acelerometro,
        R.string.sensor_acelerometro_proposito,
        R.string.unidad_aceleracion
    ),
    LUZ(
        Sensor.TYPE_LIGHT,
        R.string.sensor_luz,
        R.string.sensor_luz_proposito,
        R.string.unidad_luz
    ),
    PROXIMIDAD(
        Sensor.TYPE_PROXIMITY,
        R.string.sensor_proximidad,
        R.string.sensor_proximidad_proposito,
        R.string.unidad_distancia
    )
}
