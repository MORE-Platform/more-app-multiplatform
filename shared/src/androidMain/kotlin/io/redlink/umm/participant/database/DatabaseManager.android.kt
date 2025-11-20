package io.redlink.umm.participant.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("more_app.db")
    return Room.databaseBuilder(
        context = appContext,
        name = dbFile.absolutePath
    )
}
