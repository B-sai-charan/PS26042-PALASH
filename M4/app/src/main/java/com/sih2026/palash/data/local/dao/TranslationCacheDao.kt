package com.sih2026.palash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationCacheDao {
    @Query("SELECT * FROM translation_cache WHERE sourceText = :sourceText AND sourceLanguage = :sourceLanguage AND targetLanguage = :targetLanguage LIMIT 1")
    suspend fun findTranslation(sourceText: String, sourceLanguage: String, targetLanguage: String): TranslationCacheEntity?

    @Query("SELECT * FROM translation_cache ORDER BY createdAt DESC")
    fun getAllTranslations(): Flow<List<TranslationCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationCacheEntity>)

    @Query("DELETE FROM translation_cache")
    suspend fun clearCache()
}
