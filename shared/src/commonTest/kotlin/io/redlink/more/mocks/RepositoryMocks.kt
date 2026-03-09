package io.redlink.more.mocks

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.database.entities.DataPointEntity
import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.database.repository.BluetoothDeviceRepository
import io.redlink.more.database.repository.DataPointCountRepository
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.database.repository.NotificationRepository
import io.redlink.more.database.repository.ObservationDataRepository
import io.redlink.more.database.repository.ObservationRepository
import io.redlink.more.database.repository.ScheduleRepository
import io.redlink.more.database.repository.StudyRepository
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.DataRecorder
import io.redlink.more.observations.ObservationDataManager
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.ObservationFactoryImpl
import io.redlink.more.observations.observationTypes.ObservationType
import io.redlink.more.scopes.MoreScope
import io.redlink.more.scopes.StudyMoreScope
import io.redlink.more.services.network.openapi.model.DataBulk
import io.redlink.more.services.network.openapi.model.Study
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.Instant
import kotlin.coroutines.CoroutineContext

class MockMoreScope(private val testScope: TestScope) : MoreScope {
    override val coroutineContext: CoroutineContext = testScope.coroutineContext

    val jobs = mutableMapOf<String, Job>()

    override fun launch(
        coroutineContext: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = "job_${jobs.size}"
        val job = testScope.launch(coroutineContext, start, block)
        jobs[uuid] = job
        return uuid to job
    }

    override fun repeatedLaunch(
        intervalMillis: Long,
        coroutineContext: CoroutineContext,
        initalDelay: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = "repeat_${jobs.size}"
        val job = testScope.launch(coroutineContext) {
            // Simple mock: just run it once or handle it as needed in tests
            block()
        }
        jobs[uuid] = job
        return uuid to job
    }

    override fun cancel(uuid: String) {
        jobs[uuid]?.cancel()
    }

    override fun cancel(uuids: Collection<String>) {
        uuids.forEach { cancel(it) }
    }

    override fun cancel() {
        jobs.values.forEach { it.cancel() }
    }
}

class MockStudyMoreScope(private val testScope: TestScope) : StudyMoreScope {
    override val coroutineContext: CoroutineContext = testScope.coroutineContext

    val jobs = mutableMapOf<String, Job>()

    override fun launch(
        coroutineContext: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = "study_job_${jobs.size}"
        val job = testScope.launch(coroutineContext, start, block)
        jobs[uuid] = job
        return uuid to job
    }

    override fun repeatedLaunch(
        intervalMillis: Long,
        coroutineContext: CoroutineContext,
        initalDelay: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = "study_repeat_${jobs.size}"
        val job = testScope.launch(coroutineContext) {
            block()
        }
        jobs[uuid] = job
        return uuid to job
    }

    override fun cancel(uuid: String) {
        jobs[uuid]?.cancel()
    }

    override fun cancel(uuids: Collection<String>) {
        uuids.forEach { cancel(it) }
    }

    override fun cancel() {
        jobs.values.forEach { it.cancel() }
    }
}

class MockNotificationRepository : NotificationRepository {
    private val notifications = MutableStateFlow<Map<String, NotificationEntity>>(emptyMap())

    override suspend fun storeNotification(notification: NotificationEntity) {
        notifications.value += (notification.notificationId to notification)
    }

    override suspend fun storeNotifications(notifications: List<NotificationEntity>) {
        this.notifications.value += notifications.associateBy { it.notificationId }
    }

    override suspend fun getNotification(notificationId: String): NotificationEntity? {
        return notifications.value[notificationId]
    }

    override fun setNotificationReadStatus(key: String, read: Boolean) {
        notifications.value[key]?.let {
            notifications.value += (key to it.copy(read = read))
        }
    }

    override fun setNotificationCompletedStatus(key: String, completed: Boolean) {
        notifications.value[key]?.let {
            notifications.value += (key to it.copy(completed = completed))
        }
    }

    override fun deleteNotification(notificationId: String) {
        notifications.value -= notificationId
    }

    override suspend fun scheduledNotifications(): List<NotificationEntity> {
        return notifications.value.values.filter { !it.userFacing }
    }

    override fun getAllUserFacingNotifications(): Flow<List<NotificationEntity>> {
        return notifications.map { it.values.filter { n -> n.userFacing }.toList() }
    }

    override suspend fun deleteAll() {
        notifications.value = emptyMap()
    }

    override suspend fun update(notificationId: String, read: Boolean?, priority: Long?) {
        notifications.value[notificationId]?.let {
            notifications.value += (notificationId to it.copy(
                read = read ?: it.read,
                priority = priority ?: it.priority
            ))
        }
    }

    override suspend fun scheduledNotificationCount(): Int = scheduledNotifications().size
}

class MockObservationRepository : ObservationRepository {
    private val observations =
        MutableStateFlow<Map<String, ObservationEntity>>(emptyMap())

    val timestamps = MutableStateFlow<Map<String, Long>>(emptyMap())

    override fun observationById(observationId: String): Flow<ObservationEntity?> {
        return observations.map { it[observationId] }
    }

    fun storeObservation(observation: ObservationEntity) {
        observations.value += (observation.observationId to observation)
    }

    override suspend fun getCount(): Int = observations.value.size

    override fun observations(): Flow<List<ObservationEntity>> =
        observations.map { it.values.toList() }

    override fun observationWithUndoneSchedules(): Flow<Map<ObservationEntity, List<ScheduleEntity>>> =
        flowOf(emptyMap())

    override suspend fun updateLastCollection(type: String, timestamp: Long) {}

    override suspend fun updateLastCollection(types: Set<String>, timestamp: Long) {}

    override fun collectionTimestamp(type: String): Flow<Long?> = timestamps.map { it[type] }

    override fun collectAllTimestamps(): Flow<Map<String, Long>> = timestamps

    override fun collectTimestampForObservationIds(observationIds: Set<String>): Flow<Long> =
        flowOf(0L)

    override fun collectTimestampOfType(
        type: String,
        newState: (Long?) -> Unit
    ): Closeable = object : Closeable {
        override fun close() {}
    }

    override fun collectAllTimestamps(newState: (Map<String, Long>) -> Unit): Closeable =
        object : Closeable {
            override fun close() {}
        }

    override fun collectObservationsWithUndoneSchedules(newState: (Map<ObservationEntity, List<ScheduleEntity>>) -> Unit): Closeable =
        object : Closeable {
            override fun close() {}
        }

    override fun observationTypes(): Flow<Set<String>> =
        observations.map { it.values.map { o -> o.observationType }.toSet() }

    override suspend fun getObservationByObservationId(observationId: String): ObservationEntity? =
        observations.value[observationId]
}

class MockScheduleRepository : ScheduleRepository {
    private val _schedules = MutableStateFlow<Map<String, ScheduleEntity>>(emptyMap())
    val schedules: StateFlow<Map<String, ScheduleEntity>> = _schedules
    var scheduleWithIdResult: Flow<ScheduleEntity?>? = null
    var firstScheduleAvailableForObservationIdResult: Flow<ScheduleEntity?>? = null

    override fun scheduleWithId(id: String): Flow<ScheduleEntity?> =
        scheduleWithIdResult ?: _schedules.map { it[id] }

    override fun firstScheduleAvailableForObservationId(observationId: String): Flow<ScheduleEntity?> =
        firstScheduleAvailableForObservationIdResult
            ?: _schedules.map { it.values.find { s -> s.observationId == observationId } }

    fun storeSchedule(schedule: ScheduleEntity) {
        _schedules.value += (schedule.scheduleId to schedule)
    }

    override fun count(): Flow<Int> = _schedules.map { it.size }

    override fun allSchedulesWithStatus(done: Boolean): Flow<List<ScheduleEntity>> =
        _schedules.map { it.values.filter { s -> s.done == done } }

    override fun allSchedulesWithStates(states: Set<ScheduleState>): Flow<List<ScheduleEntity>> =
        _schedules.map { it.values.filter { s -> s.getState() in states } }

    override fun getSchedulesWithReminder(
        states: Set<ScheduleState>,
        minTimestamp: Instant,
        maxTimestamp: Instant,
        limit: Int
    ): Flow<List<ScheduleEntity>> = flowOf(emptyList())

    override fun allScheduleWithRunningState(scheduleState: ScheduleState): Flow<List<ScheduleEntity>> =
        _schedules.map { it.values.filter { s -> s.getState() == scheduleState } }

    override fun allSchedulesToday(observationType: ObservationType): Flow<List<ScheduleEntity>> =
        flowOf(emptyList())

    override fun firstScheduleIdAvailableForObservationId(observationId: String): Flow<String?> =
        firstScheduleAvailableForObservationId(observationId).map { it?.scheduleId }

    var firstAndLastDateResult: Flow<Pair<ScheduleEntity?, ScheduleEntity?>>? = null
    override fun getFirstAndLastDate(observationId: String): Flow<Pair<ScheduleEntity?, ScheduleEntity?>> =
        firstAndLastDateResult ?: flowOf(null to null)

    var lastSetRunningState: Pair<String, ScheduleState>? = null
    override suspend fun setRunningStateFor(id: String, scheduleState: ScheduleState) {
        lastSetRunningState = id to scheduleState
        _schedules.value[id]?.let {
            _schedules.value += (id to it.copy(state = scheduleState.name))
        }
    }

    var lastSetCompletionState: Pair<String, Boolean>? = null
    override suspend fun setCompletionStateFor(id: String, wasDone: Boolean) {
        lastSetCompletionState = id to wasDone
        _schedules.value[id]?.let {
            _schedules.value += (id to it.copy(done = wasDone))
        }
    }

    override suspend fun updateTaskStates(
        observationFactory: ObservationFactory,
        dataRecorder: DataRecorder
    ) {
    }
}

class MockObservationDataRepository : ObservationDataRepository {
    val addedData = mutableListOf<ObservationDataEntity>()
    var storeCalled = false
    var count = 0
    var deletedIds = setOf<String>()

    override fun addData(dataList: List<ObservationDataEntity>) {
        addedData.addAll(dataList)
    }

    override suspend fun store() {
        storeCalled = true
    }

    override suspend fun getCount(): Int = count

    override suspend fun allAsBulk(): DataBulk? = null

    override suspend fun deleteAllWithId(idSet: Set<String>) {
        deletedIds = idSet
    }
}

class MockDataPointCountRepository : DataPointCountRepository {
    val increments = mutableListOf<Pair<Set<String>, Long>>()
    val deletedScheduleIds = mutableListOf<String>()
    var dataPointResults = mutableMapOf<String, Flow<DataPointEntity?>>()

    override fun count(): Flow<Long> = flowOf(0L)

    override fun incrementCount(scheduleIdSet: Set<String>, addCount: Long) {
        increments.add(scheduleIdSet to addCount)
    }

    override fun get(scheduleId: String): Flow<DataPointEntity?> =
        dataPointResults[scheduleId] ?: flowOf(null)

    override fun delete(scheduleId: String) {
        deletedScheduleIds.add(scheduleId)
    }
}

class MockBluetoothDeviceRepository : BluetoothDeviceRepository {
    override fun storePairedDevice(bluetoothDevice: BluetoothDeviceEntity) {}
    override fun unpairDevice(bluetoothDevice: BluetoothDeviceEntity) {}
    override fun pairedDevices(): Flow<List<BluetoothDeviceEntity>> = flowOf(emptyList())
}

class MockStudyRepository : StudyRepository {
    private val _study = MutableStateFlow<StudyEntity?>(null)
    override val study: StateFlow<StudyEntity?> = _study

    private val _studyState = MutableStateFlow(io.redlink.more.models.StudyState.NONE)
    override val studyState: StateFlow<io.redlink.more.models.StudyState> = _studyState

    private val _finishText = MutableStateFlow<String?>(null)
    override val finishText: StateFlow<String?> = _finishText

    override suspend fun upsert(study: Study) {
        _study.value = StudyEntity.fromStudy(study)
    }

    suspend fun upsert(study: StudyEntity) {
        _study.value = study
    }

    override fun getStudy(): Flow<StudyEntity?> = study

    override suspend fun updateStudyState(state: io.redlink.more.models.StudyState) {
        _studyState.value = state
    }

    override suspend fun deleteStudy() {
        _study.value = null
    }
}

class MockMainRepository : MainRepository {
    private val _mockSchedule = MockScheduleRepository()
    private val _mockNotification = MockNotificationRepository()
    private val _mockObservation = MockObservationRepository()
    private val _mockObservationData = MockObservationDataRepository()
    private val _mockDataPointCount = MockDataPointCountRepository()
    private val _mockBluetoothDevice = MockBluetoothDeviceRepository()
    private val _mockStudy = MockStudyRepository()

    override val schedule: ScheduleRepository get() = _mockSchedule
    override val notification: NotificationRepository get() = _mockNotification
    override val observation: ObservationRepository get() = _mockObservation
    override val observationData: ObservationDataRepository get() = _mockObservationData
    override val dataPointCount: DataPointCountRepository get() = _mockDataPointCount
    override val bluetoothDevice: BluetoothDeviceRepository get() = _mockBluetoothDevice
    override val study: StudyRepository get() = _mockStudy

    val mockSchedule: MockScheduleRepository get() = _mockSchedule
    val mockNotification: MockNotificationRepository get() = _mockNotification
    val mockObservation: MockObservationRepository get() = _mockObservation
    val mockObservationData: MockObservationDataRepository get() = _mockObservationData
    val mockDataPointCount: MockDataPointCountRepository get() = _mockDataPointCount
    val mockStudy: MockStudyRepository get() = _mockStudy

    override suspend fun deleteAll() {}
}

class MockObservationFactory(
    repository: MainRepository = MockMainRepository(),
    dataManager: ObservationDataManager? = null
) : ObservationFactoryImpl(repository, dataManager ?: mockObservationDataManager(repository)) {
    var matchingObservationTypes: Set<String> = emptySet()

    override fun getMatchingObservationTypes(types: Set<String>): Set<String> =
        matchingObservationTypes
}
