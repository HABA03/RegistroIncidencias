package com.example.registroincidencias.sensores

import com.example.registroincidencias.R
import com.example.registroincidencias.model.RecursoSensor
import org.junit.Assert.assertEquals
import org.junit.Test

class InterpretacionSensorTest {

    @Test
    fun aceleracionSoloGravedadEsReposo() {
        // Lectura por defecto del emulador con el equipo en vertical.
        val resultado = InterpretacionSensor.aceleracion(0f, 9.77622f, 0.812345f)

        assertEquals(R.string.interpretacion_reposo, resultado.mensaje)
        assertEquals(NivelLectura.NORMAL, resultado.nivel)
    }

    @Test
    fun aceleracionEquipoBocaArribaTambienEsReposo() {
        val resultado = InterpretacionSensor.aceleracion(0f, 0f, GRAVEDAD_TERRESTRE)

        assertEquals(R.string.interpretacion_reposo, resultado.mensaje)
    }

    @Test
    fun aceleracionModeradaEsMovimientoLeve() {
        val resultado = InterpretacionSensor.aceleracion(1.5f, 10.5f, 0f)

        assertEquals(R.string.interpretacion_movimiento_leve, resultado.mensaje)
        assertEquals(NivelLectura.INFORMATIVO, resultado.nivel)
    }

    @Test
    fun aceleracionAltaEsMovimientoBrusco() {
        val resultado = InterpretacionSensor.aceleracion(8f, 12f, 4f)

        assertEquals(R.string.interpretacion_movimiento_fuerte, resultado.mensaje)
        assertEquals(NivelLectura.ATENCION, resultado.nivel)
    }

    @Test
    fun caidaLibreTambienEsMovimientoBrusco() {
        // En caída libre el acelerómetro marca ~0 m/s²: se aleja de la gravedad igual que un golpe.
        val resultado = InterpretacionSensor.aceleracion(0f, 0f, 0f)

        assertEquals(R.string.interpretacion_movimiento_fuerte, resultado.mensaje)
    }

    @Test
    fun luzSeClasificaPorRangos() {
        assertEquals(R.string.interpretacion_luz_oscuro, InterpretacionSensor.luz(0f).mensaje)
        assertEquals(R.string.interpretacion_luz_tenue, InterpretacionSensor.luz(50f).mensaje)
        assertEquals(R.string.interpretacion_luz_interior, InterpretacionSensor.luz(400f).mensaje)
        assertEquals(R.string.interpretacion_luz_mucha, InterpretacionSensor.luz(5_000f).mensaje)
        assertEquals(R.string.interpretacion_luz_solar, InterpretacionSensor.luz(40_000f).mensaje)
    }

    @Test
    fun luzEnLimitesPasaAlSiguienteRango() {
        assertEquals(R.string.interpretacion_luz_tenue, InterpretacionSensor.luz(10f).mensaje)
        assertEquals(R.string.interpretacion_luz_mucha, InterpretacionSensor.luz(1_000f).mensaje)
    }

    @Test
    fun proximidadBinariaCercaYLejos() {
        // Sensor binario típico: 0 cm (cerca) o su rango máximo (lejos).
        assertEquals(
            R.string.interpretacion_proximidad_cerca,
            InterpretacionSensor.proximidad(0f, rangoMaximo = 5f).mensaje
        )
        assertEquals(
            R.string.interpretacion_proximidad_lejos,
            InterpretacionSensor.proximidad(5f, rangoMaximo = 5f).mensaje
        )
    }

    @Test
    fun proximidadConRangoAmplioUsaDistanciaCercana() {
        assertEquals(
            R.string.interpretacion_proximidad_cerca,
            InterpretacionSensor.proximidad(3f, rangoMaximo = 10f).mensaje
        )
        assertEquals(
            R.string.interpretacion_proximidad_lejos,
            InterpretacionSensor.proximidad(8f, rangoMaximo = 10f).mensaje
        )
    }

    @Test
    fun interpretarDelegaSegunElRecurso() {
        assertEquals(
            R.string.interpretacion_luz_interior,
            InterpretacionSensor.interpretar(RecursoSensor.LUZ, listOf(300f), 40_000f).mensaje
        )
        assertEquals(
            R.string.interpretacion_reposo,
            InterpretacionSensor.interpretar(
                RecursoSensor.ACELEROMETRO,
                listOf(0f, GRAVEDAD_TERRESTRE, 0f),
                39.2f
            ).mensaje
        )
    }
}
