package com.example.registroincidencias

import android.hardware.SensorManager
import android.os.Process
import android.os.SystemClock
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.registroincidencias.model.RecursoSensor
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica que los listeners se liberan cuando la Activity deja el primer plano (ON_PAUSE)
 * y se vuelven a registrar al regresar (ON_RESUME), sin intervención de la persona usuaria.
 */
@RunWith(AndroidJUnit4::class)
class CicloVidaSensoresTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun segundoPlanoLiberaYPrimerPlanoVuelveARegistrar() {
        val actividad = composeRule.activity
        val sensorManager = actividad.getSystemService(SensorManager::class.java)
        val disponibles = RecursoSensor.entries.count { sensorManager.getDefaultSensor(it.tipo) != null }
        assumeTrue("El dispositivo no tiene sensores que revisar", disponibles > 0)

        val escuchando = actividad.resources.getQuantityString(
            R.plurals.diagnostico_escuchando, disponibles, disponibles
        )
        composeRule.onNodeWithTag("tab_DIAGNOSTICO").performClick()
        composeRule.onNodeWithTag("estado_monitoreo").assertTextEquals(escuchando)

        // ON_PAUSE: la app deja el primer plano y cada listener debe liberarse. Con la Activity
        // en pausa Compose Test no puede leer la jerarquía, así que se verifica con el registro
        // que deja LecturaSensor en Logcat.
        val liberadosAntes = contarLiberaciones()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        assertEquals(disponibles, esperarLiberaciones(liberadosAntes + disponibles) - liberadosAntes)

        // ON_RESUME: al volver se registran de nuevo.
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.onNodeWithTag("estado_monitoreo").assertTextEquals(escuchando)

        // Al cambiar de pestaña el efecto sale de la composición y también libera el recurso.
        composeRule.onNodeWithTag("tab_REGISTRO").performClick()
        composeRule.onNodeWithTag("tab_DIAGNOSTICO").performClick()
        composeRule.onNodeWithTag("estado_monitoreo").assertTextEquals(escuchando)
    }

    private fun contarLiberaciones(): Int {
        val comando = arrayOf(
            "logcat", "-d", "--pid=${Process.myPid()}", "-s", "DiagnosticoSensores:D"
        )
        return Runtime.getRuntime().exec(comando).inputStream.bufferedReader()
            .useLines { lineas -> lineas.count { "Listener liberado" in it } }
    }

    private fun esperarLiberaciones(esperadas: Int, tiempoMaximoMs: Long = 5_000): Int {
        val limite = SystemClock.uptimeMillis() + tiempoMaximoMs
        var actuales = contarLiberaciones()
        while (actuales < esperadas && SystemClock.uptimeMillis() < limite) {
            SystemClock.sleep(100)
            actuales = contarLiberaciones()
        }
        return actuales
    }
}
