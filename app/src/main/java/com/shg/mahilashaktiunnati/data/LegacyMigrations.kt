package com.shg.mahilashaktiunnati.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object LegacyMigrations {
    val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE loans ADD COLUMN isClosed INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
}
