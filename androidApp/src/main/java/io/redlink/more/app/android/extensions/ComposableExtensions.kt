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


import android.app.Activity
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource

@Composable
@ReadOnlyComposable
fun getStringResource(@StringRes id: Int): String =
    LocalContext.current.resources.getText(id).toString()

@Composable
@ReadOnlyComposable
fun color(@ColorRes id: Int) = colorResource(id = id)

@Composable
fun Image(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) =
    Image(
        painter = painterResource(id = id),
        contentDescription,
        modifier,
        alignment,
        contentScale,
        alpha,
        colorFilter
    )

fun showNewActivityAndClearStack(context: Context, cls: Class<*>, forwardExtras: Boolean = false, forwardDeepLink: Boolean = false) {
    (context as? Activity)?.let {
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (forwardExtras) {
            intent.putExtras(it.intent)
        }

        if (forwardDeepLink) {
            intent.data = it.intent.data
        }
        TaskStackBuilder.create(it).addNextIntentWithParentStack(intent).startActivities()
    }
}

fun showNewActivity(context: Context, cls: Class<*>) {
    (context as? Activity)?.let {
        val intent = Intent(context, cls)
        it.startActivity(intent)
    }
}