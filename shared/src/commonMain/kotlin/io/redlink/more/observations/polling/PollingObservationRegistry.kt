package io.redlink.more.observations.polling

import io.github.aakira.napier.Napier
import io.redlink.more.extensions.jsonRead
import io.redlink.more.extensions.jsonString
import io.redlink.more.services.store.SharedStorageRepository

/**
 * Tracks which observation types currently want background polling and keeps the single shared
 * [PollingTaskScheduler] request (one Worker on Android, one BGAppRefreshTask on iOS) in sync
 * with that set, without ever resubmitting an already-active request for an unchanged interval.
 * Persisted so a worker/task run in a fresh process (app not running) still knows which
 * observation types are activated.
 */
object PollingObservationRegistry {
    private var scheduler: PollingTaskScheduler? = null
    private var storage: SharedStorageRepository? = null
    private val activeIntervals = mutableMapOf<String, Long>()

    fun init(scheduler: PollingTaskScheduler?, storage: SharedStorageRepository) {
        this.scheduler = scheduler
        this.storage = storage
        activeIntervals.clear()
        activeIntervals.putAll(loadPersisted(storage))
    }

    fun activate(observationType: String, intervalMillis: Long) {
        if (activeIntervals[observationType] == intervalMillis) {
            return
        }
        activeIntervals[observationType] = intervalMillis
        persist()
        Napier.i(tag = "PollingObservationRegistry::activate") { "Activated background polling for $observationType every ${intervalMillis}ms" }
        scheduler?.schedule(activeIntervals.values.min())
    }

    fun deactivate(observationType: String) {
        if (activeIntervals.remove(observationType) == null) {
            return
        }
        persist()
        Napier.i(tag = "PollingObservationRegistry::deactivate") { "Deactivated background polling for $observationType" }
        if (activeIntervals.isEmpty()) {
            scheduler?.cancel()
        } else {
            scheduler?.schedule(activeIntervals.values.min())
        }
    }

    fun activeObservationTypes(): Set<String> = activeIntervals.keys.toSet()

    private fun persist() {
        storage?.store(STORAGE_KEY, activeIntervals.jsonString())
    }

    private fun loadPersisted(storage: SharedStorageRepository): Map<String, Long> {
        val raw = storage.load(STORAGE_KEY, "")
        if (raw.isBlank()) return emptyMap()
        return raw.jsonRead<Map<String, Long>>() ?: emptyMap()
    }

    private const val STORAGE_KEY = "pollingObservationRegistryActiveIntervals"
}
