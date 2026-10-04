package com.example.registroincidencias

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.registroincidencias.ui.theme.RegistroIncidenciasTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistroIncidenciasScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        composeRule.setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasScreen()
            }
        }
    }

    @Test
    fun accionesImeCoherentes() {
        composeRule.onNodeWithTag("campo_titulo").assert(hasImeAction(ImeAction.Next))
        composeRule.onNodeWithTag("campo_descripcion").assert(hasImeAction(ImeAction.Done))
    }

    @Test
    fun siguienteEnfocaDescripcion() {
        composeRule.onNodeWithTag("campo_titulo").performTextInput("Proyector sin señal")
        composeRule.onNodeWithTag("campo_titulo").performImeAction()

        composeRule.onNodeWithTag("campo_descripcion").assertIsFocused()
    }

    @Test
    fun tocarPrioridadLaSelecciona() {
        composeRule.onNodeWithTag("prioridad_ALTA").performClick()

        composeRule.onNodeWithTag("prioridad_ALTA").assertIsSelected()
        composeRule.onNodeWithTag("prioridad_BAJA").assertIsNotSelected()
        composeRule.onNodeWithTag("prioridad_MEDIA").assertIsNotSelected()
        composeRule.onNodeWithTag("retroalimentacion_prioridad")
            .assertTextEquals("Prioridad seleccionada: Alta · Atención inmediata")
    }

    @Test
    fun registrarSinPrioridadMuestraError() {
        composeRule.onNodeWithTag("campo_titulo").performTextInput("Proyector sin señal")
        composeRule.onNodeWithTag("campo_descripcion").performTextInput("No proyecta imagen.")
        composeRule.onNodeWithTag("boton_registrar").performClick()

        composeRule.onNodeWithTag("mensaje_estado")
            .assertTextEquals("Selecciona una prioridad antes de registrar.")
    }

    @Test
    fun registroCompletoAgregaIncidencia() {
        composeRule.onNodeWithTag("campo_titulo").performTextInput("Proyector sin señal")
        composeRule.onNodeWithTag("campo_descripcion").performTextInput("No proyecta imagen.")
        composeRule.onNodeWithTag("prioridad_MEDIA").performClick()
        composeRule.onNodeWithTag("boton_registrar").performClick()

        composeRule.onNodeWithTag("mensaje_estado")
            .assertTextEquals("Incidencia registrada correctamente con prioridad Media.")
        composeRule.onNodeWithText("Incidencias registradas (1)").assertExists()
        composeRule.onNodeWithText("Proyector sin señal").assertExists()
        composeRule.onNodeWithTag("prioridad_MEDIA").assertIsNotSelected()
    }
}
