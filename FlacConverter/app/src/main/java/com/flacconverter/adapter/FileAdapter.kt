package com.flacconverter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flacconverter.R
import com.flacconverter.databinding.ItemFileBinding
import com.flacconverter.model.AudioFile

class FileAdapter(
    private val files: MutableList<AudioFile>,
    private val onRemove: (AudioFile) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        with(holder.binding) {
            tvFileName.text = file.name
            tvFileSize.text = file.sizeFormatted
            ivFileIcon.setImageResource(R.drawable.ic_audio_file)
            btnRemove.setOnClickListener { onRemove(file) }
        }
    }

    override fun getItemCount() = files.size
}
