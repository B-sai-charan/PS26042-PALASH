package com.sih2026.palash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a bilingual flashcard.
 */
@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val lessonId: Int,
    val frontText: String,
    val backText: String,
    val language: String,
    val version: Int,
    val createdAt: Long = System.currentTimeMillis()
)
