package io.redlink.more.more_app_mutliplatform.android.ui.theme


import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColors(
    primary = MoreColors.PrimaryDark,
    secondary = MoreColors.BackgroundOverlay,
)

private val LightColorScheme = lightColors(
    primary = MoreColors.PrimaryLight,
    onPrimary = MoreColors.Primary,
    secondary = MoreColors.SecondaryLight,
    onSecondary = MoreColors.TextInactive,
    background = MoreColors.PrimaryLight,
    onBackground = MoreColors.Primary,
)

@Composable
fun MorePlatformTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).let {
                it.window.statusBarColor = colorScheme.primary.toArgb()
//            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
            }
        }
    }

    MaterialTheme(
        colors = colorScheme,
        typography = Typography,
        content = content
    )
}