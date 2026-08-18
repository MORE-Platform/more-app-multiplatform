package io.redlink.more.mocks

import io.redlink.more.database.AppDatabase
import io.redlink.more.database.dao.AggregatedObservationDataDao
import io.redlink.more.database.dao.BaseDao
import io.redlink.more.database.dao.BluetoothDeviceDao
import io.redlink.more.database.dao.LatestObservationDataDao
import io.redlink.more.database.dao.NotificationDao
import io.redlink.more.database.dao.ObservationDao
import io.redlink.more.database.dao.ObservationDataDao
import io.redlink.more.database.dao.ScheduleDao
import io.redlink.more.database.dao.StudyDao
import io.redlink.more.database.entities.AggregatedObservationDataEntity
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.database.entities.LatestObservationDataEntity
import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// NECESSARY!! DO NOT REMOVE!
// This interface is a workaround to mock the room database, as there is a an issue within Room
interface DB {
    fun clearAllTables() {}
}

fun mockAppDatabase(): AppDatabase_Impl {
    return AppDatabase_Impl()
}

class AppDatabase_Impl : AppDatabase(), DB {
    val studyDao = MockStudyDao()
    val scheduleDao = MockScheduleDao()
    val observationDao = MockObservationDao()
    val observationDataDao = MockObservationDataDao()
    val notificationDao = MockNotificationDao()
    val bluetoothDeviceDao = MockBluetoothDeviceDao()
    val aggregatedObservationDataDao = MockAggregatedObservationDataDao()
    val latestObservationDataDao = MockLatestObservationDataDao()

    override fun studyDao() = studyDao
    override fun scheduleDao() = scheduleDao
    override fun observationDao() = observationDao
    override fun observationDataDao() = observationDataDao
    override fun notificationDao() = notificationDao
    override fun bluetoothDeviceDao() = bluetoothDeviceDao
    override fun aggregatedObservationDataDao() = aggregatedObservationDataDao
    override fun latestObservationDataDao() = latestObservationDataDao
    override fun dataPointDao() = TODO()

    override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
        return androidx.room.InvalidationTracker(this, emptyMap(), emptyMap(), "")
    }

    // DO NOT REMOVE THIS METHOD! IT IS NECESSARY FOR MOCKING THE DATABASE!
    override fun clearAllTables() {
        // Simple implementation for fake
    }
}

open class MockBaseDao<T : Any> : BaseDao<T> {
    val items = mutableListOf<T>()
    var insertCallCount = 0
    var insertAllCallCount = 0
    var updateCallCount = 0
    var deleteCallCount = 0

    override suspend fun insert(entity: T) {
        insertCallCount++
        items.add(entity)
    }

    override suspend fun insertAll(entities: List<T>) {
        insertAllCallCount++
        items.addAll(entities)
    }

    override suspend fun update(entity: T) {
        updateCallCount++
        /* simplistic */
        items.remove(entity)
        items.add(entity)
    }

    override suspend fun updateAll(entities: List<T>) {
        entities.forEach { update(it) }
    }

    override suspend fun delete(entity: T) {
        deleteCallCount++
        items.remove(entity)
    }
}

class MockStudyDao : MockBaseDao<StudyEntity>(), StudyDao {
    private val studyFlow = MutableStateFlow<StudyEntity?>(null)
    var updateStudyStateCallCount = 0
    var deleteAllCallCount = 0

    override suspend fun insert(entity: StudyEntity) {
        super.insert(entity)
        studyFlow.value = entity
    }

    override suspend fun get(): StudyEntity? = items.lastOrNull()
    override fun getFlow(): Flow<StudyEntity?> = studyFlow
    override suspend fun updateStudyState(studyId: String, state: String) {
        updateStudyStateCallCount++
        val study = items.find { it.studyId == studyId }
        if (study != null) {
            val updated = study.copy(state = state)
            items.remove(study)
            items.add(updated)
            studyFlow.value = updated
        }
    }

    override suspend fun deleteById(studyId: String) {
        items.removeAll { it.studyId == studyId }
    }

    override suspend fun deleteAll() {
        deleteAllCallCount++
        items.clear()
        studyFlow.value = null
    }

    override fun getByIdFlow(studyId: String): Flow<StudyEntity?> =
        studyFlow.map { if (it?.studyId == studyId) it else null }

    override suspend fun getById(studyId: String): StudyEntity? =
        items.find { it.studyId == studyId }

    override suspend fun getByActive(active: Boolean): List<StudyEntity> =
        items.filter { it.active == active }

    override fun getByActiveFlow(active: Boolean): Flow<List<StudyEntity>> = TODO()
    override suspend fun getByState(state: String): List<StudyEntity> =
        items.filter { it.state == state }

    override fun getByStateFlow(state: String): Flow<List<StudyEntity>> = TODO()
    override suspend fun getByParticipantId(participantId: Int): List<StudyEntity> =
        items.filter { it.participantId == participantId }

    override suspend fun getActiveStudiesAtTime(timestamp: Long): List<StudyEntity> = TODO()
    override fun getCount(): Flow<Int> = studyFlow.map { if (it != null) 1 else 0 }
    override suspend fun getCountByActive(active: Boolean): Int =
        items.count { it.active == active }
}

class MockObservationDao : MockBaseDao<ObservationEntity>(), ObservationDao {
    override suspend fun insertAll(entities: List<ObservationEntity>) {
        insertAllCallCount++
        entities.forEach { entity ->
            items.removeAll { it.observationId == entity.observationId }
            items.add(entity)
        }
    }

    override suspend fun deleteById(id: String) {
        items.removeAll { it.observationId == id }
    }

    override suspend fun deleteByObservationId(observationId: String) {
        items.removeAll { it.observationId == observationId }
    }

    override suspend fun deleteAll() {
        items.clear()
    }

    override suspend fun getByObservationId(observationId: String): ObservationEntity? =
        items.find { it.observationId == observationId }

    override fun getByObservationIdFlow(observationId: String): Flow<ObservationEntity?> = TODO()

    override suspend fun getAll(): List<ObservationEntity> = items.toList()
    override fun getAllFlow(): Flow<List<ObservationEntity>> = TODO()
    override suspend fun getByObservationType(observationType: String): List<ObservationEntity> =
        items.filter { it.observationType == observationType }

    override fun getByObservationTypeFlow(observationType: String): Flow<List<ObservationEntity>> =
        TODO()

    override suspend fun getByHidden(hidden: Boolean): List<ObservationEntity> = TODO()
    override fun getByHiddenFlow(hidden: Boolean): Flow<List<ObservationEntity>> = TODO()
    override suspend fun getByScheduleLess(scheduleLess: Boolean): List<ObservationEntity> = TODO()
    override fun getByScheduleLessFlow(scheduleLess: Boolean): Flow<List<ObservationEntity>> =
        TODO()

    override suspend fun getByRequired(required: Boolean): List<ObservationEntity> = TODO()
    override fun getByRequiredFlow(required: Boolean): Flow<List<ObservationEntity>> = TODO()
    override suspend fun getByVersion(version: Long): List<ObservationEntity> = TODO()
    override suspend fun getByTimeRange(
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<ObservationEntity> = TODO()

    override fun getByTimeRangeFlow(
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationEntity>> = TODO()

    override suspend fun searchByTitle(searchTerm: String): List<ObservationEntity> = TODO()
    override suspend fun searchByParticipantInfo(searchTerm: String): List<ObservationEntity> =
        TODO()

    override suspend fun getCount(): Int = items.size
    override suspend fun getCountByType(observationType: String): Int =
        items.count { it.observationType == observationType }

    override suspend fun getCountByRequired(required: Boolean): Int =
        items.count { it.required == required }

    override suspend fun getAllObservationTypes(): List<String> =
        items.map { it.observationType }.distinct()

    override suspend fun updateVersion(observationId: String, version: Long) {
        val obs = items.find { it.observationId == observationId }
        if (obs != null) {
            items.remove(obs)
            items.add(obs.copy(version = version))
        }
    }

    override suspend fun updateHidden(observationId: String, hidden: Boolean) {
        val obs = items.find { it.observationId == observationId }
        if (obs != null) {
            items.remove(obs)
            items.add(obs.copy(hidden = hidden))
        }
    }
}

class MockScheduleDao : MockBaseDao<ScheduleEntity>(), ScheduleDao {
    var updateStateCallCount = 0

    private val itemsFlow = MutableStateFlow<List<ScheduleEntity>>(emptyList())

    override suspend fun insert(entity: ScheduleEntity) {
        super.insert(entity)
        itemsFlow.value = items.toList()
    }

    override suspend fun insertAll(entities: List<ScheduleEntity>) {
        insertAllCallCount++
        entities.forEach { entity ->
            items.removeAll { it.scheduleId == entity.scheduleId }
            items.add(entity)
        }
        itemsFlow.value = items.toList()
    }

    override suspend fun deleteAll() {
        items.clear()
        itemsFlow.value = items.toList()
    }

    override suspend fun deleteById(scheduleId: String) {
        items.removeAll { it.scheduleId == scheduleId }
        itemsFlow.value = items.toList()
    }

    override suspend fun deleteByObservationId(observationId: String) {
        items.removeAll { it.observationId == observationId }
        itemsFlow.value = items.toList()
    }

    override fun getById(scheduleId: String): Flow<ScheduleEntity?> =
        itemsFlow.map { list -> list.find { it.scheduleId == scheduleId } }

    override fun getByIdFlow(scheduleId: String): Flow<ScheduleEntity?> =
        itemsFlow.map { list -> list.find { it.scheduleId == scheduleId } }

    override suspend fun getAll(): List<ScheduleEntity> = items.toList()
    override fun getAllFlow(): Flow<List<ScheduleEntity>> = itemsFlow
    override suspend fun getByObservationId(observationId: String): List<ScheduleEntity> =
        items.filter { it.observationId == observationId }

    override fun getByObservationIdFlow(observationId: String): Flow<List<ScheduleEntity>> = TODO()
    override suspend fun getByObservationType(observationType: String): List<ScheduleEntity> =
        TODO()

    override fun getByObservationTypeFlow(observationType: String): Flow<List<ScheduleEntity>> =
        TODO()

    override suspend fun getByDone(done: Boolean): List<ScheduleEntity> =
        items.filter { it.done == done }

    override fun getByDoneFlow(done: Boolean): Flow<List<ScheduleEntity>> = TODO()
    override fun getByStatesFlow(states: List<String>): Flow<List<ScheduleEntity>> = TODO()

    override suspend fun getByHidden(hidden: Boolean): List<ScheduleEntity> = TODO()
    override fun getByHiddenFlow(hidden: Boolean): Flow<List<ScheduleEntity>> = TODO()
    override suspend fun getByState(state: String): List<ScheduleEntity> =
        items.filter { it.state == state }

    override fun getByStateFlow(state: String): Flow<List<ScheduleEntity>> = TODO()
    override fun getSchedulesWithReminder(
        states: List<String>,
        minTimestamp: Long,
        maxTimestamp: Long,
        limit: Int
    ): Flow<List<ScheduleEntity>> = TODO()

    override suspend fun getActiveSchedulesAtTime(timestamp: Long): List<ScheduleEntity> = TODO()
    override suspend fun getAvailableSchedules(currentTime: Long): List<ScheduleEntity> = TODO()
    override fun getAvailableSchedulesFlow(currentTime: Long): Flow<List<ScheduleEntity>> = TODO()
    override suspend fun getExpiredSchedules(timestamp: Long): List<ScheduleEntity> = TODO()
    override suspend fun getCount(): Int = items.size
    override fun countAsFlow(): Flow<Int> = TODO()
    override suspend fun getCountByDone(done: Boolean): Int =
        items.count { it.done == done }

    override suspend fun getCountByObservationId(observationId: String): Int =
        items.count { it.observationId == observationId }

    override fun getObservationTypesForScheduleIds(scheduleIds: Set<String>): Flow<List<String>> =
        TODO()

    override suspend fun updateDoneStatus(scheduleId: String, done: Boolean) {
        val sch = items.find { it.scheduleId == scheduleId }
        if (sch != null) {
            items.remove(sch)
            items.add(sch.copy(done = done))
            itemsFlow.value = items.toList()
        }
    }

    override suspend fun updateState(scheduleId: String, state: String) {
        updateStateCallCount++
        val sch = items.find { it.scheduleId == scheduleId }
        if (sch != null) {
            items.remove(sch)
            items.add(sch.copy(state = state))
            itemsFlow.value = items.toList()
        }
    }
}

class MockNotificationDao : MockBaseDao<NotificationEntity>(), NotificationDao {
    private val _itemsFlow = MutableStateFlow<List<NotificationEntity>>(emptyList())

    override suspend fun insert(entity: NotificationEntity) {
        super.insert(entity)
        _itemsFlow.value = items.toList()
    }

    override suspend fun insertAll(entities: List<NotificationEntity>) {
        super.insertAll(entities)
        _itemsFlow.value = items.toList()
    }

    override suspend fun deleteById(notificationId: String) {
        items.removeAll { it.notificationId == notificationId }
        _itemsFlow.value = items.toList()
    }

    override suspend fun deleteByChannelId(channelId: String) {
        items.removeAll { it.channelId == channelId }
        _itemsFlow.value = items.toList()
    }

    override suspend fun deleteAll() {
        items.clear()
        _itemsFlow.value = items.toList()
    }

    override suspend fun getById(notificationId: String): NotificationEntity? =
        items.find { it.notificationId == notificationId }

    override fun getByIdFlow(notificationId: String): Flow<NotificationEntity?> =
        _itemsFlow.map { list -> list.find { it.notificationId == notificationId } }

    override suspend fun getAll(): List<NotificationEntity> = items.toList()
    override fun getAllFlow(): Flow<List<NotificationEntity>> = _itemsFlow

    override suspend fun getByChannelId(channelId: String): List<NotificationEntity> =
        items.filter { it.channelId == channelId }

    override fun getByChannelIdFlow(channelId: String): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.channelId == channelId } }

    override suspend fun getByReadStatus(read: Boolean): List<NotificationEntity> =
        items.filter { it.read == read }

    override fun getByReadStatusFlow(read: Boolean): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.read == read } }

    override suspend fun getByCompletedStatus(completed: Boolean): List<NotificationEntity> =
        items.filter { it.completed == completed }

    override fun getByCompletedStatusFlow(completed: Boolean): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.completed == completed } }

    override suspend fun getByUserFacing(userFacing: Boolean): List<NotificationEntity> =
        items.filter { it.userFacing == userFacing }

    override fun getByUserFacingFlow(userFacing: Boolean): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.userFacing == userFacing } }

    override suspend fun getByPriority(priority: Long): List<NotificationEntity> =
        items.filter { it.priority == priority }

    override fun getByPriorityFlow(priority: Long): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.priority == priority } }

    override suspend fun getByMinPriority(minPriority: Long): List<NotificationEntity> =
        items.filter { it.priority >= minPriority }

    override fun getByMinPriorityFlow(minPriority: Long): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.priority >= minPriority } }

    override suspend fun getByTimeRange(
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<NotificationEntity> =
        items.filter { it.timestamp != null && it.timestamp >= fromTimestamp && it.timestamp <= toTimestamp }

    override fun getByTimeRangeFlow(
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.timestamp != null && it.timestamp >= fromTimestamp && it.timestamp <= toTimestamp } }

    override suspend fun getWithDeepLink(): List<NotificationEntity> =
        items.filter { it.deepLink != null && it.deepLink.isNotEmpty() }

    override fun getWithDeepLinkFlow(): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.deepLink != null && it.deepLink.isNotEmpty() } }

    override suspend fun searchByContent(searchTerm: String): List<NotificationEntity> =
        items.filter { it.toString().contains(searchTerm, ignoreCase = true) }

    override fun searchByContentFlow(searchTerm: String): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list ->
            list.filter {
                it.toString().contains(searchTerm, ignoreCase = true)
            }
        }

    override suspend fun getLatest(limit: Int): List<NotificationEntity> =
        items.sortedByDescending { it.timestamp }.take(limit)

    override fun getByPastUserFacingFlow(userFacing: Boolean): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.filter { it.userFacing == userFacing } }

    override fun getUnreadUserFacingFromPastFlow(): Flow<Int> =
        _itemsFlow.map { list -> list.count { !it.read && it.userFacing } }

    override suspend fun getScheduledNotificationCount(currentTimestamp: Long): Int =
        items.count { (it.timestamp ?: 0) > currentTimestamp }

    override suspend fun getScheduledNotifications(currentTimestamp: Long): List<NotificationEntity> =
        items.filter { (it.timestamp ?: 0) > currentTimestamp }

    override fun getLatestFlow(limit: Int): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list -> list.sortedByDescending { it.timestamp }.take(limit) }

    override suspend fun getLatestUserFacing(limit: Int): List<NotificationEntity> =
        items.filter { it.userFacing }.sortedByDescending { it.timestamp }.take(limit)

    override fun getLatestUserFacingFlow(limit: Int): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list ->
            list.filter { it.userFacing }.sortedByDescending { it.timestamp }.take(limit)
        }

    override suspend fun getUnreadUserFacing(): List<NotificationEntity> =
        items.filter { !it.read && it.userFacing }.sortedByDescending { it.priority }
            .sortedByDescending { it.timestamp }

    override fun getUnreadUserFacingFlow(): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list ->
            list.filter { !it.read && it.userFacing }.sortedByDescending { it.priority }
                .sortedByDescending { it.timestamp }
        }

    override suspend fun getAllOrderedByPriorityAndTime(): List<NotificationEntity> =
        items.sortedByDescending { it.priority }.sortedByDescending { it.timestamp }

    override fun getAllOrderedByPriorityAndTimeFlow(): Flow<List<NotificationEntity>> =
        _itemsFlow.map { list ->
            list.sortedByDescending { it.priority }.sortedByDescending { it.timestamp }
        }

    override fun getCount(): Flow<Long> = _itemsFlow.map { it.size.toLong() }

    override suspend fun getCountByReadStatus(read: Boolean): Int =
        items.count { it.read == read }

    override suspend fun getCountByCompletedStatus(completed: Boolean): Int =
        items.count { it.completed == completed }

    override fun getCountByUserFacing(userFacing: Boolean): Flow<Long> =
        _itemsFlow.map { list -> list.count { it.userFacing == userFacing }.toLong() }

    override suspend fun getUnreadUserFacingCount(): Int =
        items.count { !it.read && it.userFacing }

    override fun getUnreadUserFacingCountFlow(): Flow<Int> =
        _itemsFlow.map { list -> list.count { !it.read && it.userFacing } }

    override suspend fun getAllChannelIds(): List<String> =
        items.mapNotNull { it.channelId }.distinct()

    override suspend fun updateReadStatus(notificationId: String, read: Boolean) {
        val index = items.indexOfFirst { it.notificationId == notificationId }
        if (index != -1) {
            items[index] = items[index].copy(read = read)
            _itemsFlow.value = items.toList()
        }
    }

    override suspend fun updateCompletedStatus(notificationId: String, completed: Boolean) {
        val index = items.indexOfFirst { it.notificationId == notificationId }
        if (index != -1) {
            items[index] = items[index].copy(completed = completed)
            _itemsFlow.value = items.toList()
        }
    }

    override suspend fun markAllAsReadByChannelId(channelId: String) {
        items.forEachIndexed { index, notificationEntity ->
            if (notificationEntity.channelId == channelId) {
                items[index] = notificationEntity.copy(read = true)
            }
        }
        _itemsFlow.value = items.toList()
    }

    override suspend fun markAllAsRead() {
        items.forEachIndexed { index, notificationEntity ->
            items[index] = notificationEntity.copy(read = true)
        }
        _itemsFlow.value = items.toList()
    }

    override suspend fun deleteOlderThan(timestamp: Long): Int {
        val toRemove = items.filter { (it.timestamp ?: 0) < timestamp }
        items.removeAll(toRemove)
        _itemsFlow.value = items.toList()
        return toRemove.size
    }

    override suspend fun deleteOldReadAndCompleted(timestamp: Long): Int {
        val toRemove = items.filter { it.read && it.completed && (it.timestamp ?: 0) < timestamp }
        items.removeAll(toRemove)
        _itemsFlow.value = items.toList()
        return toRemove.size
    }
}

class MockObservationDataDao : ObservationDataDao {
    override suspend fun insert(entity: ObservationDataEntity) = TODO()
    override suspend fun insertAll(entities: List<ObservationDataEntity>) = TODO()
    override suspend fun update(entity: ObservationDataEntity) = TODO()
    override suspend fun updateAll(entities: List<ObservationDataEntity>) = TODO()
    override suspend fun delete(entity: ObservationDataEntity) = TODO()
    override suspend fun deleteByObservationId(observationId: String) {}
    override suspend fun deleteAll() {}
    override suspend fun getAll(): List<ObservationDataEntity> = TODO()
    override fun getAllFlow(): Flow<List<ObservationDataEntity>> = TODO()
    override suspend fun getByObservationId(observationId: String): List<ObservationDataEntity> =
        TODO()

    override fun getByObservationIdFlow(observationId: String): Flow<List<ObservationDataEntity>> =
        TODO()

    override suspend fun deleteById(dataId: String) = TODO()
    override suspend fun deleteByObservationType(observationType: String) = TODO()
    override suspend fun getById(dataId: String): ObservationDataEntity? = TODO()
    override fun getByIdFlow(dataId: String): Flow<ObservationDataEntity?> = TODO()
    override suspend fun getByObservationType(observationType: String): List<ObservationDataEntity> =
        TODO()

    override fun getByObservationTypeFlow(observationType: String): Flow<List<ObservationDataEntity>> =
        TODO()

    override suspend fun getByObservationIdAndType(
        observationId: String,
        observationType: String
    ): List<ObservationDataEntity> = TODO()

    override fun getByObservationIdAndTypeFlow(
        observationId: String,
        observationType: String
    ): Flow<List<ObservationDataEntity>> = TODO()

    override suspend fun getByTimeRange(
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<ObservationDataEntity> = TODO()

    override fun getByTimeRangeFlow(
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationDataEntity>> = TODO()

    override suspend fun getByObservationIdAndTimeRange(
        observationId: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<ObservationDataEntity> = TODO()

    override fun getByObservationIdAndTimeRangeFlow(
        observationId: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationDataEntity>> = TODO()

    override suspend fun getByObservationTypeAndTimeRange(
        observationType: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<ObservationDataEntity> = TODO()

    override fun getByObservationTypeAndTimeRangeFlow(
        observationType: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationDataEntity>> = TODO()

    override suspend fun getFromTimestamp(timestamp: Long): List<ObservationDataEntity> = TODO()
    override fun getFromTimestampFlow(timestamp: Long): Flow<List<ObservationDataEntity>> = TODO()
    override suspend fun getUpToTimestamp(timestamp: Long): List<ObservationDataEntity> = TODO()
    override fun getUpToTimestampFlow(timestamp: Long): Flow<List<ObservationDataEntity>> = TODO()
    override suspend fun getLatest(limit: Int): List<ObservationDataEntity> = TODO()
    override fun getLatestFlow(limit: Int): Flow<List<ObservationDataEntity>> = TODO()
    override suspend fun getLatestByObservationId(
        observationId: String,
        limit: Int
    ): List<ObservationDataEntity> = TODO()

    override fun getLatestByObservationIdFlow(
        observationId: String,
        limit: Int
    ): Flow<List<ObservationDataEntity>> = TODO()

    override suspend fun getCount(): Int = TODO()
    override suspend fun getCountByObservationId(observationId: String): Int = TODO()
    override suspend fun getCountByObservationType(observationType: String): Int = TODO()
    override suspend fun getCountByTimeRange(fromTimestamp: Long, toTimestamp: Long): Int = TODO()
    override suspend fun getAllObservationTypes(): List<String> = TODO()
    override suspend fun getAllObservationIds(): List<String> = TODO()
    override suspend fun getEarliestTimestamp(): Long? = TODO()
    override suspend fun getLatestTimestamp(): Long? = TODO()
    override suspend fun deleteOlderThan(timestamp: Long): Int = TODO()
    override suspend fun deleteOlderThanByObservationId(
        observationId: String,
        timestamp: Long
    ): Int = TODO()
}

class MockLatestObservationDataDao : LatestObservationDataDao {
    val items = MutableStateFlow<Map<String, LatestObservationDataEntity>>(emptyMap())

    override suspend fun upsert(data: LatestObservationDataEntity) {
        items.value += (data.scheduleId to data)
    }

    override fun getByScheduleId(scheduleId: String): Flow<LatestObservationDataEntity?> =
        items.map { it[scheduleId] }

    override suspend fun getLatestByObservationType(observationType: String): LatestObservationDataEntity? =
        items.value.values.filter { it.observationType == observationType }
            .maxByOrNull { it.timestamp }

    override suspend fun deleteByScheduleId(scheduleId: String) {
        items.value -= scheduleId
    }

    override suspend fun deleteAll() {
        items.value = emptyMap()
    }
}

class MockAggregatedObservationDataDao : AggregatedObservationDataDao {
    override suspend fun insert(entity: AggregatedObservationDataEntity) = TODO()
    override suspend fun insertAll(entities: List<AggregatedObservationDataEntity>) = TODO()
    override suspend fun update(entity: AggregatedObservationDataEntity) = TODO()
    override suspend fun updateAll(entities: List<AggregatedObservationDataEntity>) = TODO()
    override suspend fun delete(entity: AggregatedObservationDataEntity) = TODO()
    override suspend fun deleteByObservationId(observationId: String) {}
    override suspend fun deleteAll() {}
    override suspend fun getById(id: String): AggregatedObservationDataEntity? = TODO()
    override fun getByIdFlow(id: String): Flow<AggregatedObservationDataEntity?> = TODO()
    override suspend fun getByObservationId(observationId: String): List<AggregatedObservationDataEntity> =
        TODO()

    override fun getByObservationIdFlow(observationId: String): Flow<List<AggregatedObservationDataEntity>> =
        TODO()

    override suspend fun getByObservationType(observationType: String): List<AggregatedObservationDataEntity> =
        TODO()

    override fun getByObservationTypeFlow(observationType: String): Flow<List<AggregatedObservationDataEntity>> =
        TODO()

    override suspend fun getAll(): List<AggregatedObservationDataEntity> = TODO()
    override fun getAllFlow(): Flow<List<AggregatedObservationDataEntity>> = TODO()
    override suspend fun deleteById(id: String) = TODO()
    override suspend fun getCount(): Int = TODO()
    override suspend fun getCountByObservationId(observationId: String): Int = TODO()
}

class MockBluetoothDeviceDao : BluetoothDeviceDao {
    override suspend fun insert(entity: BluetoothDeviceEntity) = TODO()
    override suspend fun insertAll(entities: List<BluetoothDeviceEntity>) = TODO()
    override suspend fun update(entity: BluetoothDeviceEntity) = TODO()
    override suspend fun updateAll(entities: List<BluetoothDeviceEntity>) = TODO()
    override suspend fun delete(entity: BluetoothDeviceEntity) = TODO()
    override suspend fun getAll(): List<BluetoothDeviceEntity> = TODO()
    override fun getAllFlow(): Flow<List<BluetoothDeviceEntity>> = TODO()
    override suspend fun getByAddress(address: String): BluetoothDeviceEntity? = TODO()
    override fun getByAddressFlow(address: String): Flow<BluetoothDeviceEntity?> = TODO()
    override suspend fun getAllAddresses(): List<String> = TODO()
    override fun getAllAddressesFlow(): Flow<List<String>> = TODO()
    override suspend fun deleteByAddress(address: String) = TODO()
    override suspend fun getCount(): Int = TODO()
    override suspend fun deleteAll() = TODO()
}
