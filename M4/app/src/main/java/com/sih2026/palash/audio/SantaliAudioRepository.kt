package com.sih2026.palash.audio

import android.content.Context
import android.util.Log

/**
 * Data model for Santali Audio Metadata as defined in the M3 manifest.
 * Facts: 22050 Hz, mono, 16-bit PCM, Pre-rendered M3 Piper-generated Santali reference audio.
 */
data class SantaliAudioMetadata(
    val audioId: String,
    val relativeAssetPath: String,
    val category: String,
    val grade: Int = 2,
    val sampleRate: Int = 22050,
    val channels: Int = 1,
    val bitsPerSample: Int = 16,
    val generationMethod: String = "Piper-generated",
    val validationStatus: String = "UNVERIFIED"
)

/**
 * Clean abstraction for resolving and verifying Santali audio assets bundled in assets.
 */
interface SantaliAudioRepository {
    fun getAudioMetadata(audioId: String): SantaliAudioMetadata?
    fun getAssetPathForAudio(audioId: String): String?
    fun hasAudioAsset(audioId: String): Boolean
    fun listGrade2AudioAssets(): List<SantaliAudioMetadata>
}

class SantaliAudioRepositoryImpl(
    private val context: Context
) : SantaliAudioRepository {

    companion object {
        private const val TAG = "SantaliAudioRepository"
        private const val BASE_ASSET_DIR = "audio/santali/g2_counting"
    }

    private val assetMap = mutableMapOf<String, SantaliAudioMetadata>()

    init {
        scanAssets()
    }

    private fun scanAssets() {
        val baseDirs = listOf("audio/santali/g2_counting" to 2, "audio/santali/g3_counting" to 3)
        val categories = listOf("numbers", "worksheet", "flashcards", "assessment", "lesson")

        for ((baseDir, gradeVal) in baseDirs) {
            for (category in categories) {
                val dirPath = "$baseDir/$category"
                try {
                    val files = context.assets.list(dirPath) ?: emptyArray()
                    for (file in files) {
                        if (file.endsWith(".wav")) {
                            val audioId = file.removeSuffix(".wav")
                            val fullAssetPath = "$dirPath/$file"
                            val meta = SantaliAudioMetadata(
                                audioId = audioId,
                                relativeAssetPath = fullAssetPath,
                                category = category,
                                grade = gradeVal,
                                sampleRate = 22050,
                                channels = 1,
                                bitsPerSample = 16,
                                generationMethod = "Piper-generated",
                                validationStatus = "UNVERIFIED"
                            )
                            assetMap[audioId] = meta
                            assetMap[file] = meta // Also map exact filename

                            // Register deterministic alias mappings
                            if (audioId.endsWith("_question")) {
                                val baseId = audioId.removeSuffix("_question")
                                assetMap[baseId] = meta
                            }
                            if (audioId.startsWith("L2_")) {
                                val secId = audioId.removePrefix("L2_")
                                assetMap[secId] = meta
                            }
                            if (audioId.startsWith("number_")) {
                                val numStr = audioId.removePrefix("number_")
                                assetMap[numStr] = meta
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not list assets in $dirPath: ${e.message}")
                }
            }
        }
        Log.i(TAG, "Indexed ${assetMap.size} Santali audio assets across Grade 2 and Grade 3.")
    }

    override fun getAudioMetadata(audioId: String): SantaliAudioMetadata? {
        return assetMap[audioId]
    }

    override fun getAssetPathForAudio(audioId: String): String? {
        return assetMap[audioId]?.relativeAssetPath
    }

    override fun hasAudioAsset(audioId: String): Boolean {
        val path = getAssetPathForAudio(audioId) ?: return false
        return try {
            val afd = context.assets.openFd(path)
            afd.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun listGrade2AudioAssets(): List<SantaliAudioMetadata> {
        return assetMap.values.distinctBy { it.audioId }
    }
}
