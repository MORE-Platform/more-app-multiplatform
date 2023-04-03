package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class ScheduleRepository: Repository<ScheduleSchema>() {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    override fun count(): Flow<Long> = realmDatabase.count<ScheduleSchema>()

    fun allSchedulesWithStatus(done: Boolean = false) = realmDatabase.query<ScheduleSchema>(query = "done = $0", queryArgs = arrayOf(done))

    fun allScheduleWithRunningState(scheduleState: ScheduleState = ScheduleState.RUNNING) = realmDatabase.query<ScheduleSchema>(query = "state = $0", queryArgs = arrayOf(scheduleState.name))

    fun collectRunningState(forState: ScheduleState, provideNewState: (List<ScheduleSchema>) -> Unit): Closeable {
        return allScheduleWithRunningState(forState).asClosure(provideNewState)
    }

    fun setRunningStateFor(id: String, scheduleState: ScheduleState) {
        realmDatabase.realm?.writeBlocking {
            this.query<ScheduleSchema>("scheduleId = $0", ObjectId(id)).first().find()?.let {
                it.state = scheduleState.name
            }
        }
    }

    fun setCompletionStateFor(id: String, wasDone: Boolean) {
        realmDatabase.realm?.writeBlocking {
            this.query<ScheduleSchema>("scheduleId = $0", ObjectId(id)).first().find()?.let {
                it.done = true
                it.updateState(if (wasDone) ScheduleState.DONE else ScheduleState.ENDED)
            }
        }
    }

    fun scheduleWithId(id: String) = realmDatabase.queryFirst<ScheduleSchema>(query = "scheduleId = $0", queryArgs = arrayOf(ObjectId(id)))

    fun updateTaskStates(observationFactory: ObservationFactory? = null) {
        scope.launch {
            Napier.i { "Updating all tasks" }
            allSchedulesWithStatus().firstOrNull()?.let { schedules ->
                Napier.i { schedules.toString() }
                val changeMap = schedules.map {
                    it to it.updateState()
                }
                Napier.i { changeMap.toString() }
                changeMap.forEach {
                    setRunningStateFor(it.first.scheduleId.toHexString(), it.second)
                }
                observationFactory?.observationTypesNeedingRestartingAfterAppClosure()?.forEach { type ->
                    Napier.i { "Schedules with type $type need to reset; ${changeMap.filter { it.second == ScheduleState.RUNNING }}}" }
                    changeMap.filter { it.second == ScheduleState.RUNNING && type == it.first.observationType }
                        .forEach {
                            Napier.i { "Resetting ${it.first.scheduleId}" }
                            setRunningStateFor(it.first.scheduleId.toHexString(), ScheduleState.ACTIVE)
                        }
                }
            }
        }
    }


}