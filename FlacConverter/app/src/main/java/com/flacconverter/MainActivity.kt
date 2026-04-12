package com.flacconverter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.flacconverter.databinding.ActivityMainBinding
import com.flacconverter.model.AudioFile
import com.flacconverter.model.ConversionSettings
import com.flacconverter.model.OutputFormat
import com.flacconverter.adapter.FileAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fileAdapter: FileAdapter
    private val selectedFiles = mutableListOf<AudioFile>()
    private var outputFormat = OutputFormat.MP3
    private var bitrate = "192k"
    private var quality = "5"
    private var outputUri: Uri? = null

    private val pickFiles = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris?.forEach { uri -> addFile(uri) }
        updateUI()
    }

    private val pickOutputDir = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            outputUri = it
            binding.tvOutputPath.text = it.path?.replace("/tree/primary:", "📁 /storage/")
                ?: "📁 Carpeta seleccionada"
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pickFiles.launch(arrayOf("audio/flac", "audio/*"))
        else Snackbar.make(binding.root, "Se necesita permiso para leer archivos", Snackbar.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFormatSelector()
        setupBitrateSelector()
        setupButtons()
        updateUI()
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(selectedFiles) { file ->
            selectedFiles.remove(file)
            fileAdapter.notifyDataSetChanged()
            updateUI()
        }
        binding.rvFiles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
        }
    }

    private fun setupFormatSelector() {
        binding.chipMp3.isChecked = true
        val chips = listOf(binding.chipMp3, binding.chipOgg, binding.chipOpus, binding.chipAac)

        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { it.isChecked = false }
                chip.isChecked = true
                outputFormat = when (chip.id) {
                    R.id.chipMp3 -> OutputFormat.MP3
                    R.id.chipOgg -> OutputFormat.OGG
                    R.id.chipOpus -> OutputFormat.OPUS
                    R.id.chipAac -> OutputFormat.AAC
                    else -> OutputFormat.MP3
                }
                updateBitrateOptions()
            }
        }
    }

    private fun setupBitrateSelector() {
        binding.chipBitrate128.isChecked = false
        binding.chipBitrate192.isChecked = true
        binding.chipBitrate256.isChecked = false
        binding.chipBitrate320.isChecked = false

        val bitrateChips = listOf(
            binding.chipBitrate128, binding.chipBitrate192,
            binding.chipBitrate256, binding.chipBitrate320
        )

        bitrateChips.forEach { chip ->
            chip.setOnClickListener {
                bitrateChips.forEach { it.isChecked = false }
                chip.isChecked = true
                bitrate = when (chip.id) {
                    R.id.chipBitrate128 -> "128k"
                    R.id.chipBitrate192 -> "192k"
                    R.id.chipBitrate256 -> "256k"
                    R.id.chipBitrate320 -> "320k"
                    else -> "192k"
                }
            }
        }

        binding.sliderQuality.addOnChangeListener { _, value, _ ->
            quality = value.toInt().toString()
            binding.tvQualityValue.text = "Calidad: ${value.toInt()}/10"
        }
    }

    private fun updateBitrateOptions() {
        // Opus, MP3 y AAC utilizan Bitrate. Solo OGG Vorbis usa la escala de calidad 0-10.
        val showBitrate = outputFormat != OutputFormat.OGG
        val showQuality = outputFormat == OutputFormat.OGG
        
        binding.layoutBitrate.visibility = if (showBitrate) View.VISIBLE else View.GONE
        binding.layoutQuality.visibility = if (showQuality) View.VISIBLE else View.GONE
    }

    private fun setupButtons() {
        binding.btnAddFiles.setOnClickListener { checkPermissionAndPick() }
        binding.btnClearAll.setOnClickListener {
            selectedFiles.clear()
            fileAdapter.notifyDataSetChanged()
            updateUI()
        }
        binding.btnOutputFolder.setOnClickListener { pickOutputDir.launch(null) }
        binding.btnConvert.setOnClickListener { startConversion() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun checkPermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED ->
                pickFiles.launch(arrayOf("audio/flac", "audio/*"))
            else -> requestPermission.launch(permission)
        }
    }

    private fun addFile(uri: Uri) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                val name = if (nameIndex >= 0) it.getString(nameIndex) else "archivo.flac"
                val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L

                if (selectedFiles.none { f -> f.uri == uri }) {
                    selectedFiles.add(AudioFile(uri = uri, name = name, size = size))
                }
            }
        }
    }

    private fun startConversion() {
        if (selectedFiles.isEmpty()) {
            Snackbar.make(binding.root, "Agrega archivos FLAC primero", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (outputUri == null) {
            MaterialAlertDialogBuilder(this)
                .setTitle("¿Sin carpeta de destino?")
                .setMessage("Se guardarán en la carpeta de Descargas. ¿Continuar?")
                .setPositiveButton("Continuar") { _, _ -> launchConversion() }
                .setNegativeButton("Elegir carpeta") { _, _ -> pickOutputDir.launch(null) }
                .show()
        } else {
            launchConversion()
        }
    }

    private fun launchConversion() {
        val settings = ConversionSettings(
            outputFormat = outputFormat,
            bitrate = bitrate,
            quality = quality,
            outputUri = outputUri
        )
        val intent = Intent(this, ConversionActivity::class.java).apply {
            putParcelableArrayListExtra("files", ArrayList(selectedFiles))
            putExtra("settings", settings)
        }
        startActivity(intent)
    }

    private fun updateUI() {
        val count = selectedFiles.size
        binding.tvFileCount.text = if (count == 0) "Sin archivos seleccionados"
            else "$count archivo${if (count != 1) "s" else ""} seleccionado${if (count != 1) "s" else ""}"
        binding.btnConvert.isEnabled = count > 0
        binding.btnClearAll.visibility = if (count > 0) View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility = if (count == 0) View.VISIBLE else View.GONE
        binding.rvFiles.visibility = if (count > 0) View.VISIBLE else View.GONE
    }
}
