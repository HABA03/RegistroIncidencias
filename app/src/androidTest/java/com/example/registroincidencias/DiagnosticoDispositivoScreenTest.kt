package com.example.registroincidencias

import android.hardware.SensorManager
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.registroincidencias.model.RecursoSensor
import com.example.registroincidencias.ui.diagnostico.DiagnosticoDispositivoScreen
import com.example.registroincidencias.ui.theme.RegistroIncidenciasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticoDispositivoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun sinSensoresMuestraMensajeClaro() {
        composeRule.setContent {
            RegistroIncidenciasTheme {
                DiagnosticoDispositivoScreen(sensorManager = null)
            }
        }

        val noDisponible = contexto.getString(R.string.sensor_no_disponible)
        RecursoSensor.entries.forEach { recurso ->
            composeRule.onNodeWithTag("estado_${recurso.name}").assertTextEquals(noDisponible)
        }
        composeRule.onNodeWithTag("resumen_disponibles")
            .assertTextEquals("Sensores disponibles: 0 de 3")
        composeRule.onNodeWithTag("estado_monitoreo")
            .assertTextEquals(contexto.getString(R.string.diagnostico_sin_sensores))
        composeRule.onNodeWithTag("boton_monitoreo").assertIsNotEnabled()
    }

    @Test
    fun pausarLiberaLosSensoresYReanudarLosVuelveARegistrar() {
        val sensorManager = contexto.getSystemService(SensorManager::class.java)
        val disponibles = RecursoSensor.entries.count { sensorManager.getDefaultSensor(it.tipo) != null }
        composeRule.setContent {
            RegistroIncidenciasTheme {
                DiagnosticoDispositivoScreen(sensorManager = sensorManager)
            }
        }

        composeRule.onNodeWithTag("resumen_disponibles")
            .assertTextEquals("Sensores disponibles: $disponibles de 3")
        if (disponibles == 0) return

        val escuchando = contexto.resources.getQuantityString(
            R.plurals.diagnostico_escuchando, disponibles, disponibles
        )
        composeRule.onNodeWithTag("estado_monitoreo").assertTextEquals(escuchando)

        composeRule.onNodeWithTag("boton_monitoreo").assertIsEnabled().performClick()
        composeRule.onNodeWithTag("estado_monitoreo")
            .assertTextEquals(contexto.getString(R.string.diagnostico_en_pausa))
        composeRule.onNodeWithText(contexto.getString(R.string.diagnostico_reanudar)).assertExists()

        composeRule.onNodeWithTag("boton_monitoreo").performClick()
        composeRule.onNodeWithTag("estado_monitoreo").assertTextEquals(escuchando)
    }

    @Test
    fun cambiarDePestanaConservaElFormulario() {
        composeRule.setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasApp()
            }
        }

        composeRule.onNodeWithTag("campo_titulo").performTextInput("Pantalla parpadea")
        composeRule.onNodeWithTag("tab_DIAGNOSTICO").performClick()
        composeRule.onNodeWithText(contexto.getString(R.string.diagnostico_monitoreo)).assertExists()

        composeRule.onNodeWithTag("tab_REGISTRO").performClick()
        composeRule.onNodeWithTag("campo_titulo").assertTextContains("Pantalla parpadea")
    }
}
