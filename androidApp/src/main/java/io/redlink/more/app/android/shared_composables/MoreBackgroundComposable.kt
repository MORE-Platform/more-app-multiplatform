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
package io.redlink.more.app.android.shared_composables

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.main.MainTabView
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.MorePlatformTheme
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel

@Composable
fun MoreBackground(
    navigationTitle: String = "",
    showBackButton: Boolean = false,
    onBackButtonClick: () -> Unit = {},
    leftCornerContent: @Composable () -> Unit = {},
    rightCornerContent: @Composable () -> Unit = {},
    showTabRow: Boolean = false,
    tabSelectionIndex: Int = 0,
    onTabChange: (Int) -> Unit = {},
    maxWidth: Float = 0.9F,
    alertDialogModel: AlertDialogModel? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    if (MoreApplication.openSettings.value) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
        MoreApplication.openSettings.value = false
    }
    MorePlatformTheme {
        Scaffold(topBar = {
            MoreTopAppBar(navigationTitle, showBackButton, onBackButtonClick, leftCornerContent, rightCornerContent)
        },
            bottomBar = {
                if (showTabRow) {
                    MoreBottomAppBar(selectedIndex = tabSelectionIndex, onTabChange = onTabChange)
                }
            }
        ) {
            Surface(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                color = MoreColors.PrimaryLight
            ) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth(maxWidth)
                            .fillMaxHeight()
                    ) {
                        content()
                    }

                    alertDialogModel?.let {
                        MessageAlertDialog(model = it)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreTopAppBar(
    navigationTitle: String,
    showBackButton: Boolean = true,
    onBackButtonClick: () -> Unit = {},
    leftCornerContent: @Composable () -> Unit = {},
    rightCornerContent: @Composable () -> Unit = {}
) {
    TopAppBar(
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
            ) {
                Box(
                    modifier = Modifier.weight(0.1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (showBackButton) {
                        IconButton(
                            onClick = { onBackButtonClick() },
                            enabled = true,
                        ) {
                            Icon(
                                Icons.Default.ArrowBackIos,
                                contentDescription = "Back",
                            )
                        }
                    } else {
                        leftCornerContent()
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(0.8f)
                ) {
                    NavigationBarTitle(text = navigationTitle)
                }
                Box(
                    modifier = Modifier.weight(0.1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    rightCornerContent()
                }
            }
        }
    }
}

@Composable
fun MoreBottomAppBar(selectedIndex: Int, onTabChange: (Int) -> Unit) {
    BottomAppBar(elevation = 2.dp, backgroundColor = MoreColors.PrimaryDark) {
        MainTabView(selectedIndex, onTabChange)
    }
}

@Preview
@Composable
fun BackgroundPreview() {
    MoreBackground(navigationTitle = "Test", true, alertDialogModel = AlertDialogModel("Test", "Message", "Accept", "DEcline", {})) {
        Text("Hello WOrld")
    }
}