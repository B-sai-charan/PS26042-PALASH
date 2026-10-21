package com.sih2026.palash.audio

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

enum class AudioPlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    COMPLETED,
    ERROR
}

interface AudioPlaybackEngine {
    val currentState: AudioPlaybackState
    val currentPlayingAudioId: StateFlow<String?>
    fun playAsset(
        assetPath: String,
        audioId: String = assetPath,
        onStateChanged: (AudioPlaybackState, String?) -> Unit
    )
    fun stop()
    fun release()
}

class AndroidAudioPlaybackEngineImpl(
    private val context: Context
) : AudioPlaybackEngine {

    override val currentState: AudioPlaybackState
        get() = if (GlobalAudioPlayer.currentPlayingAudioId.value != null) AudioPlaybackState.PLAYING else AudioPlaybackState.IDLE

    override val currentPlayingAudioId: StateFlow<String?>
        get() = GlobalAudioPlayer.currentPlayingAudioId

    override fun playAsset(
        assetPath: String,
        audioId: String,
        onStateChanged: (AudioPlaybackState, String?) -> Unit
    ) {
        GlobalAudioPlayer.playAudio(context, audioId, assetPath, onStateChanged)
    }

    override fun stop() {
        GlobalAudioPlayer.stop()
    }

    override fun release() {
        GlobalAudioPlayer.stop()
    }
}
