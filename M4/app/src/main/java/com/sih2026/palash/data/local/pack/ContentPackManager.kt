package com.sih2026.palash.data.local.pack

import androidx.room.withTransaction
import com.sih2026.palash.data.local.db.PalashDatabase
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LanguagePackStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContentPackManager(
    private val database: PalashDatabase
) {
    fun getAllPacksFlow(): Flow<List<LanguagePackMetadataEntity>> =
        database.languagePackMetadataDao().getAllLanguagePacks()

    fun getPackStatusFlow(languageCode: String): Flow<LanguagePackStatus> =
        getAllPacksFlow().map { packs ->
            val pack = packs.find { it.languageCode == languageCode }
            if (pack != null) {
                try {
                    LanguagePackStatus.valueOf(pack.status)
                } catch (e: Exception) {
                    LanguagePackStatus.INVALID
                }
            } else {
                LanguagePackStatus.NOT_INSTALLED
            }
        }

    suspend fun getInstalledVersion(languageCode: String): String? {
        val pack = database.languagePackMetadataDao().getLanguagePack(languageCode)
        return pack?.version
    }

    suspend fun isPackInstalled(languageCode: String): Boolean {
        val pack = database.languagePackMetadataDao().getLanguagePack(languageCode)
        return pack != null && pack.status == LanguagePackStatus.INSTALLED.name
    }

    /**
     * Atomically installs or updates a ContentPack.
     * All metadata, lessons, worksheets, and translation cache entries are written
     * inside a single Room database transaction. If any step fails, the entire
     * transaction is rolled back and previous state remains intact.
     */
    suspend fun installPackAtomically(pack: ContentPack): Boolean {
        return try {
            database.withTransaction {
                // Validate metadata checksum before proceeding
                if (pack.metadata.checksum.isBlank()) {
                    throw IllegalArgumentException("Invalid language pack checksum")
                }

                database.languagePackMetadataDao().insertLanguagePack(pack.metadata)
                if (pack.lessons.isNotEmpty()) {
                    database.lessonDao().insertLessons(pack.lessons)
                }
                if (pack.worksheets.isNotEmpty()) {
                    database.worksheetDao().insertWorksheets(pack.worksheets)
                }
                if (pack.translations.isNotEmpty()) {
                    database.translationCacheDao().insertTranslations(pack.translations)
                }
                if (pack.audios.isNotEmpty()) {
                    database.audioMetadataDao().insertAudios(pack.audios)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Resets or reinstalls the Santali demo pack atomically.
     */
    suspend fun resetDemoPack(): Boolean {
        val demoPack = DemoContentPackFactory.createSantaliDemoPack()
        return installPackAtomically(demoPack)
    }

    /**
     * Removes a language pack metadata entry atomically.
     */
    suspend fun removePackMetadata(languageCode: String): Boolean {
        return try {
            database.withTransaction {
                val pack = database.languagePackMetadataDao().getLanguagePack(languageCode)
                if (pack != null) {
                    val updated = pack.copy(status = LanguagePackStatus.NOT_INSTALLED.name)
                    database.languagePackMetadataDao().insertLanguagePack(updated)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
