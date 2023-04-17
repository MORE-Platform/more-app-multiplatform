package io.redlink.more.app.android.extensions

import android.content.Context
import android.provider.Settings
import io.redlink.more.app.android.MoreApplication


fun getString(id: Int) = MoreApplication.appContext?.getString(id) ?: ""

fun getQuantityString(id: Int, count: Int, formatArgs: Any) = MoreApplication.appContext?.resources?.getQuantityString(id, count, formatArgs) ?: ""

fun <T> getSystemService(serviceClass: Class<T>): T? = MoreApplication.appContext?.getSystemService(serviceClass)

fun getSecureID(context: Context) = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

fun getProductName() = android.os.Build.PRODUCT