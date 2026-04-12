package com.flacconverter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.flacconverter.databinding.ActivityConversionBinding
import com.flacconverter.db.HistoryDatabase
import com.flacconverter.model.AudioFile
import com.flacconverter.model.ConversionHistory
import com.flacconverter.model.ConversionSettings
import com.flacconverter.model.OutputFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ConversionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversionBinding
    private val db by lazy { HistoryDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val files = intent.getParcelableArrayListExtra<AudioFile>("files") ?: return
        val settings = intent.getParcelableExtra<ConversionSettings>("settings") ?: return

        binding.btnCancel.setOnClickListener {
            FFmpegKit.cancel()
            finish()
        }
        binding.btnDone.setOnClickListener { finish() }

        startConversion(files, settings)
    }

    private fun startConversion(files: List<AudioFile>, settings: ConversionSettings) {
        lifecycleScope.launch {
            binding.progressBar.max = files.size
            binding.progressBar.progress = 0
            var successCount = 0
            var failCount = 0

            files.forEachIndexed { index, audioFile ->
                binding.tvCurrentFile.text = "Convirtiendo: ${audioFile.name}"
                binding.tvProgress.text = "${index + 1} / ${files.size}"

                val result = withContext(Dispatchers.IO) {
                    convertFile(audioFile, settings)
                }

                if (result.success) {
                    successCount++
                    addLog("✅ ${audioFile.name} → ${result.outputName}", success = true)
                    saveHistory(audioFile, result.outputName ?: "", settings, true)
                } else {
                    failCount++
                    addLog("❌ ${audioFile.name}: ${result.error}", success = false)
                    saveHistory(audioFile, "", settings, false)
                }

                binding.progressBar.progress = index + 1
            }

            // Done
            binding.tvCurrentFile.text = "¡Conversión terminada!"
            binding.tvProgress.text = "✅ $successCount exitosos   ❌ $failCount fallidos"
            binding.btnCancel.visibility = View.GONE
            binding.btnDone.visibility = View.VISIBLE
        }
    }

    private fun convertFile(audioFile: AudioFile, settings: ConversionSettings): ConversionResult {
        return try {
            // Copy input to temp file
            val inputStream = contentResolver.openInputStream(audioFile.uri)
                ?: return ConversionResult(false, error = "No se pudo leer el archivo")

            val tempInput = File(cacheDir, "input_${System.currentTimeMillis()}.flac")
            tempInput.outputStream().use { out -> inputStream.copyTo(out) }

            val ext = settings.outputFormat.extension
            val baseName = audioFile.name.substringBeforeLast(".")
            val outputName = "$baseName.$ext"

            val outputFile = if (settings.outputUri != null) {
                // Use selected output folder
                val docFile = androidx.documentfile.provider.DocumentFile
                    .fromTreeUri(this, settings.outputUri)
                    ?.createFile("audio/$ext", outputName)
                val outStream = docFile?.uri?.let { contentResolver.openOutputStream(it) }
                    ?: return ConversionResult(false, error = "No se pudo crear archivo de salida")
                val tempOut = File(cacheDir, "output_${System.currentTimeMillis()}.$ext")
                tempOut.also { it.createNewFile() }
            } else {
                // Use Downloads
                val downloads = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS)
                File(downloads, outputName)
            }

            val command = buildFFmpegCommand(tempInput.absolutePath, outputFile.absolutePath, settings)
            val session = FFmpegKit.execute(command)

            tempInput.delete()

            if (ReturnCode.isSuccess(session.returnCode)) {
                // If output uri was selected, move file there
                if (settings.outputUri != null) {
                    val docFile = androidx.documentfile.provider.DocumentFile
                        .fromTreeUri(this, settings.outputUri)
                        ?.createFile("audio/$ext", outputName)
                    val outStream = docFile?.uri?.let { contentResolver.openOutputStream(it) }
                    if (outStream != null) {
                        outputFile.inputStream().use { it.copyTo(outStream) }
                    }
                    outputFile.delete()
                }
                ConversionResult(true, outputName = outputName)
            } else {
                outputFile.delete()
                ConversionResult(false, error = "Error FFmpeg: ${session.failStackTrace?.take(100)}")
            }
        } catch (e: Exception) {
            ConversionResult(false, error = e.message ?: "Error desconocido")
        }
    }

    private fun buildFFmpegCommand(input: String, output: String, settings: ConversionSettings): String {
        return when (settings.outputFormat) {
            OutputFormat.MP3 -> "-i \"$input\" -codec:a libmp3lame -b:a ${settings.bitrate} -y \"$output\""
            OutputFormat.OGG -> "-i \"$input\" -codec:a libvorbis -b:a ${settings.bitrate} -q:a ${settings.quality} -y \"$output\""
            OutputFormat.OPUS -> "-i \"$input\" -codec:a libopus -b:a 128k -y \"$output\""
            OutputFormat.AAC -> "-i \"$input\" -codec:a aac -b:a ${settings.bitrate} -y \"$output\""
        }
    }

    private fun addLog(message: String, success: Boolean) {
        runOnUiThread {
            val current = binding.tvLog.text.toString()
            binding.tvLog.text = "$message\n$current"
        }
    }

    private suspend fun saveHistory(
        file: AudioFile, outputName: String,
        settings: ConversionSettings, success: Boolean
    ) = withContext(Dispatchers.IO) {
        val entry = ConversionHistory(
            inputName = file.name,
            outputName = outputName,
            format = settings.outputFormat.name,
            bitrate = settings.bitrate,
            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            success = success
        )
        db.historyDao().insert(entry)
    }

    data class ConversionResult(
        val success: Boolean,
        val outputName: String? = null,
        val error: String? = null
    )
}
