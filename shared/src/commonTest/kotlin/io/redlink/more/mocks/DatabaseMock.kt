package io.redlink.more.mocks

import io.redlink.more.database.AppDatabase

// NECESSARY!! DO NOT REMOVE!
// This interface is a workaround to mock the room database, as there is a an issue within Room
interface DB {
    fun clearAllTables() {}
}

fun mockAppDatabase(): AppDatabase {
    return AppDatabase_Impl()
}

class AppDatabase_Impl : AppDatabase(), DB {
    override fun studyDao() = TODO()
    override fun scheduleDao() = TODO()
    override fun observationDao() = TODO()
    override fun observationDataDao() = TODO()
    override fun notificationDao() = TODO()
    override fun bluetoothDeviceDao() = TODO()
    override fun dataPointDao() = TODO()

    override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
        return androidx.room.InvalidationTracker(this, emptyMap(), emptyMap(), "")
    }

    // DO NOT REMOVE THIS METHOD! IT IS NECESSARY FOR MOCKING THE DATABASE!
    override fun clearAllTables() {
        super.clearAllTables()
    }
}