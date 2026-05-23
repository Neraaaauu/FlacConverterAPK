package com.flacconverter.converter

import android.content.Context
import com.flacconverter.db.HistoryDatabase
import com.flacconverter.model.AudioFile
import com.flacconverter.model.ConversionSettings

class ConversionFacade(
    private val context: Context,
    private val database: HistoryDatabase,
    private val observer: ConversionObserver
) {
    suspend fun convertFiles(files: List<AudioFile>, settings: ConversionSettings): ConversionSummary {
        observer.onQueueStarted(files.size)

        val iterator = AudioFileIterator(files)
        var index = 0
        var successCount = 0
        var failCount = 0

        while (iterator.hasNext()) {
            val audioFile = iterator.next()
            index++

            observer.onFileSelected(audioFile, index, files.size)

            val command = ConvertAudioCommand(
                context = context,
                audioFile = audioFile,
                settings = settings,
                database = database,
                observer = observer
            )

            val result = command.execute()

            if (result.success) {
                successCount++
                observer.onConversionLog("✅ ${audioFile.name} → ${result.outputName}", true)
            } else {
                failCount++
                observer.onConversionLog("❌ ${audioFile.name}: ${result.error}", false)
            }
        }

        val summary = ConversionSummary(successCount, failCount, files.size)
        observer.onQueueFinished(summary)
        return summary
    }
}
