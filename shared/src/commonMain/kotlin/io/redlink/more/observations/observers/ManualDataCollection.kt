package io.redlink.more.observations.observers

interface ManualObserver {
    suspend fun collectAllData()
}