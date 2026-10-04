package com.example.registroincidencias.model

import androidx.annotation.StringRes
import com.example.registroincidencias.R

enum class Prioridad(
    @StringRes val etiqueta: Int,
    @StringRes val detalle: Int
) {
    BAJA(R.string.prioridad_baja, R.string.prioridad_baja_detalle),
    MEDIA(R.string.prioridad_media, R.string.prioridad_media_detalle),
    ALTA(R.string.prioridad_alta, R.string.prioridad_alta_detalle)
}
