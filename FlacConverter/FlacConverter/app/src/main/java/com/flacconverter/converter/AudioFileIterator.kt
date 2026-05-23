package com.flacconverter.converter

import com.flacconverter.model.AudioFile

class AudioFileIterator(files: List<AudioFile>) : Iterator<AudioFile> {
    private val iterator = files.iterator()

    override fun hasNext(): Boolean = iterator.hasNext()

    override fun next(): AudioFile = iterator.next()
}
