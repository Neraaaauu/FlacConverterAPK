package com.flacconverter.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

enum class OutputFormat(val extension: String, val label: String) {
    MP3("mp3", "MP3"),
    OGG("ogg", "OGG Vorbis"),
    OPUS("opus", "OGG Opus"),
    AAC("aac", "AAC")
}

@Parcelize
data class AudioFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    var status: FileStatus = FileStatus.PENDING
) : Parcelable {
    val sizeFormatted: String get() {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    }
}

enum class FileStatus { PENDING, CONVERTING, DONE, ERROR }

@Parcelize
data class ConversionSettings(
    val outputFormat: OutputFormat,
    val bitrate: String,
    val quality: String,
    val outputUri: Uri?
) : Parcelable

@Entity(tableName = "conversion_history")
data class ConversionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inputName: String,
    val outputName: String,
    val format: String,
    val bitrate: String,
    val date: String,
    val success: Boolean
)
