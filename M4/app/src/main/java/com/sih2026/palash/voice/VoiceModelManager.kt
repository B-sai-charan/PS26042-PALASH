package com.sih2026.palash.voice

import android.content.Context
import android.util.Log
import com.sih2026.palash.asr.VoskHindiAsrEngine
import com.sih2026.palash.tts.LivePiperTtsEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Thread-safe singleton model manager for Vosk Hindi ASR and Piper ONNX TTS engines.
 * Reuses initialized model instances across Teacher Voice Input screen entries to eliminate startup lag.
 */
object VoiceModelManager {
    private const val TAG = "VoiceModelManager"

    @Volatile
    private var piperEngine: LivePiperTtsEngine? = null

    @Volatile
    private var voskEngine: VoskHindiAsrEngine? = null

    @Synchronized
    fun getPiperEngine(context: Context): LivePiperTtsEngine {
        var engine = piperEngine
        if (engine == null) {
            Log.i(TAG, "Creating singleton LivePiperTtsEngine instance...")
            engine = LivePiperTtsEngine(context.applicationContext)
            piperEngine = engine
        }
        return engine
    }

    @Synchronized
    fun getVoskEngine(): VoskHindiAsrEngine {
        var engine = voskEngine
        if (engine == null) {
            Log.i(TAG, "Creating singleton VoskHindiAsrEngine instance...")
            engine = VoskHindiAsrEngine()
            voskEngine = engine
        }
        return engine
    }

    fun warmUp(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Pre-warming Voice ASR & TTS models on Dispatchers.IO...")
                getPiperEngine(context)
                val vosk = getVoskEngine()
                vosk.initialize(context.applicationContext, "vosk-model-small-hi-0.22") { _ -> }
            } catch (e: Exception) {
                Log.w(TAG, "VoiceModelManager warmUp warning: ${e.message}")
            }
        }
    }
}
