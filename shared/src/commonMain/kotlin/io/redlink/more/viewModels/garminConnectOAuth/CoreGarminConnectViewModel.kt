/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */

package io.redlink.more.viewModels.garminConnectOAuth

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import io.redlink.more.extensions.overlaps
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.services.network.NetworkService
import io.redlink.more.services.store.SharedStorageRepository
import io.redlink.more.viewModels.CoreViewModel
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.flow.MutableStateFlow

class CoreGarminConnectViewModel(
    private val networkService: NetworkService,
    private val sharedStorageRepository: SharedStorageRepository
) : CoreViewModel() {

    private val _isLoading = MutableStateFlow(false)

    @NativeCoroutines
    val isLoading: MutableStateFlow<Boolean> = _isLoading

    fun garminSSOUrl() = networkService.getGarminSSOUrl()

    fun loading(state: Boolean) {
        _isLoading.value = state
    }

    fun basicAuthHeader(forUrl: String): String? {
        val requestUrl = Url(forUrl)
        if (requestUrl.host.overlaps(networkService.baseUrl())) {
            return networkService.getBasicAuthHeader()
        }
        return null
    }

    fun checkIfUrlIsCallback(url: String): Boolean {
        Napier.d(tag = "CoreGarminConnectViewModel::checkIfUrlIsCallback") { "Checking URL: $url" }
        val requestUrl = Url(url)
        if (requestUrl.host != networkService.garminSSOCallbackUrl()?.host || requestUrl.encodedPath != networkService.garminSSOCallbackUrl()?.encodedPath) {
            return false
        }
        val code = requestUrl.parameters[GARMIN_CALLBACK_CODE_PARAMETER]
        val status = requestUrl.parameters[GARMIN_CALLBACK_STATUS_PARAMETER]
        Napier.d(tag = "CoreGarminConnectViewModel::checkIfUrlIsCallback") { "Code: $code, Status: $status" }
        return code != null
    }

    suspend fun sendCallback(url: String): Boolean {
        if (!checkIfUrlIsCallback(url)) {
            return false
        }
        Napier.d(tag = "CoreGarminConnectViewModel::sendCallback") { "Handling callback: $url" }
        val requestUrl = Url(url)
        val code = requestUrl.parameters[GARMIN_CALLBACK_CODE_PARAMETER]!!
        val status = requestUrl.parameters[GARMIN_CALLBACK_STATUS_PARAMETER] ?: ""
        return networkService.garminSSOCallback(
            code = code,
            status = status
        )
    }

    fun setLoading(state: Boolean) {
        _isLoading.value = state
    }

    fun onSuccess() {
        sharedStorageRepository.store(GARMIN_CONNECT_SUCCESSFUL_LOGIN, true)
        ViewManager.requestGarminConnectView(false)
    }

    fun closeView() {
        ViewManager.requestGarminConnectView(false)
    }

    override fun viewIdentifier(): String {
        return NavigationRoute.GARMIN_CONNECT.viewIdentifier
    }

    companion object {
        const val GARMIN_CONNECT_SUCCESSFUL_LOGIN = "GARMIN_CONNECT_SUCCESSFUL_LOGIN"

        private const val GARMIN_CALLBACK_CODE_PARAMETER = "code"
        private const val GARMIN_CALLBACK_STATUS_PARAMETER = "state"
    }
}