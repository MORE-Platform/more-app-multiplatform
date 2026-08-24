/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.store

class PermissionRepositoryImpl(private val sharedStorageRepository: SharedStorageRepository) :
    PermissionRepository {

    override fun updatePermission(permissionType: PermissionType, granted: Boolean) {
        sharedStorageRepository.store(permissionType.key, granted.toString())
    }

    override fun getPermission(permissionType: PermissionType): PermissionApprovalState {
        val rawValue = sharedStorageRepository.load(permissionType.key, NOT_SET_STRING)
        return when (rawValue) {
            "true" -> PermissionApprovalState.GRANTED
            "false" -> PermissionApprovalState.DECLINED
            else -> PermissionApprovalState.NOT_SET
        }
    }

    override fun removePermission(permissionType: PermissionType) {
        sharedStorageRepository.remove(permissionType.key)
    }

    override fun storeValue(key: String, value: String) {
        sharedStorageRepository.store(key, value)
    }

    override fun loadValue(key: String): String? {
        val value = sharedStorageRepository.load(key, NOT_SET_STRING)
        return if (value == NOT_SET_STRING) null else value
    }

    override fun removeValue(key: String) {
        sharedStorageRepository.remove(key)
    }

    companion object {
        private const val NOT_SET_STRING = "NOT_SET"
    }
}
