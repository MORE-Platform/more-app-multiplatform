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
package io.redlink.more.app.android.p2r_composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.app.android.theme.MorePlatformTheme

@Composable
fun SharedBackground(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackButtonClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    /** extra padding to apply around content (useful to align cards etc) */
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable () -> Unit
) {
    MorePlatformTheme {
        Scaffold(
            topBar = {
                SharedTopAppBar(
                    title = title,
                    subtitle = subtitle,
                    showBackButton = showBackButton,
                    onBackButtonClick = onBackButtonClick,
                    actions = actions
                )
            },
            bottomBar = bottomBar,
            modifier = modifier.fillMaxSize(),
            backgroundColor = MoreColors.PrimaryLight
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    // apply an overall vertical gradient that automatically spans the box height
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFEDF2F7),
                                MoreColors.PrimaryLight
                            )
                        )
                    )
                    // inner content padding so tabs/content don't touch screen edges
                    .padding(contentPadding)
            ) {
                // expose the content area for screen readers as the main content
                Box(modifier = Modifier.fillMaxSize().semantics {} ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SharedTopAppBar(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackButtonClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        title = {
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = MoreColors.PrimaryDark,
                        letterSpacing = (-0.5).sp
                    )
                    ,
                    // mark the title as a heading for accessibility
                    modifier = Modifier.semantics { heading() }
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = MoreColors.Secondary
                        )
                    )
                }
            }
        },
        navigationIcon = if (showBackButton) {
            {
                IconButton(
                    onClick = onBackButtonClick,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = MoreColors.PrimaryDark
                    )
                }
            }
        } else null,
        actions = actions
    )
}

@Preview(showBackground = true)
@Composable
fun SharedBackgroundPreview() {
    SharedBackground(
        title = "Title",
        subtitle = "Subtitle placeholder text.",
        actions = {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MoreColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Content goes here")
        }
    }
}
