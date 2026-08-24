/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.more.database.entities.BluetoothDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothDeviceDao : BaseDao<BluetoothDeviceEntity> {

    @Query("SELECT * FROM BluetoothDeviceEntity")
    suspend fun getAll(): List<BluetoothDeviceEntity>

    @Query("SELECT * FROM BluetoothDeviceEntity")
    fun getAllFlow(): Flow<List<BluetoothDeviceEntity>>

    @Query("SELECT COUNT(*) FROM BluetoothDeviceEntity")
    suspend fun getCount(): Int

    @Query("DELETE FROM BluetoothDeviceEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM BluetoothDeviceEntity WHERE address = :address LIMIT 1")
    suspend fun getByAddress(address: String): BluetoothDeviceEntity?

    @Query("SELECT * FROM BluetoothDeviceEntity WHERE address = :address")
    fun getByAddressFlow(address: String): Flow<BluetoothDeviceEntity?>

    @Query("SELECT address FROM BluetoothDeviceEntity")
    suspend fun getAllAddresses(): List<String>

    @Query("SELECT address FROM BluetoothDeviceEntity")
    fun getAllAddressesFlow(): Flow<List<String>>

    @Query("DELETE FROM BluetoothDeviceEntity WHERE address = :address")
    suspend fun deleteByAddress(address: String)
}