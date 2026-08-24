/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.store

interface PermissionRepository {
    fun updatePermission(permissionType: PermissionType, granted: Boolean)

    fun getPermission(permissionType: PermissionType): PermissionApprovalState

    fun removePermission(permissionType: PermissionType)

    fun storeValue(key: String, value: String)

    fun loadValue(key: String): String?

    fun removeValue(key: String)
}

enum class PermissionApprovalState {
    GRANTED, DECLINED, NOT_SET
}

enum class PermissionType(val key: String) {
    APP_TRACKING("app_tracking")
}