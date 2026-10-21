package com.sih2026.palash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.palash.data.local.entity.WorksheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorksheetDao {
    @Query("SELECT * FROM worksheets ORDER BY createdAt DESC")
    fun getAllWorksheets(): Flow<List<WorksheetEntity>>

    @Query("SELECT * FROM worksheets WHERE grade = :grade AND subject = :subject AND topic = :topic")
    fun getWorksheetsByTopic(grade: String, subject: String, topic: String): Flow<List<WorksheetEntity>>

    @Query("SELECT * FROM worksheets WHERE id = :id LIMIT 1")
    suspend fun getWorksheetById(id: String): WorksheetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorksheet(worksheet: WorksheetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorksheets(worksheets: List<WorksheetEntity>)

    @Query("DELETE FROM worksheets")
    suspend fun clearWorksheets()
}
