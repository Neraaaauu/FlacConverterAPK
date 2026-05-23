package com.flacconverter.converter

import android.content.Context
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.flacconverter.db.HistoryDatabase
import com.flacconverter.model.AudioFile
import com.flacconverter.model.ConversionHistory
import com.flacconverter.model.ConversionSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConvertAudioCommand(
    private val context: Context,
    private val audioFile: AudioFile,
    private val settings: ConversionSettings,
    private val database: HistoryDatabase,
    private val observer: ConversionObserver
) : ConversionCommand {

    override suspend fun execute(): ConversionResult = withContext(Dispatchers.IO) {
        observer.onStateChanged(audioFile, ConversionState.Converting)

        val result = convertFile()

        if (result.success) {
            observer.onStateChanged(audioFile, ConversionState.Completed)
            saveHistory(result.outputName.orEmpty(), true)
        } else {
            observer.onStateChanged(audioFile, ConversionState.Error(result.error ?: "Error desconocido"))
            saveHistory("", false)
        }

        result
    }

    private fun convertFile(): ConversionResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(audioFile.uri)
                ?: return ConversionResult(false, error = "No se pudo leer el archivo")

            val ext = settings.outputFormat.extension
            val baseName = audioFile.name.substringBeforeLast(".")
            val outputName = "$baseName.$ext"
            val tempInput = File(context.cacheDir, "input_${System.currentTimeMillis()}.flac")
            val tempOutput = File(context.cacheDir, "output_${System.currentTimeMillis()}.$ext")

            inputStream.use { input ->
                tempInput.outputStream().use { output -> input.copyTo(output) }
            }

            val converter = ConverterFactory.create(settings.outputFormat)
            val command = converter.buildCommand(tempInput.absolutePath, tempOutput.absolutePath, settings)
            val session = FFmpegKit.execute(command)

            tempInput.delete()

            if (ReturnCode.isSuccess(session.returnCode)) {
                saveOutputFile(tempOutput, outputName, ext)
                tempOutput.delete()
                ConversionResult(true, outputName = outputName)
            } else {
                tempOutput.delete()
                ConversionResult(false, error = "Error FFmpeg: ${session.failStackTrace?.take(100)}")
            }
        } catch (e: Exception) {
            ConversionResult(false, error = e.message ?: "Error desconocido")
        }
    }

    private fun saveOutputFile(tempOutput: File, outputName: String, ext: String) {
        if (settings.outputUri != null) {
            val docFile = DocumentFile
                .fromTreeUri(context, settings.outputUri)
                ?.createFile("audio/$ext", outputName)

            docFile?.uri?.let { destUri ->
                context.contentResolver.openOutputStream(destUri)?.use { outStream ->
                    tempOutput.inputStream().use { it.copyTo(outStream) }
                }
            }
        } else {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloads.mkdirs()
            val finalFile = File(downloads, outputName)
            tempOutput.copyTo(finalFile, overwrite = true)
        }
    }

    private suspend fun saveHistory(outputName: String, success: Boolean) {
        val entry = ConversionHistory(
            inputName = audioFile.name,
            outputName = outputName,
            format = settings.outputFormat.name,
            bitrate = settings.bitrate,
            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            success = success
        )
        database.historyDao().insert(entry)
    }
}
