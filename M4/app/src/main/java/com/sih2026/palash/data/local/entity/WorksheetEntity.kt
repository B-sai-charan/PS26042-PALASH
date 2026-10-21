package com.sih2026.palash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worksheets")
data class WorksheetEntity(
    @PrimaryKey val id: String,
    val grade: String,
    val subject: String,
    val topic: String,
    val title: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val contentJson: String,
    val version: String,
    val createdAt: Long = System.currentTimeMillis()
)
