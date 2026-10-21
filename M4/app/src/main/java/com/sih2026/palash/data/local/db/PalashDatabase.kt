package com.sih2026.palash.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sih2026.palash.data.local.dao.AudioMetadataDao
import com.sih2026.palash.data.local.dao.FlashcardDao
import com.sih2026.palash.data.local.dao.LanguagePackMetadataDao
import com.sih2026.palash.data.local.dao.LessonDao
import com.sih2026.palash.data.local.dao.TranslationCacheDao
import com.sih2026.palash.data.local.dao.WorksheetDao
import com.sih2026.palash.data.local.entity.AudioMetadataEntity
import com.sih2026.palash.data.local.entity.FlashcardEntity
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.local.entity.WorksheetEntity

@Database(
    entities = [
        LessonEntity::class,
        TranslationCacheEntity::class,
        WorksheetEntity::class,
        LanguagePackMetadataEntity::class,
        AudioMetadataEntity::class,
        FlashcardEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PalashDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun translationCacheDao(): TranslationCacheDao
    abstract fun worksheetDao(): WorksheetDao
    abstract fun languagePackMetadataDao(): LanguagePackMetadataDao
    abstract fun audioMetadataDao(): AudioMetadataDao
    abstract fun flashcardDao(): FlashcardDao
}
