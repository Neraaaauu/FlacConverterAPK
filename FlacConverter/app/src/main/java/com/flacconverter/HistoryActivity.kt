package com.flacconverter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flacconverter.databinding.ActivityHistoryBinding
import com.flacconverter.databinding.ItemHistoryBinding
import com.flacconverter.db.HistoryDatabase
import com.flacconverter.model.ConversionHistory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val db by lazy { HistoryDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnClearHistory.setOnClickListener { confirmClear() }

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val history = withContext(Dispatchers.IO) { db.historyDao().getAll() }
            if (history.isEmpty()) {
                binding.tvEmpty.visibility = android.view.View.VISIBLE
                binding.rvHistory.visibility = android.view.View.GONE
            } else {
                binding.tvEmpty.visibility = android.view.View.GONE
                binding.rvHistory.visibility = android.view.View.VISIBLE
                binding.rvHistory.layoutManager = LinearLayoutManager(this@HistoryActivity)
                binding.rvHistory.adapter = HistoryAdapter(history)
            }
        }
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Limpiar historial")
            .setMessage("¿Eliminar todo el historial de conversiones?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { db.historyDao().clearAll() }
                    loadHistory()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    inner class HistoryAdapter(private val items: List<ConversionHistory>) :
        RecyclerView.Adapter<HistoryAdapter.VH>() {

        inner class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            with(holder.binding) {
                tvHistoryInput.text = item.inputName
                tvHistoryOutput.text = if (item.success) "→ ${item.outputName}" else "❌ Falló"
                tvHistoryDate.text = item.date
                tvHistoryFormat.text = "${item.format} • ${item.bitrate}"
                ivStatus.setImageResource(
                    if (item.success) R.drawable.ic_check_circle else R.drawable.ic_error
                )
            }
        }
    }
}
