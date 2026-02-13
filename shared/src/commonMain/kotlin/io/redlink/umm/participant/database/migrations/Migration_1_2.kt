package io.redlink.umm.participant.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE schedules ADD COLUMN reminder INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE observations ADD COLUMN reminder INTEGER NOT NULL DEFAULT 0")
    }
}