package com.shg.mahilashaktiunnati.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemberEntity::class,
        SavingsEntryEntity::class,
        LoanEntity::class,
        LoanRepaymentEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shgDao(): ShgDao

    companion object {
        private const val DB_NAME = "mahila_shakti_unnati.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(LegacyMigrations.migration1To2)
                .build()
        }
    }
}
