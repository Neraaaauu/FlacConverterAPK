package com.flacconverter.converter

sealed class ConversionState(val label: String) {
    object Pending : ConversionState("Pendiente")
    object Converting : ConversionState("Convirtiendo")
    object Completed : ConversionState("Completado")
    object Cancelled : ConversionState("Cancelado")
    data class Error(val message: String) : ConversionState("Error")
}
