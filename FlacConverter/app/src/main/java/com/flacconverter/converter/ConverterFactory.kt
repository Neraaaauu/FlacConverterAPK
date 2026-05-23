package com.flacconverter.converter

import com.flacconverter.model.OutputFormat

object ConverterFactory {
    fun create(format: OutputFormat): AudioConverter {
        return when (format) {
            OutputFormat.MP3 -> Mp3Converter()
            OutputFormat.OGG -> OggConverter()
            OutputFormat.OPUS -> OpusConverter()
            OutputFormat.AAC -> AacConverter()
        }
    }
}
