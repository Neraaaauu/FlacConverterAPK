package com.flacconverter.converter

interface ConversionCommand {
    suspend fun execute(): ConversionResult
}
