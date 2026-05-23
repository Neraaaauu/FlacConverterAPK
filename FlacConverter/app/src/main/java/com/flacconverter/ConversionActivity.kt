package com.flacconverter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.flacconverter.converter.ConversionFacade
import com.flacconverter.converter.ConversionObserver
import com.flacconverter.converter.ConversionState
import com.flacconverter.converter.ConversionSummary
import com.flacconverter.databinding.ActivityConversionBinding
import com.flacconverter.db.HistoryDatabase
import com.flacconverter.model.AudioFile
import com.flacconverter.model.ConversionSettings
import kotlinx.coroutines.launch

class ConversionActivity : AppCompatActivity(), ConversionObserver {

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
            val facade = ConversionFacade(
                context = this@ConversionActivity,
                database = db,
                observer = this@ConversionActivity
            )
            facade.convertFiles(files, settings)
        }
    }

    override fun onQueueStarted(total: Int) {
        runOnUiThread {
            binding.progressBar.max = total
            binding.progressBar.progress = 0
            binding.tvProgress.text = "0 / $total"
        }
    }

    override fun onFileSelected(file: AudioFile, index: Int, total: Int) {
        runOnUiThread {
            binding.tvCurrentFile.text = "Convirtiendo: ${file.name}"
            binding.tvProgress.text = "$index / $total"
        }
    }

    override fun onStateChanged(file: AudioFile, state: ConversionState) {
        runOnUiThread {
            when (state) {
                ConversionState.Pending -> binding.tvCurrentFile.text = "Pendiente: ${file.name}"
                ConversionState.Converting -> binding.tvCurrentFile.text = "Convirtiendo: ${file.name}"
                ConversionState.Completed -> binding.tvCurrentFile.text = "Completado: ${file.name}"
                ConversionState.Cancelled -> binding.tvCurrentFile.text = "Cancelado: ${file.name}"
                is ConversionState.Error -> binding.tvCurrentFile.text = "Error en: ${file.name}"
            }
        }
    }

    override fun onConversionLog(message: String, success: Boolean) {
        runOnUiThread {
            val current = binding.tvLog.text.toString()
            binding.tvLog.text = "$message\n$current"

            if (binding.progressBar.progress < binding.progressBar.max) {
                binding.progressBar.progress = binding.progressBar.progress + 1
            }
        }
    }

    override fun onQueueFinished(summary: ConversionSummary) {
        runOnUiThread {
            binding.progressBar.progress = summary.total
            binding.tvCurrentFile.text = "¡Conversión terminada!"
            binding.tvProgress.text = "✅ ${summary.successCount} exitosos   ❌ ${summary.failCount} fallidos"
            binding.btnCancel.visibility = View.GONE
            binding.btnDone.visibility = View.VISIBLE
        }
    }
}
