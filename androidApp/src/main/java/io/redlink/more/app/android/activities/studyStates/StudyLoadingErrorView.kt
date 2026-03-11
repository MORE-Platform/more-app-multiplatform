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

package io.redlink.more.app.android.activities.studyStates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.subcomponents.ExitButton
import io.redlink.more.app.android.activities.subcomponents.ReloadButton
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.Title

@Composable
fun StudyLoadingErrorView() {
    val context = LocalContext.current
    MoreBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        id = R.drawable.warning_exclamation,
                        contentDescription = "More Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .aspectRatio(1.5f)
                    )
                }
                Title(
                    text = getStringResource(id = R.string.study_loading_error_title),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MediumTitle(
                        getStringResource(R.string.study_loading_error_message),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ReloadButton()
                ExitButton()
            }
        }
    }
}

@Preview
@Composable
fun StudyLoadingErrorPreview() {
    StudyLoadingErrorView()
}