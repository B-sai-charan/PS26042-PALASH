package com.sih2026.palash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LanguagePackStatus {
    NOT_INSTALLED,
    INSTALLED,
    UPDATE_AVAILABLE,
    INVALID
}

@Entity(tableName = "language_pack_metadata")
data class LanguagePackMetadataEntity(
    @PrimaryKey val languageCode: String,
    val languageName: String,
    val script: String = "Ol Chiki / Devanagari",
    val version: String,
    val status: String = LanguagePackStatus.INSTALLED.name,
    val checksum: String,
    val installedAt: Long = System.currentTimeMillis()
)
