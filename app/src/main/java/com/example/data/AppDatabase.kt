package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, Vehicle::class, FuelRecord::class, ExpenseRecord::class, Reminder::class, MaintenanceRecord::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun recordDao(): RecordDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN garageName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN cityId TEXT NOT NULL DEFAULT 'ist_avr'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arac_karnesi_db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
