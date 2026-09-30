package com.smashedtatoes.stepcount.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `hourly_step_records` (`date` TEXT NOT NULL, `hour` INTEGER NOT NULL, `steps` INTEGER NOT NULL, PRIMARY KEY(`date`, `hour`))"
        )
    }
}

/**
 * Room Database for Step Counting.
 *
 * CRITICAL ARCHITECTURAL REQUIREMENT:
 * Any future database schema changes (adding/modifying tables or columns):
 * 1. Increment the database `version` in @Database.
 * 2. ALWAYS write an explicit [Migration] object (e.g. MIGRATION_2_3).
 * 3. Register the new migration in [getInstance] via `.addMigrations(...)`.
 * 4. NEVER use `fallbackToDestructiveMigration()` as it will drop user step history!
 */
@Database(entities = [StepRecord::class, HourlyStepRecord::class], version = 2, exportSchema = false)
abstract class StepDatabase : RoomDatabase() {

    abstract fun stepDao(): StepDao

    companion object {
        @Volatile
        private var INSTANCE: StepDatabase? = null

        fun getInstance(context: Context): StepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StepDatabase::class.java,
                    "step_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
