package io.redlink.more.app.android.activities.dashboard.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun DashboardTab(model: DashboardViewModel, onClick: (Int) -> Unit = {}) {
    val tabIndex = model.currentTabIndex
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp))
    {

        TabRow(
            selectedTabIndex = 0,
            indicator = {
                TabRowDefaults.Indicator(
                    color = Color.Transparent
                )
            },
            backgroundColor = MoreColors.Primary,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(10))
        ) {
            model.tabData.forEachIndexed { index, viewObject ->
                Tab(
                    selected = tabIndex.value == index,
                    onClick = {
                        tabIndex.value = index
                        onClick(tabIndex.value)
                    },
                    modifier = Modifier
                        .background(if(tabIndex.value == index) MoreColors.Primary else MoreColors.PrimaryMedium)
                        .border(if (tabIndex.value == index) BorderStroke(2.dp, MoreColors.PrimaryDark) else BorderStroke(0.dp, MoreColors.PrimaryLight)),
                    selectedContentColor = MoreColors.PrimaryLight,
                    unselectedContentColor = MoreColors.PrimaryLight200,

                ) {
                    Text(
                        text = viewObject.tabText,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clip(RoundedCornerShape(40))
                    )
                }
            }
        }

    }
}