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

fun showNewActivityAndClearStack(context: Context, cls: Class<*>) {
    (context as? Activity)?.let {
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        TaskStackBuilder.create(it).addNextIntentWithParentStack(intent).startActivities()
    }
}

fun showNewActivity(context: Context, cls: Class<*>) {
    (context as? Activity)?.let {
        val intent = Intent(context, cls)
        it.startActivity(intent)
    }
}