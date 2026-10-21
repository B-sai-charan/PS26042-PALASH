package com.sih2026.palash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguagePackMetadataDao {
    @Query("SELECT * FROM language_pack_metadata")
    fun getAllLanguagePacks(): Flow<List<LanguagePackMetadataEntity>>

    @Query("SELECT * FROM language_pack_metadata WHERE languageCode = :languageCode LIMIT 1")
    suspend fun getLanguagePack(languageCode: String): LanguagePackMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguagePack(pack: LanguagePackMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguagePacks(packs: List<LanguagePackMetadataEntity>)
}
