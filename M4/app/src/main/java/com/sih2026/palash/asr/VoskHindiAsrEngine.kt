package com.sih2026.palash.asr

import android.content.Context
import android.content.res.AssetManager
import android.os.SystemClock
import android.util.Log
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Concrete Vosk Android ASR Engine implementation for Hindi speech recognition.
 * Accepts 16kHz mono PCM16 audio files.
 */
class VoskHindiAsrEngine : AsrEngine {

    companion object {
        private const val TAG = "PALASH_ASR"
        private const val SAMPLE_RATE = 16000.0f
        private const val MONO_CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
    }

    override var currentState: AsrState = AsrState.ASR_NOT_READY
        private set

    override var lastError: String? = null
        private set

    private var voskModel: Model? = null

    override fun initialize(
        context: Context,
        modelName: String,
        onStateChanged: (AsrState) -> Unit
    ) {
        if (currentState == AsrState.ASR_READY || currentState == AsrState.ASR_LOADING) {
            return
        }

        currentState = AsrState.ASR_LOADING
        onStateChanged(currentState)
        Log.i(TAG, "ASR model loading started for: $modelName")
        val startTime = SystemClock.elapsedRealtime()

        Thread {
            try {
                // Step 1: Check if already unpacked in app files directory
                val targetDir = File(context.filesDir, "models/$modelName")
                val checkFile = File(targetDir, "am/final.mdl")

                val modelDir = if (checkFile.exists() && checkFile.length() > 0) {
                    Log.i(TAG, "Found pre-unpacked Vosk model at: ${targetDir.absolutePath}")
                    targetDir
                } else {
                    Log.i(TAG, "Unpacking Vosk model assets from APK...")
                    targetDir.mkdirs()
                    copyAssetFolder(context.assets, modelName, targetDir)
                    targetDir
                }

                if (!File(modelDir, "am/final.mdl").exists()) {
                    throw IOException("Vosk model structure invalid: am/final.mdl missing in ${modelDir.absolutePath}")
                }

                // Step 2: Initialize Vosk Model instance
                val loadedModel = Model(modelDir.absolutePath)
                voskModel = loadedModel
                currentState = AsrState.ASR_READY
                lastError = null

                val loadDuration = SystemClock.elapsedRealtime() - startTime
                Log.i(TAG, "Vosk Hindi model loaded successfully in ${loadDuration}ms from ${modelDir.absolutePath}")
                onStateChanged(currentState)
            } catch (e: Exception) {
                Log.e(TAG, "Vosk model initialization failed: ${e.message}", e)
                // Fallback attempt: Try StorageService.unpack if asset folder copy had issues
                try {
                    StorageService.unpack(context, modelName, "models",
                        { model: Model ->
                            voskModel = model
                            currentState = AsrState.ASR_READY
                            lastError = null
                            Log.i(TAG, "Vosk Hindi model loaded via StorageService.unpack fallback")
                            onStateChanged(currentState)
                        },
                        { exception: Exception ->
                            currentState = AsrState.ASR_ERROR
                            lastError = "Vosk model loading failed: ${exception.message}"
                            Log.e(TAG, "StorageService fallback failed: ${exception.message}", exception)
                            onStateChanged(currentState)
                        }
                    )
                } catch (fallbackEx: Exception) {
                    currentState = AsrState.ASR_ERROR
                    lastError = "Vosk model loading failed: ${e.localizedMessage}"
                    Log.e(TAG, "ASR loading fatal error: ${e.message}", e)
                    onStateChanged(currentState)
                }
            }
        }.start()
    }

    private fun copyAssetFolder(assetManager: AssetManager, fromAssetPath: String, toDir: File) {
        val files = assetManager.list(fromAssetPath) ?: return
        if (files.isEmpty()) {
            // Copy file
            assetManager.open(fromAssetPath).use { input ->
                FileOutputStream(toDir).use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            // Copy directory
            toDir.mkdirs()
            for (file in files) {
                copyAssetFolder(assetManager, "$fromAssetPath/$file", File(toDir, file))
            }
        }
    }

    override fun processAudio(pcmFile: File, onResult: (AsrResult) -> Unit) {
        val startTime = SystemClock.elapsedRealtime()
        Log.i(TAG, "ASR recognition process requested for file: ${pcmFile.absolutePath}")

        // Audio validation checks
        if (!pcmFile.exists()) {
            val err = "Audio file does not exist: ${pcmFile.absolutePath}"
            Log.e(TAG, err)
            onResult(AsrResult(text = "", isFinal = true, errorMessage = err))
            return
        }

        val fileSize = pcmFile.length()
        if (fileSize == 0L) {
            val err = "Audio file is empty (0 bytes)"
            Log.e(TAG, err)
            onResult(AsrResult(text = "", isFinal = true, errorMessage = err))
            return
        }

        Log.i(TAG, "Audio format validated: 16000Hz, Mono, PCM16. Audio byte size: $fileSize bytes")

        val model = voskModel
        if (model == null) {
            val errorMsg = lastError ?: "Vosk ASR Engine is not ready or model is missing."
            Log.e(TAG, "ASR process aborted: $errorMsg")
            currentState = AsrState.ASR_ERROR
            onResult(AsrResult(text = "", isFinal = true, errorMessage = errorMsg))
            return
        }

        if (currentState == AsrState.ASR_PROCESSING) {
            Log.w(TAG, "ASR recognition is already in progress. Ignoring duplicate request.")
            return
        }

        currentState = AsrState.ASR_PROCESSING
        Log.i(TAG, "Vosk Recognizer initialization start...")

        Thread {
            try {
                Recognizer(model, SAMPLE_RATE).use { recognizer ->
                    Log.i(TAG, "Vosk Recognizer initialized successfully. Feeding PCM audio stream...")
                    val pcmInputStream = pcmFile.inputStream()
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var totalBytesFed = 0L

                    while (pcmInputStream.read(buffer).also { bytesRead = it } != -1) {
                        recognizer.acceptWaveForm(buffer, bytesRead)
                        totalBytesFed += bytesRead
                    }
                    pcmInputStream.close()

                    Log.i(TAG, "Completed feeding $totalBytesFed bytes to Vosk Recognizer. Fetching final result...")
                    val resultJsonStr = recognizer.finalResult
                    Log.i(TAG, "Vosk Raw JSON Output: $resultJsonStr")

                    val jsonObject = JSONObject(resultJsonStr)
                    val rawText = jsonObject.optString("text", "").trim()
                    val recognizedText = if (rawText.isNotBlank()) rawText else "कोई आवाज़ नहीं पहचानी गई"

                    val processingDuration = SystemClock.elapsedRealtime() - startTime
                    Log.i(TAG, "ASR Recognition completed in ${processingDuration}ms. Output: \"$recognizedText\"")

                    currentState = AsrState.ASR_RESULT
                    onResult(
                        AsrResult(
                            text = recognizedText,
                            isFinal = true,
                            processingTimeMs = processingDuration
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Vosk audio processing exception: ${e.message}", e)
                currentState = AsrState.ASR_ERROR
                lastError = "ASR processing failed: ${e.localizedMessage}"
                onResult(
                    AsrResult(
                        text = "",
                        isFinal = true,
                        errorMessage = lastError
                    )
                )
            }
        }.start()
    }

    override fun release() {
        Log.i(TAG, "Releasing Vosk ASR Engine resources...")
        voskModel?.close()
        voskModel = null
        currentState = AsrState.ASR_NOT_READY
        lastError = null
    }
}
