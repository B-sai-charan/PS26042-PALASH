package com.sih2026.palash.data.repository

import com.sih2026.palash.data.local.dao.LanguagePackMetadataDao
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import kotlinx.coroutines.flow.Flow

interface LanguagePackRepository {
    fun getAllLanguagePacks(): Flow<List<LanguagePackMetadataEntity>>
    suspend fun getLanguagePack(languageCode: String): LanguagePackMetadataEntity?
    suspend fun insertLanguagePack(pack: LanguagePackMetadataEntity)
    suspend fun insertLanguagePacks(packs: List<LanguagePackMetadataEntity>)
}

class LanguagePackRepositoryImpl(
    private val languagePackMetadataDao: LanguagePackMetadataDao
) : LanguagePackRepository {
    override fun getAllLanguagePacks(): Flow<List<LanguagePackMetadataEntity>> =
        languagePackMetadataDao.getAllLanguagePacks()

    override suspend fun getLanguagePack(languageCode: String): LanguagePackMetadataEntity? =
        languagePackMetadataDao.getLanguagePack(languageCode)

    override suspend fun insertLanguagePack(pack: LanguagePackMetadataEntity) =
        languagePackMetadataDao.insertLanguagePack(pack)

    override suspend fun insertLanguagePacks(packs: List<LanguagePackMetadataEntity>) =
        languagePackMetadataDao.insertLanguagePacks(packs)
}
