package com.sih2026.palash.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

/**
 * Thread-safe central audio player manager enforcing SINGLE ACTIVE PLAYER rule across PALASH app.
 * Maximum one audio playback stream may be active at any given time.
 */
object GlobalAudioPlayer {
    private const val TAG = "GlobalAudioPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var currentAfd: android.content.res.AssetFileDescriptor? = null
    private var currentFis: FileInputStream? = null
    private var pcmAudioTrack: AudioTrack? = null
    private var pcmJob: Job? = null
    private val pcmScope = CoroutineScope(Dispatchers.IO)

    private val _currentPlayingAudioId = MutableStateFlow<String?>(null)
    val currentPlayingAudioId: StateFlow<String?> = _currentPlayingAudioId.asStateFlow()

    private var currentCallback: ((AudioPlaybackState, String?) -> Unit)? = null

    /**
     * Plays audio (WAV file or asset) using MediaPlayer.
     * Automatically stops and cleans up any currently active audio item before starting.
     */
    @Synchronized
    fun playAudio(
        context: Context,
        audioId: String,
        audioPath: String,
        onStateChanged: ((AudioPlaybackState, String?) -> Unit)? = null
    ) {
        Log.i(TAG, "playAudio requested: id='$audioId', path='$audioPath'")

        // 1. Stop any currently active audio item
        stopInternal()

        // 2. Set new active item
        _currentPlayingAudioId.value = audioId
        currentCallback = onStateChanged

        updateState(AudioPlaybackState.LOADING, null)

        try {
            val diskFile = File(audioPath)
            val isDiskFile = diskFile.exists() && diskFile.isFile

            val player = MediaPlayer()
            mediaPlayer = player

            if (isDiskFile) {
                val fis = FileInputStream(diskFile)
                currentFis = fis
                player.setDataSource(fis.fd)
            } else {
                try {
                    val afd = context.assets.openFd(audioPath)
                    currentAfd = afd
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                } catch (e: Exception) {
                    // Fallback to cache file if openFd fails
                    val tempFile = File(context.cacheDir, "temp_global_play_${audioId.hashCode()}.wav")
                    context.assets.open(audioPath).use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    player.setDataSource(tempFile.absolutePath)
                }
            }

            player.setOnPreparedListener { mp ->
                if (mediaPlayer == mp && _currentPlayingAudioId.value == audioId) {
                    val rate = com.sih2026.palash.tts.LiveTtsConfig.getSpeechRateForAudioId(audioId)
                    if (rate != 1.0f) {
                        try {
                            val params = mp.playbackParams
                            params.speed = rate
                            mp.playbackParams = params
                            Log.i(TAG, "Applied pitch-preserving speed $rate to MediaPlayer for id: '$audioId'")
                        } catch (e: Exception) {
                            Log.w(TAG, "Could not set MediaPlayer playbackParams: ${e.message}")
                        }
                    }
                    Log.i(TAG, "MediaPlayer prepared, starting playback for id: '$audioId'")
                    updateState(AudioPlaybackState.PLAYING, null)
                    mp.start()
                }
            }

            player.setOnCompletionListener { mp ->
                if (mediaPlayer == mp) {
                    Log.i(TAG, "MediaPlayer completed playback for id: '$audioId'")
                    updateState(AudioPlaybackState.COMPLETED, null)
                    cleanUpInternalOnly()
                    if (_currentPlayingAudioId.value == audioId) {
                        _currentPlayingAudioId.value = null
                    }
                }
            }

            player.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer error for id '$audioId': what=$what, extra=$extra")
                val errorMsg = "Audio playback error (what: $what, extra: $extra)"
                updateState(AudioPlaybackState.ERROR, errorMsg)
                cleanUpInternalOnly()
                if (_currentPlayingAudioId.value == audioId) {
                    _currentPlayingAudioId.value = null
                }
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load/prepare audio for id '$audioId': ${e.message}", e)
            val errorMsg = "Failed to load audio: ${e.localizedMessage}"
            updateState(AudioPlaybackState.ERROR, errorMsg)
            cleanUpInternalOnly()
            if (_currentPlayingAudioId.value == audioId) {
                _currentPlayingAudioId.value = null
            }
        }
    }

    /**
     * Plays raw PCM or WAV file using AudioTrack on Dispatchers.IO with exact sample rate detection.
     */
    @Synchronized
    fun playPcm(
        audioId: String,
        file: File,
        sampleRate: Int = 16000,
        onStateChanged: ((AudioPlaybackState, String?) -> Unit)? = null
    ) {
        Log.i(TAG, "playPcm requested: id='$audioId', file='${file.absolutePath}'")

        stopInternal()

        _currentPlayingAudioId.value = audioId
        currentCallback = onStateChanged

        if (!file.exists() || file.length() == 0L) {
            updateState(AudioPlaybackState.ERROR, "Audio file does not exist")
            _currentPlayingAudioId.value = null
            return
        }

        updateState(AudioPlaybackState.PLAYING, null)

        pcmJob = pcmScope.launch {
            var inputStream: FileInputStream? = null
            try {
                var actualSampleRate = sampleRate
                var dataOffset = 0L

                // Detect RIFF WAV header for WAV files regardless of file extension
                if (file.length() >= 44L) {
                    val headerBytes = ByteArray(44)
                    val fis = FileInputStream(file)
                    val read = fis.read(headerBytes)
                    fis.close()
                    if (read >= 44 && headerBytes[0] == 'R'.code.toByte() && headerBytes[1] == 'I'.code.toByte() && headerBytes[2] == 'F'.code.toByte() && headerBytes[3] == 'F'.code.toByte()) {
                        val sr = (headerBytes[24].toInt() and 0xFF) or
                                ((headerBytes[25].toInt() and 0xFF) shl 8) or
                                ((headerBytes[26].toInt() and 0xFF) shl 16) or
                                ((headerBytes[27].toInt() and 0xFF) shl 24)
                        if (sr in 8000..48000) {
                            actualSampleRate = sr
                        }
                        dataOffset = 44L
                        Log.i(TAG, "RIFF WAV header detected for '$audioId': sampleRate=$actualSampleRate, skipping $dataOffset bytes header.")
                    } else {
                        Log.i(TAG, "Raw PCM stream assumed for '$audioId': sampleRate=$actualSampleRate, dataOffset=0.")
                    }
                }

                val channelMask = AudioFormat.CHANNEL_OUT_MONO
                val encoding = AudioFormat.ENCODING_PCM_16BIT
                val minBufferSize = AudioTrack.getMinBufferSize(actualSampleRate, channelMask, encoding)
                val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(encoding)
                            .setSampleRate(actualSampleRate)
                            .setChannelMask(channelMask)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                pcmAudioTrack = track
                val rate = com.sih2026.palash.tts.LiveTtsConfig.getSpeechRateForAudioId(audioId)
                if (rate != 1.0f) {
                    try {
                        val params = track.playbackParams
                        params.speed = rate
                        track.playbackParams = params
                        Log.i(TAG, "Applied pitch-preserving speed $rate to AudioTrack for id: '$audioId'")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not set AudioTrack playbackParams speed: ${e.message}")
                    }
                }
                track.play()

                inputStream = FileInputStream(file)
                if (dataOffset > 0) {
                    inputStream.skip(dataOffset)
                }

                val buffer = ByteArray(bufferSize)
                var bytesRead = inputStream.read(buffer)

                while (bytesRead > 0 && _currentPlayingAudioId.value == audioId) {
                    val written = track.write(buffer, 0, bytesRead)
                    if (written < 0) break
                    bytesRead = inputStream.read(buffer)
                }

                if (_currentPlayingAudioId.value == audioId) {
                    updateState(AudioPlaybackState.COMPLETED, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "PCM playback exception: ${e.message}", e)
                if (_currentPlayingAudioId.value == audioId) {
                    updateState(AudioPlaybackState.ERROR, e.localizedMessage ?: "Playback error")
                }
            } finally {
                try { inputStream?.close() } catch (_: Exception) {}
                cleanUpInternalOnly()
                if (_currentPlayingAudioId.value == audioId) {
                    _currentPlayingAudioId.value = null
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        stopInternal()
    }

    private fun stopInternal() {
        val oldId = _currentPlayingAudioId.value
        cleanUpInternalOnly()
        if (oldId != null) {
            updateState(AudioPlaybackState.IDLE, null)
            _currentPlayingAudioId.value = null
        }
    }

    private fun cleanUpInternalOnly() {
        pcmJob?.cancel()
        pcmJob = null

        pcmAudioTrack?.let { track ->
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.flush()
                track.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing AudioTrack: ${e.message}")
            }
        }
        pcmAudioTrack = null

        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null

        currentAfd?.let { afd ->
            try {
                afd.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing AssetFileDescriptor: ${e.message}")
            }
        }
        currentAfd = null

        currentFis?.let { fis ->
            try {
                fis.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing FileInputStream: ${e.message}")
            }
        }
        currentFis = null
    }

    private fun updateState(state: AudioPlaybackState, errorMsg: String?) {
        currentCallback?.invoke(state, errorMsg)
    }
}
