package com.example.registroincidencias.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.registroincidencias.R
import com.example.registroincidencias.model.Prioridad
import com.example.registroincidencias.ui.theme.PrioridadAlta
import com.example.registroincidencias.ui.theme.PrioridadBaja
import com.example.registroincidencias.ui.theme.PrioridadMedia

val Prioridad.color: Color
    get() = when (this) {
        Prioridad.BAJA -> PrioridadBaja
        Prioridad.MEDIA -> PrioridadMedia
        Prioridad.ALTA -> PrioridadAlta
    }

/**
 * Selector táctil de prioridad: cada opción es una tarjeta completa que responde al toque
 * mediante [selectable] (rol RadioButton), por lo que el área táctil abarca toda la tarjeta
 * y TalkBack anuncia la opción como seleccionada o no seleccionada.
 */
@Composable
fun PrioridadSelector(
    seleccionada: Prioridad?,
    onSeleccionar: (Prioridad) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.label_prioridad),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = stringResource(R.string.hint_prioridad),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Prioridad.entries.forEach { prioridad ->
                OpcionPrioridad(
                    prioridad = prioridad,
                    seleccionada = prioridad == seleccionada,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSeleccionar(prioridad)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        RetroalimentacionPrioridad(seleccionada)
    }
}

@Composable
private fun OpcionPrioridad(
    prioridad: Prioridad,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = prioridad.color
    val shape = MaterialTheme.shapes.medium
    val fondo by animateColorAsState(
        targetValue = if (seleccionada) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        label = "fondoPrioridad"
    )
    val borde by animateColorAsState(
        targetValue = if (seleccionada) color else MaterialTheme.colorScheme.outlineVariant,
        label = "bordePrioridad"
    )

    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(shape)
            .selectable(
                selected = seleccionada,
                onClick = onClick,
                role = Role.RadioButton
            )
            .testTag("prioridad_${prioridad.name}"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(if (seleccionada) 2.dp else 1.dp, borde)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RadioButton(
                selected = seleccionada,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = color,
                    unselectedColor = color
                )
            )
            Text(
                text = stringResource(prioridad.etiqueta),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium,
                color = if (seleccionada) color else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(prioridad.detalle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RetroalimentacionPrioridad(seleccionada: Prioridad?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(seleccionada?.color ?: MaterialTheme.colorScheme.outlineVariant)
        )
        Text(
            text = if (seleccionada != null) {
                stringResource(
                    R.string.prioridad_seleccionada,
                    stringResource(seleccionada.etiqueta),
                    stringResource(seleccionada.detalle)
                )
            } else {
                stringResource(R.string.prioridad_sin_seleccion)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (seleccionada != null) FontWeight.SemiBold else FontWeight.Normal,
            color = seleccionada?.color ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("retroalimentacion_prioridad")
        )
    }
}

@Composable
fun EtiquetaPrioridad(prioridad: Prioridad, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = prioridad.color.copy(alpha = 0.12f),
        contentColor = prioridad.color
    ) {
        Text(
            text = stringResource(prioridad.etiqueta),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
