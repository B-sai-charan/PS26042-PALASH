package com.sih2026.palash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val grade: String,
    val subject: String,
    val topic: String,
    val title: String,
    val languageCode: String,
    val contentJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
