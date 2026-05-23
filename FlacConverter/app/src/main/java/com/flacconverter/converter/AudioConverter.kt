package com.flacconverter.converter

import com.flacconverter.model.ConversionSettings

interface AudioConverter {
    fun buildCommand(input: String, output: String, settings: ConversionSettings): String
}
