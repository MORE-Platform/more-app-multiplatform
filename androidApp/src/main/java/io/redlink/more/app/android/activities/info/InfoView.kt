package io.redlink.more.app.android.activities.info

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.BLESetup.BLEConnectionActivity
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.showNewActivity
import io.redlink.more.app.android.shared_composables.AppVersion
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun InfoView(navController: NavController, viewModel: InfoViewModel) {
    val context = LocalContext.current
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.INFO.routeWithParameters())
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Divider()
            InfoItem(
                title = getStringResource(id = R.string.info_study_details),
                imageVector = Icons.Default.Info,
                contentDescription = getStringResource(id = R.string.info_study_details_desc),
                onClick = {
                    navController.navigate(NavigationScreen.STUDY_DETAILS.routeWithParameters())
                }
            )
            InfoItem(
                title = getStringResource(id = R.string.info_running_observations),
                imageVector = Icons.Outlined.Autorenew,
                contentDescription = getStringResource(id = R.string.info_running_observations_desc),
                onClick = {
                    navController.navigate(NavigationScreen.RUNNING_SCHEDULES.routeWithParameters())
                }
            )
            InfoItem(
                title = getStringResource(id = R.string.info_completed_observations),
                imageVector = Icons.Default.Check,
                contentDescription = getStringResource(id = R.string.info_completed_observations_desc),
                onClick = {
                    navController.navigate(NavigationScreen.COMPLETED_SCHEDULES.routeWithParameters())
                }
            )
            InfoItem(
                title = NavigationScreen.BLUETOOTH_CONNECTION.stringRes(),
                imageVector = Icons.Default.Watch,
                contentDescription = getStringResource(id = R.string.more_ble_icon_description),
                onClick = {
                    (context as? Activity)?.let {
                        showNewActivity(it, BLEConnectionActivity::class.java)
                    }
                }
            )
            InfoItem(
                title = getStringResource(id = R.string.info_settings),
                imageVector = Icons.Default.Settings,
                contentDescription = getStringResource(id = R.string.info_consent_settings_desc),
                onClick = {
                    navController.navigate(NavigationScreen.SETTINGS.routeWithParameters())
                }
            )
            InfoItem(
                title = getStringResource(id = R.string.info_leave_study),
                imageVector = Icons.Default.ExitToApp,
                contentDescription = getStringResource(id = R.string.info_leave_study_desc),
                onClick = {
                    navController.navigate(NavigationScreen.LEAVE_STUDY.routeWithParameters())
                }
            )
        }

        item {
            viewModel.model.value?.let {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        if (it.study.participantId != null || it.study.participantAlias != null) {
                            Spacer(modifier = Modifier.height(25.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if(it.study.participantId != null ) {
                                SmallTitle(
                                    text = getStringResource(id = R.string.info_participant_credentials),
                                    color = MoreColors.Secondary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                SmallTitle(
                                    text = it.study.participantId.toString(),
                                    color = MoreColors.Secondary
                                )
                                SmallTitle(text = ": ", color = MoreColors.Secondary)
                            }
                            if(it.study.participantAlias != null) {
                                BasicText(text = it.study.participantAlias.toString(), color = MoreColors.Secondary)
                            }
                        }
                        if (it.study.participantId != null || it.study.participantAlias != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        if(it.study.contactPerson != null || it.study.contactEmail != null || it.study.contactPhoneNumber != null) {
                            SmallTitle(
                                text = getStringResource(id = R.string.info_contact_data),
                                color = MoreColors.PrimaryDark,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp
                            )
                        }


                        if(it.study.contactInstitute != null) {
                            SmallTitle(
                                text = it.study.contactInstitute as String,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        if(it.study.contactPerson != null) {
                            SmallTitle(
                                text = it.study.contactPerson as String,
                                modifier = Modifier.fillMaxWidth(),
                                color = MoreColors.Secondary,
                                textAlign = TextAlign.Center
                            )
                        }


                        if (it.study.contactEmail != null) {
                            BasicText(
                                text = it.study.contactEmail as String,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                color = MoreColors.Secondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (it.study.contactPhoneNumber != null)
                            BasicText(
                                text = it.study.contactPhoneNumber as String,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                color = MoreColors.Secondary,
                                textAlign = TextAlign.Center
                            )

                        if(it.study.contactPerson != null || it.study.contactEmail != null || it.study.contactPhoneNumber != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()

                            BasicText(
                                text = getStringResource(id = R.string.info_disclaimer),
                                color = MoreColors.Secondary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            )
                        }

                    }
                }
            }


        }
        item {
            AppVersion()
        }
    }
}
