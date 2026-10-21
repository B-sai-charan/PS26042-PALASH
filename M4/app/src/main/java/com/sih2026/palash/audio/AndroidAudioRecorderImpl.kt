package com.sih2026.palash.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorderImpl(
    private val context: Context
) : AudioRecorder {

    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val MAX_RECORDING_DURATION_MS = 10000L
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null
    private val recorderScope = CoroutineScope(Dispatchers.IO)

    @Volatile
    override var currentState: RecordingState = RecordingState.IDLE
        private set

    @Volatile
    override var currentDurationMs: Long = 0L
        private set

    private var targetFile: File? = null
    private var recordingStartTime: Long = 0L
    private var autoStopCallback: ((AudioRecordingResult?) -> Unit)? = null

    @SuppressLint("MissingPermission")
    override fun startRecording(
        outputFile: File,
        onStateChanged: (RecordingState, String?) -> Unit
    ) {
        if (currentState == RecordingState.RECORDING) return

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            currentState = RecordingState.ERROR
            onStateChanged(RecordingState.ERROR, "Unsupported audio configuration or device buffer error")
            return
        }

        try {
            val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                currentState = RecordingState.ERROR
                onStateChanged(RecordingState.ERROR, "AudioRecord initialization failed")
                return
            }

            targetFile = outputFile
            outputFile.parentFile?.mkdirs()

            audioRecord?.startRecording()
            recordingStartTime = System.currentTimeMillis()
            currentState = RecordingState.RECORDING
            currentDurationMs = 0L
            onStateChanged(RecordingState.RECORDING, null)

            // Background thread to stream PCM buffer to disk file
            recordingJob = recorderScope.launch {
                val pcmBuffer = ByteArray(bufferSize)
                FileOutputStream(outputFile).use { fos ->
                    while (isActive && currentState == RecordingState.RECORDING) {
                        val readBytes = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                        if (readBytes > 0) {
                            fos.write(pcmBuffer, 0, readBytes)
                        }
                    }
                    fos.flush()
                }
            }

            // Duration timer and 10s auto-stop
            timerJob = recorderScope.launch {
                while (isActive && currentState == RecordingState.RECORDING) {
                    delay(100)
                    val elapsed = System.currentTimeMillis() - recordingStartTime
                    currentDurationMs = elapsed
                    if (elapsed >= MAX_RECORDING_DURATION_MS) {
                        stopRecording { result ->
                            autoStopCallback?.invoke(result)
                        }
                        break
                    }
                }
            }

        } catch (e: Exception) {
            releaseInternal()
            currentState = RecordingState.ERROR
            onStateChanged(RecordingState.ERROR, "Microphone recording error: ${e.localizedMessage}")
        }
    }

    override fun stopRecording(onCompleted: (AudioRecordingResult?) -> Unit) {
        if (currentState != RecordingState.RECORDING && currentState != RecordingState.STOPPING) {
            val file = targetFile
            if (file != null && file.exists() && file.length() > 0) {
                val duration = if (currentDurationMs > 0) currentDurationMs else (System.currentTimeMillis() - recordingStartTime)
                onCompleted(
                    AudioRecordingResult(
                        audioFile = file,
                        durationMs = duration,
                        sampleRate = SAMPLE_RATE,
                        channels = 1,
                        bitsPerSample = 16
                    )
                )
            } else {
                onCompleted(null)
            }
            return
        }

        currentState = RecordingState.STOPPING
        timerJob?.cancel()

        recorderScope.launch {
            val duration = if (currentDurationMs > 0) currentDurationMs else (System.currentTimeMillis() - recordingStartTime)
            currentDurationMs = duration

            try {
                audioRecord?.stop()
            } catch (e: Exception) {
                // Ignore stop exceptions
            }

            recordingJob?.join()
            releaseInternal()

            currentState = RecordingState.RECORDED

            val file = targetFile
            if (file != null && file.exists() && file.length() > 0) {
                val result = AudioRecordingResult(
                    audioFile = file,
                    durationMs = duration,
                    sampleRate = SAMPLE_RATE,
                    channels = 1,
                    bitsPerSample = 16
                )
                onCompleted(result)
            } else {
                currentState = RecordingState.ERROR
                onCompleted(null)
            }
        }
    }

    override fun cancelRecording() {
        timerJob?.cancel()
        recordingJob?.cancel()
        releaseInternal()
        targetFile?.delete()
        targetFile = null
        currentDurationMs = 0L
        currentState = RecordingState.IDLE
    }

    override fun release() {
        cancelRecording()
    }

    private fun releaseInternal() {
        try {
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore release exceptions
        }
        audioRecord = null
    }
}
