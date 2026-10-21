package com.sih2026.palash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sih2026.palash.translation.ValidationStatus

@Entity(tableName = "translation_cache")
data class TranslationCacheEntity(
    @PrimaryKey val id: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val sourceText: String,
    val targetText: String?,
    val script: String = "Ol Chiki",
    val validationStatus: String = ValidationStatus.UNVERIFIED.name,
    val validatorType: String? = null,
    val validatorNameOrId: String? = null,
    val validationDate: Long = 0L,
    val sourceReference: String? = null,
    val license: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
