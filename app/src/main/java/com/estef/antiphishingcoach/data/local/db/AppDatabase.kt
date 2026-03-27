package com.estef.antiphishingcoach.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.estef.antiphishingcoach.data.local.converter.StringListConverter
import com.estef.antiphishingcoach.data.local.dao.AnalysisResultDao
import com.estef.antiphishingcoach.data.local.dao.DetectedSignalDao
import com.estef.antiphishingcoach.data.local.dao.IncidentDao
import com.estef.antiphishingcoach.data.local.dao.UserDao
import com.estef.antiphishingcoach.data.local.entity.AnalysisResultEntity
import com.estef.antiphishingcoach.data.local.entity.DetectedSignalEntity
import com.estef.antiphishingcoach.data.local.entity.IncidentEntity
import com.estef.antiphishingcoach.data.local.entity.UserEntity

@Database(
    entities = [
        IncidentEntity::class,
        AnalysisResultEntity::class,
        DetectedSignalEntity::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun analysisResultDao(): AnalysisResultDao
    abstract fun detectedSignalDao(): DetectedSignalDao
    abstract fun userDao(): UserDao

    companion object {
        private const val DB_NAME = "antiphishing_coach.db"
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN avatarId TEXT NOT NULL DEFAULT 'avatar_default'"
                )
            }
        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
