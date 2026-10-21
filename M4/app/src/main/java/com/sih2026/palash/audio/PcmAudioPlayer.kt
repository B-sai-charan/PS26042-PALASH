package com.sih2026.palash.audio

import java.io.File

enum class PcmPlaybackState {
    IDLE,
    PLAYING,
    COMPLETED,
    ERROR
}

interface PcmAudioPlayer {
    val currentState: PcmPlaybackState
    fun playPcmFile(
        file: File,
        sampleRate: Int = 16000,
        onStateChanged: (PcmPlaybackState, String?) -> Unit
    )
    fun playPcmFileWithId(
        file: File,
        audioId: String,
        sampleRate: Int = 16000,
        onStateChanged: (PcmPlaybackState, String?) -> Unit
    )
    fun stop()
    fun release()
}

class AndroidPcmAudioPlayerImpl : PcmAudioPlayer {

    override val currentState: PcmPlaybackState
        get() = if (GlobalAudioPlayer.currentPlayingAudioId.value != null) PcmPlaybackState.PLAYING else PcmPlaybackState.IDLE

    override fun playPcmFile(
        file: File,
        sampleRate: Int,
        onStateChanged: (PcmPlaybackState, String?) -> Unit
    ) {
        playPcmFileWithId(file, file.absolutePath, sampleRate, onStateChanged)
    }

    override fun playPcmFileWithId(
        file: File,
        audioId: String,
        sampleRate: Int,
        onStateChanged: (PcmPlaybackState, String?) -> Unit
    ) {
        GlobalAudioPlayer.playPcm(audioId, file, sampleRate) { state, err ->
            val pcmState = when (state) {
                AudioPlaybackState.IDLE -> PcmPlaybackState.IDLE
                AudioPlaybackState.LOADING -> PcmPlaybackState.IDLE
                AudioPlaybackState.PLAYING -> PcmPlaybackState.PLAYING
                AudioPlaybackState.COMPLETED -> PcmPlaybackState.COMPLETED
                AudioPlaybackState.ERROR -> PcmPlaybackState.ERROR
            }
            onStateChanged(pcmState, err)
        }
    }

    override fun stop() {
        GlobalAudioPlayer.stop()
    }

    override fun release() {
        GlobalAudioPlayer.stop()
    }
}
