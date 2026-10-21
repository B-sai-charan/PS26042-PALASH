package com.sih2026.palash.data.local.seeder

import com.sih2026.palash.data.local.db.PalashDatabase
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LanguagePackStatus
import com.sih2026.palash.data.local.pack.ContentPackManager
import com.sih2026.palash.data.local.pack.DemoContentPackFactory

object DatabaseSeeder {
    suspend fun seedDatabase(database: PalashDatabase) {
        val manager = ContentPackManager(database)

        // Seed Hindi metadata
        val hindiMetadata = LanguagePackMetadataEntity(
            languageCode = "hi",
            languageName = "Hindi",
            script = "Devanagari",
            version = "1.0-built-in",
            status = LanguagePackStatus.INSTALLED.name,
            checksum = "builtin-checksum-hi-v1.0"
        )
        database.languagePackMetadataDao().insertLanguagePack(hindiMetadata)

        // Seed Santali 0.1-demo pack atomically
        val santaliDemoPack = DemoContentPackFactory.createSantaliDemoPack()
        manager.installPackAtomically(santaliDemoPack)
    }
}
