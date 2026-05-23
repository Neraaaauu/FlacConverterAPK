package com.flacconverter.converter

data class ConversionResult(
    val success: Boolean,
    val outputName: String? = null,
    val error: String? = null
)

data class ConversionSummary(
    val successCount: Int,
    val failCount: Int,
    val total: Int
)
