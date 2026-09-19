/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.viewModels.settings

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.Napier
import io.redlink.more.SharedRes
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.getPlatform
import io.redlink.more.logging.event
import io.redlink.more.models.PermissionModel
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.observations.observationTypes.AppUsageObservationType
import io.redlink.more.services.store.PermissionApprovalState
import io.redlink.more.services.store.PermissionRepositoryImpl
import io.redlink.more.services.store.PermissionType
import io.redlink.more.services.store.SharedStorageRepository
import io.redlink.more.util.openSystemSettings
import io.redlink.more.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine

interface ExitStudyListener {
    fun exitStudy(onComplete: () -> Unit)
}

class CoreSettingsViewModel(
    mainRepository: MainRepository,
    sharedStorageRepository: SharedStorageRepository,
    private val customViewIdentifier: String? = null
) : CoreViewModel() {
    private val permissionRepository = PermissionRepositoryImpl(sharedStorageRepository)
    private val _dataDeleted = MutableStateFlow(false)

    @NativeCoroutines
    val dataDeleted: StateFlow<Boolean> = _dataDeleted

    private val _study = MutableStateFlow<StudyEntity?>(null)

    @NativeCoroutines
    val study: StateFlow<StudyEntity?> = _study
    private val _permissionModel = MutableStateFlow<PermissionModel?>(null)

    @NativeCoroutines
    val permissionModel: StateFlow<PermissionModel?> = _permissionModel

    private val _needsTracking = MutableStateFlow(false)

    @NativeCoroutines
    val needsTracking: StateFlow<Boolean> = _needsTracking

    private val _allowTracking = MutableStateFlow(false)

    @NativeCoroutines
    val allowTracking: StateFlow<Boolean> = _allowTracking

    private var exitStudyObserver: ExitStudyListener? = null

    init {
        _allowTracking.value =
            permissionRepository.getPermission(PermissionType.APP_TRACKING) == PermissionApprovalState.GRANTED
        launchScope {
            mainRepository.study.study.combine(mainRepository.observation.observations()) { study, observations ->
                Pair(study, observations)
            }.cancellable().collect { (study, observations) ->
                if (study?.active == true) {
                    _study.value = study
                    _permissionModel.value = PermissionModel.createFromSchema(study, observations)
                    _needsTracking.value = observations.map { it.observationType }
                        .contains(
                            AppUsageObservationType().observationType
                        )
                }
            }
        }
    }

    fun setExitStudyObserver(observer: ExitStudyListener?) {
        exitStudyObserver = observer
    }

    fun setTrackingPermission(allow: Boolean) {
        _allowTracking.value = allow
        if (allow) {
            Napier.event(LogEvent.APP_TRACKING_ACCEPTED)
        } else {
            Napier.event(LogEvent.APP_TRACKING_DECLINED)
        }
    }

    fun exitStudy() {
        exitStudyObserver?.exitStudy {
            _dataDeleted.value = true
        }
    }

    fun openSettings() {
        openSystemSettings()
    }

    // Needed for iOS
    fun showAppTrackingPermissionDialog() {
        val isAndroid = getPlatform().name.lowercase().contains("android")
        val messageRes = if (isAndroid) {
            SharedRes.strings.app_usage_tracking_disabled_message_android
        } else {
            SharedRes.strings.app_usage_tracking_disabled_message_ios
        }
        val model = AlertDialogModel(
            title = StringDesc.Resource(SharedRes.strings.app_usage_tracking_disabled_title),
            message = StringDesc.Resource(messageRes),
            confirmLabel = StringDesc.Resource(SharedRes.strings.app_usage_tracking_disabled_confirm),
            cancelLabel = StringDesc.Resource(SharedRes.strings.app_tracking_dialog_negative_button),
            onConfirm = { openSystemSettings() }
        )
        AlertController.openAlertDialog(model)
    }

    override fun viewIdentifier(): String {
        return customViewIdentifier ?: NavigationRoute.SETTINGS.viewIdentifier
    }

    override fun close() {
        setExitStudyObserver(null)
        super.close()
    }
}