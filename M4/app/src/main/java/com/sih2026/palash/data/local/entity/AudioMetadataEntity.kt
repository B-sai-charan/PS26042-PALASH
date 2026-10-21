package com.sih2026.palash.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sih2026.palash.translation.ValidationStatus

/**
 * Entity representing metadata for verified native audio recordings linked to specific translations.
 * Privacy-safe: validatorNameOrId contains pseudo-IDs (e.g. T_JH_042), never personal contact details.
 */
@Entity(
    tableName = "audio_metadata",
    foreignKeys = [
        ForeignKey(
            entity = TranslationCacheEntity::class,
            parentColumns = ["id"],
            childColumns = ["translationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["translationId"])]
)
data class AudioMetadataEntity(
    @PrimaryKey val id: String,
    val translationId: String,
    val language: String = "sat",
    val script: String = "Ol Chiki",
    val audioPath: String?,
    val audioFormat: String = "wav",
    val sampleRateHz: Int = 16000,
    val channels: Int = 1,
    val durationMs: Long = 0L,
    val validationStatus: String = ValidationStatus.UNVERIFIED.name,
    val validatorType: String? = null,
    val validatorNameOrId: String? = null,
    val validationDate: Long = 0L,
    val sourceReference: String? = null,
    val license: String? = null,
    val sha256: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
