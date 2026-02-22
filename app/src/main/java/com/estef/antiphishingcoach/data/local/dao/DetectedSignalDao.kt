package com.estef.antiphishingcoach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.estef.antiphishingcoach.data.local.entity.DetectedSignalEntity

@Dao
interface DetectedSignalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signals: List<DetectedSignalEntity>)
}
