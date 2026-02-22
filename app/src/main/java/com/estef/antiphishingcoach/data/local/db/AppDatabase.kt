package com.estef.antiphishingcoach.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.estef.antiphishingcoach.data.local.converter.StringListConverter
import com.estef.antiphishingcoach.data.local.dao.AnalysisResultDao
import com.estef.antiphishingcoach.data.local.dao.DetectedSignalDao
import com.estef.antiphishingcoach.data.local.dao.IncidentDao
import com.estef.antiphishingcoach.data.local.entity.AnalysisResultEntity
import com.estef.antiphishingcoach.data.local.entity.DetectedSignalEntity
import com.estef.antiphishingcoach.data.local.entity.IncidentEntity

@Database(
    entities = [
        IncidentEntity::class,
        AnalysisResultEntity::class,
        DetectedSignalEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun analysisResultDao(): AnalysisResultDao
    abstract fun detectedSignalDao(): DetectedSignalDao

    companion object {
        private const val DB_NAME = "antiphishing_coach.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
