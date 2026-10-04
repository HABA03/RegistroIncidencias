package com.example.registroincidencias.model

data class Incidencia(
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad
)
