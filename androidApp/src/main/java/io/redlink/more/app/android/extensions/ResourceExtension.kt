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
package io.redlink.more.app.android.extensions

import android.content.Context
import android.provider.Settings
import io.redlink.more.app.android.BuildConfig
import io.redlink.more.app.android.MoreApplication


fun stringResource(id: Int) = MoreApplication.appContext?.getString(id) ?: ""

fun getQuantityString(id: Int, count: Int, formatArgs: Any) = MoreApplication.appContext?.resources?.getQuantityString(id, count, formatArgs) ?: ""

fun <T> getSystemService(serviceClass: Class<T>): T? = MoreApplication.appContext?.getSystemService(serviceClass)

fun getSecureID(context: Context) = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

fun getProductName() = android.os.Build.PRODUCT

const val applicationId = BuildConfig.APPLICATION_ID