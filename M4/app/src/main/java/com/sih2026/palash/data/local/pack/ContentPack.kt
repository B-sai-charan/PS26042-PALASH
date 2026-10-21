package com.sih2026.palash.data.local.pack

import com.sih2026.palash.data.local.entity.AudioMetadataEntity
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.local.entity.WorksheetEntity

data class ContentPack @JvmOverloads constructor(
    val metadata: LanguagePackMetadataEntity,
    val lessons: List<LessonEntity>,
    val worksheets: List<WorksheetEntity>,
    val translations: List<TranslationCacheEntity>,
    val audios: List<AudioMetadataEntity> = emptyList()
)
