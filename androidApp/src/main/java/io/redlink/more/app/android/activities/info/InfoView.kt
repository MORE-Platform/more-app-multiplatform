package io.redlink.more.app.android.activities.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun InfoView(navController: NavController, viewModel: InfoViewModel) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Divider()
            InfoItem(
                title = "Study Details",
                imageVector = Icons.Default.Info,
                contentDescription = "Open Study Details",
                onClick = {
                    navController.navigate(NavigationScreen.STUDY_DETAILS.route)
                }
            )
            InfoItem(
                title = "Running Observations",
                imageVector = Icons.Outlined.Circle,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
            InfoItem(
                title = "Completed Observations",
                imageVector = Icons.Default.Check,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
            InfoItem(
                title = "Settings",
                imageVector = Icons.Default.Settings,
                contentDescription = "Open Settings",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
            InfoItem(
                title = "Leave Study",
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Exit Application",
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.route)
                }
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Spacer(modifier = Modifier.height(55.dp))

                    HeaderTitle(
                        title = viewModel.studyTitle.value,
                        color = MoreColors.PrimaryDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 25.dp),
                        textAlign = TextAlign.Center
                    )

                    SmallTitle(
                        text = viewModel.institute,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )

                    SmallTitle(
                        text = viewModel.contactPerson,
                        modifier = Modifier.fillMaxWidth(),
                        color = MoreColors.Secondary,
                        textAlign = TextAlign.Center
                    )

                    if (viewModel.contactEmail != null)
                        BasicText(
                            text = viewModel.contactEmail,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            color = MoreColors.Secondary,
                            textAlign = TextAlign.Center
                        )

                    if (viewModel.contactTel != null)
                        BasicText(
                            text = viewModel.contactTel,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            color = MoreColors.Secondary,
                            textAlign = TextAlign.Center
                        )

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider()

                    BasicText(
                        text = viewModel.info,
                        color = MoreColors.Secondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
            }

        }
    }
}
