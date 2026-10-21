package com.sih2026.palash.tts

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.util.UUID

/**
 * State lifecycle enum for Live Piper TTS Engine.
 */
enum class LiveTtsState {
    IDLE,
    PROCESSING,
    READY,
    PLAYING,
    ERROR,
    UNAVAILABLE
}

/**
 * Detailed result wrapper for Live Piper TTS operations.
 */
data class LiveTtsResult(
    val text: String,
    val romanizedText: String,
    val audioFile: File? = null,
    val durationMs: Long = 0L,
    val modelLoadTimeMs: Long = 0L,
    val phonemizeTimeMs: Long = 0L,
    val inferenceTimeMs: Long = 0L,
    val wavGenTimeMs: Long = 0L,
    val totalTtsTimeMs: Long = 0L,
    val state: LiveTtsState,
    val errorMessage: String? = null
)

/**
 * Real Experimental Live Piper TTS Engine using ONNX Runtime Android.
 * Executes en_IN-spicor-medium.onnx model on device (arm64-v8a).
 */
class LivePiperTtsEngine(
    private val context: Context,
    private val modelAssetPath: String = "models/piper/en_IN-spicor-medium.onnx",
    private val jsonAssetPath: String = "models/piper/en_IN-spicor-medium.onnx.json"
) : TtsEngine {

    companion object {
        private const val TAG = "LivePiperTtsEngine"
        private const val SAMPLE_RATE = 22050
    }

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private val phonemeIdMap = mutableMapOf<String, IntArray>()
    private var isInitialized = false
    private var initErrorMessage: String? = null
    var modelLoadTimeMs: Long = 0L
        private set

    fun isReady(): Boolean = isInitialized && ortSession != null && ortEnv != null

    suspend fun awaitReady(timeoutMs: Long = 15000L): Boolean {
        if (isReady()) return true
        val start = System.currentTimeMillis()
        while (!isReady() && initErrorMessage == null && (System.currentTimeMillis() - start) < timeoutMs) {
            kotlinx.coroutines.delay(100)
        }
        return isReady()
    }

    init {
        Thread {
            initializeEngine()
        }.start()
    }

    private fun initializeEngine() {
        val startTime = System.currentTimeMillis()
        try {
            Log.i(TAG, "Initializing ONNX Runtime and loading Piper model...")

            // Parse JSON config
            val jsonStr = context.assets.open(jsonAssetPath).bufferedReader().use { it.readText() }
            val jsonObj = JSONObject(jsonStr)
            val phonemeMapObj = jsonObj.getJSONObject("phoneme_id_map")

            val keys = phonemeMapObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = phonemeMapObj.getJSONArray(k)
                val ids = IntArray(arr.length()) { i -> arr.getInt(i) }
                phonemeIdMap[k] = ids
            }

            // Copy model file from assets to app private storage for ONNX Runtime
            val modelDir = File(context.filesDir, "models/piper")
            modelDir.mkdirs()
            val modelFile = File(modelDir, "en_IN-spicor-medium.onnx")

            if (!modelFile.exists() || modelFile.length() == 0L) {
                Log.i(TAG, "Copying Piper ONNX model from assets to ${modelFile.absolutePath}...")
                context.assets.open(modelAssetPath).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            val session = env.createSession(modelFile.absolutePath, sessionOptions)

            ortEnv = env
            ortSession = session
            isInitialized = true
            modelLoadTimeMs = System.currentTimeMillis() - startTime
            Log.i(TAG, "Piper ONNX model loaded successfully in ${modelLoadTimeMs}ms")
        } catch (e: Exception) {
            initErrorMessage = "Piper TTS model load error: ${e.localizedMessage}"
            Log.e(TAG, initErrorMessage, e)
            isInitialized = false
        }
    }

    suspend fun synthesizeLive(santaliText: String): LiveTtsResult {
        val totalStart = System.currentTimeMillis()

        if (!isReady()) {
            awaitReady(15000L)
        }

        if (!isReady()) {
            return LiveTtsResult(
                text = santaliText,
                romanizedText = "",
                state = LiveTtsState.UNAVAILABLE,
                errorMessage = initErrorMessage ?: "Piper TTS engine not initialized",
                totalTtsTimeMs = System.currentTimeMillis() - totalStart
            )
        }

        val phonStart = System.currentTimeMillis()
        val romanText = SantaliOlChikiTransliteration.toRoman(santaliText)
        val phonemeIds = textToPhonemeIds(romanText)
        val phonTime = System.currentTimeMillis() - phonStart

        if (phonemeIds.isEmpty()) {
            return LiveTtsResult(
                text = santaliText,
                romanizedText = romanText,
                state = LiveTtsState.ERROR,
                errorMessage = "Phonemization failed for input text",
                totalTtsTimeMs = System.currentTimeMillis() - totalStart
            )
        }

        val session = ortSession!!
        val env = ortEnv!!

        try {
            val infStart = System.currentTimeMillis()

            val numPhonemes = phonemeIds.size.toLong()
            val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(phonemeIds), longArrayOf(1, numPhonemes))
            val lengthTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(longArrayOf(numPhonemes)), longArrayOf(1))
            val scalesTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(floatArrayOf(0.667f, LiveTtsConfig.PIPER_LENGTH_SCALE, 0.8f)), longArrayOf(3))

            val inputs = mapOf(
                "input" to inputTensor,
                "input_lengths" to lengthTensor,
                "scales" to scalesTensor
            )

            val results = session.run(inputs)
            val infTime = System.currentTimeMillis() - infStart

            val outputTensor = results[0] as OnnxTensor
            val floatBuffer = outputTensor.floatBuffer
            val sampleCount = floatBuffer.remaining()
            val samples = FloatArray(sampleCount)
            floatBuffer.get(samples)

            inputTensor.close()
            lengthTensor.close()
            scalesTensor.close()
            results.close()

            val wavStart = System.currentTimeMillis()
            val outputFile = File(context.cacheDir, "piper_live_${UUID.randomUUID()}.wav")
            writeWavFile(outputFile, samples, SAMPLE_RATE)
            val wavTime = System.currentTimeMillis() - wavStart

            val durationMs = (sampleCount * 1000L) / SAMPLE_RATE
            val totalTime = System.currentTimeMillis() - totalStart

            Log.i(TAG, "Piper live synthesis succeeded: text=\"$santaliText\", roman=\"$romanText\", audioFile=${outputFile.name}, infTime=${infTime}ms, totalTime=${totalTime}ms")

            return LiveTtsResult(
                text = santaliText,
                romanizedText = romanText,
                audioFile = outputFile,
                durationMs = durationMs,
                modelLoadTimeMs = modelLoadTimeMs,
                phonemizeTimeMs = phonTime,
                inferenceTimeMs = infTime,
                wavGenTimeMs = wavTime,
                totalTtsTimeMs = totalTime,
                state = LiveTtsState.READY
            )
        } catch (e: Exception) {
            Log.e(TAG, "Piper ONNX inference failed: ${e.message}", e)
            return LiveTtsResult(
                text = santaliText,
                romanizedText = romanText,
                state = LiveTtsState.ERROR,
                errorMessage = "Piper inference error: ${e.localizedMessage}",
                totalTtsTimeMs = System.currentTimeMillis() - totalStart
            )
        }
    }

    override suspend fun synthesize(text: String, language: String, script: String): TtsResult {
        val liveResult = synthesizeLive(text)
        return TtsResult(
            text = text,
            language = language,
            script = script,
            audioFile = liveResult.audioFile,
            durationMs = liveResult.durationMs,
            processingTimeMs = liveResult.totalTtsTimeMs,
            state = when (liveResult.state) {
                LiveTtsState.READY -> TtsState.READY
                LiveTtsState.UNAVAILABLE -> TtsState.UNAVAILABLE
                LiveTtsState.ERROR -> TtsState.ERROR
                else -> TtsState.UNAVAILABLE
            },
            errorMessage = liveResult.errorMessage
        )
    }

    private fun textToPhonemeIds(text: String): LongArray {
        val ids = mutableListOf<Long>()
        val startId = phonemeIdMap["^"]?.get(0)?.toLong() ?: 1L
        val padId = phonemeIdMap["_"]?.get(0)?.toLong() ?: 0L
        val endId = phonemeIdMap["$"]?.get(0)?.toLong() ?: 2L

        ids.add(startId)
        for (ch in text.lowercase()) {
            val key = ch.toString()
            val mapped = phonemeIdMap[key]
            if (mapped != null) {
                for (id in mapped) {
                    ids.add(id.toLong())
                }
                ids.add(padId)
            }
        }
        ids.add(endId)
        return ids.toLongArray()
    }

    private fun writeWavFile(file: File, samples: FloatArray, sampleRate: Int) {
        var maxAmp = 0.0f
        for (s in samples) {
            val abs = kotlin.math.abs(s)
            if (abs > maxAmp) maxAmp = abs
        }
        val gain = if (maxAmp > 0.95f) 0.95f / maxAmp else 1.0f

        val pcmBytes = ByteArray(samples.size * 2)
        for (i in samples.indices) {
            val s = (samples[i] * gain * 32767.0f).coerceIn(-32768.0f, 32767.0f).toInt().toShort()
            pcmBytes[i * 2] = (s.toInt() and 0xFF).toByte()
            pcmBytes[i * 2 + 1] = ((s.toInt() shr 8) and 0xFF).toByte()
        }

        val totalDataLen = pcmBytes.size + 36
        val byteRate = sampleRate * 2

        FileOutputStream(file).use { out ->
            // RIFF header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(totalDataLen))
            out.write("WAVE".toByteArray())
            // fmt chunk
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // Subchunk1Size
            out.write(shortToByteArray(1.toShort())) // AudioFormat 1 = PCM
            out.write(shortToByteArray(1.toShort())) // NumChannels 1 = Mono
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray(2.toShort())) // BlockAlign
            out.write(shortToByteArray(16.toShort())) // BitsPerSample 16
            // data chunk
            out.write("data".toByteArray())
            out.write(intToByteArray(pcmBytes.size))
            out.write(pcmBytes)
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }

    override fun release() {
        try {
            ortSession?.close()
            ortSession = null
            ortEnv?.close()
            ortEnv = null
            isInitialized = false
            Log.i(TAG, "Piper ONNX session released successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing ONNX resources: ${e.message}")
        }
    }
}
