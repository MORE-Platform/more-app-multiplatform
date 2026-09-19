/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.observations.longRunningObservation

/**
 * Interface for managing long-running observations.
 * @param T The type of the observation data or identifier.
 */
interface LongRunningObservationStorage {
    /**
     * Stores instant data.
     * @param data The observation data.
     * @param timestamp The timestamp in epoch milliseconds.
     */
    fun <T> storeInstant(data: T, timestamp: Long)

    /**
     * Starts a long-running observation.
     * @param data The observation data or event triggering the start.
     * @param identifier A unique identifier for the observation range.
     * @param timestamp The start timestamp in epoch milliseconds.
     */
    fun <T> startObservation(data: T, identifier: String, timestamp: Long)

    /**
     * Updates an ongoing long-running observation.
     * @param data The observation data or event.
     * @param identifier A unique identifier for the observation range.
     * @param timestamp The update timestamp in epoch milliseconds.
     */
    fun <T> updateObservation(
        data: T,
        identifier: String,
        timestamp: Long
    )

    /**
     * Adds an in-range event to an ongoing long-running observation.
     * @param data The observation data or event.
     * @param identifier A unique identifier for the observation range.
     * @param timestamp The update timestamp in epoch milliseconds.
     */
    fun <T> inRangeObservation(
        data: T,
        identifier: String,
        timestamp: Long
    )

    /**
     * Finishes a long-running observation and persists the result.
     * @param data The observation data or event triggering the finish.
     * @param identifier A unique identifier for the observation range.
     * @param timestamp The end timestamp in epoch milliseconds.
     */
    fun <T> finishObservation(data: T, identifier: String, timestamp: Long)
}
