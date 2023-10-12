package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.activities.main.MainTabView
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.MorePlatformTheme

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
    content: @Composable () -> Unit,
) {
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
    MoreBackground(navigationTitle = "Test", true) {
        Text("Hello WOrld")
    }
}