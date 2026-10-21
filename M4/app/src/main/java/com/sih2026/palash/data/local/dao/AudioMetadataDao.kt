package com.sih2026.palash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.palash.data.local.entity.AudioMetadataEntity

@Dao
interface AudioMetadataDao {
    @Query("SELECT * FROM audio_metadata WHERE translationId = :translationId LIMIT 1")
    suspend fun findAudioForTranslation(translationId: String): AudioMetadataEntity?

    @Query("SELECT * FROM audio_metadata WHERE id = :id LIMIT 1")
    suspend fun getAudioById(id: String): AudioMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: AudioMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudios(audios: List<AudioMetadataEntity>)

    @Query("DELETE FROM audio_metadata")
    suspend fun clearAll()
}
