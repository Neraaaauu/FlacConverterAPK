package com.flacconverter.converter

import com.flacconverter.model.ConversionSettings

class Mp3Converter : AudioConverter {
    override fun buildCommand(input: String, output: String, settings: ConversionSettings): String {
        return "-i \"$input\" -codec:a libmp3lame -b:a ${settings.bitrate} -y \"$output\""
    }
}

class OggConverter : AudioConverter {
    override fun buildCommand(input: String, output: String, settings: ConversionSettings): String {
        return "-i \"$input\" -codec:a libvorbis -b:a ${settings.bitrate} -q:a ${settings.quality} -y \"$output\""
    }
}

class OpusConverter : AudioConverter {
    override fun buildCommand(input: String, output: String, settings: ConversionSettings): String {
        return "-i \"$input\" -codec:a libopus -b:a ${settings.bitrate} -vbr on -y \"$output\""
    }
}

class AacConverter : AudioConverter {
    override fun buildCommand(input: String, output: String, settings: ConversionSettings): String {
        return "-i \"$input\" -codec:a aac -b:a ${settings.bitrate} -y \"$output\""
    }
}
