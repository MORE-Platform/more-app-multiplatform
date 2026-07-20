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
package io.redlink.more.app.android.activities.setting

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.redlink.more.SharedRes
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.OnAppearDisappear
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.Accordion
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.theme.MoreColors

@Composable
fun SettingsView() {
    val context = LocalContext.current
    val model = remember { SettingsViewModel() }
    val needsTracking by model.coreViewModel.needsTracking.collectAsState()
    val trackingApproval by model.coreViewModel.allowTracking.collectAsState()
    OnAppearDisappear({ model.coreViewModel.viewOpened() }, { model.coreViewModel.viewClosed() }) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                item {
                    SmallTextButton(
                        text = getStringResource(id = R.string.proceed_to_settings_button),
                        onClick = { model.coreViewModel.openSettings() }
                    )
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    if (needsTracking) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MoreColors.TextDefault, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    StringDesc.Resource(SharedRes.strings.app_tracking_dialog_title)
                                        .toString(context)
                                )

                                Switch(
                                    checked = trackingApproval,
                                    onCheckedChange = { isChecked ->
                                        model.coreViewModel.setTrackingPermission(isChecked)
                                    }
                                )
                            }
                            Divider()
                            Text(
                                StringDesc.Resource(SharedRes.strings.app_tracking_dialog_message)
                                    .toString(context)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    BasicText(
                        text = getStringResource(id = R.string.more_settings_permission_information),
                        color = MoreColors.TextDefault,
                    )

                    Spacer(Modifier.height(24.dp))
                }

                items(model.permissionModel.value?.consentInfo ?: emptyList()) { consentInfo ->
                    Accordion(
                        title = consentInfo.title,
                        description = consentInfo.info,
                        hasCheck = true,
                        hasSmallTitle = true,
                        hasPreview = false
                    )
                }
            }
        }
    }
}