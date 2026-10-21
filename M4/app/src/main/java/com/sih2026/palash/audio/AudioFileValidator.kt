package com.sih2026.palash.audio

import com.sih2026.palash.data.local.entity.AudioMetadataEntity
import com.sih2026.palash.translation.ValidationStatus
import java.io.File
import java.security.MessageDigest

/**
 * Validation helper for native audio assets.
 * Rejects missing files, unverified recordings, checksum mismatches, or translation ID alignment errors.
 */
object AudioFileValidator {

    fun isValidAudioFile(
        audioMetadata: AudioMetadataEntity?,
        targetTranslationId: String
    ): Boolean {
        if (audioMetadata == null) return false

        // 1. Check translation ID alignment
        if (audioMetadata.translationId != targetTranslationId) return false

        // 2. Check validation status gating
        val isApproved = audioMetadata.validationStatus == ValidationStatus.TEACHER_VERIFIED.name ||
                audioMetadata.validationStatus == ValidationStatus.OFFICIAL_SOURCE.name
        if (!isApproved) return false

        // 3. Check audio file path
        val path = audioMetadata.audioPath ?: return false
        val file = File(path)

        // 4. Check file existence and non-empty size
        if (!file.exists() || file.length() <= 0L) return false

        // 5. Check SHA-256 checksum if provided
        val expectedSha = audioMetadata.sha256
        if (!expectedSha.isNullOrBlank()) {
            val calculatedSha = calculateSha256(file)
            if (!calculatedSha.equals(expectedSha, ignoreCase = true)) {
                return false
            }
        }

        return true
    }

    fun calculateSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = file.readBytes()
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
