package com.flacconverter.converter

import com.flacconverter.model.AudioFile

interface ConversionObserver {
    fun onQueueStarted(total: Int)
    fun onFileSelected(file: AudioFile, index: Int, total: Int)
    fun onStateChanged(file: AudioFile, state: ConversionState)
    fun onConversionLog(message: String, success: Boolean)
    fun onQueueFinished(summary: ConversionSummary)
}
