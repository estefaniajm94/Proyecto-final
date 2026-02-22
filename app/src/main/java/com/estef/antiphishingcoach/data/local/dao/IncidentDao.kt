package com.estef.antiphishingcoach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.estef.antiphishingcoach.data.local.entity.IncidentEntity
import com.estef.antiphishingcoach.data.local.entity.IncidentWithAnalysisAndSignals
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incident: IncidentEntity): Long

    @Transaction
    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun observeHistory(): Flow<List<IncidentWithAnalysisAndSignals>>

    @Transaction
    @Query("SELECT * FROM incidents WHERE id = :incidentId LIMIT 1")
    fun observeIncidentDetail(incidentId: Long): Flow<IncidentWithAnalysisAndSignals?>

    @Query("DELETE FROM incidents")
    suspend fun clearAll()
}
