package com.example.registroincidencias.sensores

import androidx.annotation.StringRes
import com.example.registroincidencias.R
import com.example.registroincidencias.model.RecursoSensor
import kotlin.math.abs
import kotlin.math.sqrt

/** Gravedad estándar (m/s²); equivale a SensorManager.GRAVITY_EARTH. */
const val GRAVEDAD_TERRESTRE = 9.80665f

/** Desviación respecto a la gravedad por debajo de la cual el equipo se considera en reposo. */
const val UMBRAL_REPOSO = 0.6f

/** Desviación a partir de la cual el movimiento se considera brusco (sacudida o golpe). */
const val UMBRAL_MOVIMIENTO_FUERTE = 3.0f

/** Distancia máxima (cm) a la que un objeto se considera "cerca" del sensor de proximidad. */
const val DISTANCIA_CERCANA_CM = 5f

enum class NivelLectura { NORMAL, INFORMATIVO, ATENCION }

/** Frase legible para la persona usuaria y nivel visual con el que se muestra. */
data class Interpretacion(
    @StringRes val mensaje: Int,
    val nivel: NivelLectura
)

/**
 * Traduce los valores crudos de cada sensor a una frase con contexto, de modo que la pantalla
 * nunca muestre números aislados. No depende del framework de Android, por lo que se prueba
 * con pruebas unitarias locales.
 */
object InterpretacionSensor {

    fun interpretar(recurso: RecursoSensor, valores: List<Float>, rangoMaximo: Float): Interpretacion =
        when (recurso) {
            RecursoSensor.ACELEROMETRO -> aceleracion(valores[0], valores[1], valores[2])
            RecursoSensor.LUZ -> luz(valores[0])
            RecursoSensor.PROXIMIDAD -> proximidad(valores[0], rangoMaximo)
        }

    /**
     * En reposo el acelerómetro solo mide la gravedad (≈ 9.8 m/s²) repartida entre los tres ejes,
     * así que el movimiento se detecta por cuánto se aleja la magnitud del vector de ese valor.
     */
    fun aceleracion(x: Float, y: Float, z: Float): Interpretacion {
        val desviacion = abs(magnitud(x, y, z) - GRAVEDAD_TERRESTRE)
        return when {
            desviacion < UMBRAL_REPOSO ->
                Interpretacion(R.string.interpretacion_reposo, NivelLectura.NORMAL)
            desviacion < UMBRAL_MOVIMIENTO_FUERTE ->
                Interpretacion(R.string.interpretacion_movimiento_leve, NivelLectura.INFORMATIVO)
            else ->
                Interpretacion(R.string.interpretacion_movimiento_fuerte, NivelLectura.ATENCION)
        }
    }

    fun magnitud(x: Float, y: Float, z: Float): Float = sqrt(x * x + y * y + z * z)

    /** Rangos aproximados de iluminancia (lx) para ambientes cotidianos. */
    fun luz(lux: Float): Interpretacion = when {
        lux < 10f -> Interpretacion(R.string.interpretacion_luz_oscuro, NivelLectura.ATENCION)
        lux < 200f -> Interpretacion(R.string.interpretacion_luz_tenue, NivelLectura.INFORMATIVO)
        lux < 1_000f -> Interpretacion(R.string.interpretacion_luz_interior, NivelLectura.NORMAL)
        lux < 10_000f -> Interpretacion(R.string.interpretacion_luz_mucha, NivelLectura.INFORMATIVO)
        else -> Interpretacion(R.string.interpretacion_luz_solar, NivelLectura.ATENCION)
    }

    /**
     * Muchos sensores de proximidad son binarios: reportan 0 (cerca) o su rango máximo (lejos).
     * Se considera "cerca" cualquier valor por debajo del rango máximo y de [DISTANCIA_CERCANA_CM].
     */
    fun proximidad(distanciaCm: Float, rangoMaximo: Float): Interpretacion {
        val limite = if (rangoMaximo > 0f) minOf(rangoMaximo, DISTANCIA_CERCANA_CM) else DISTANCIA_CERCANA_CM
        return if (distanciaCm < limite) {
            Interpretacion(R.string.interpretacion_proximidad_cerca, NivelLectura.INFORMATIVO)
        } else {
            Interpretacion(R.string.interpretacion_proximidad_lejos, NivelLectura.NORMAL)
        }
    }
}
