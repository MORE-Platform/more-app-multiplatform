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
package io.redlink.more.app.android.ui.theme


import androidx.compose.foundation.BorderStroke
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MoreColors {
    companion object {

        val PrimaryDark = Color(0xff476580) // -> MainDark
        val Primary = Color(0xFF6e8fac) // -> Main, Main Darker
        val PrimaryMedium = Color(0xffa6bcd0) //-> MainLighter
        val PrimaryLight200 = Color(0xFFdce2e7)
        val PrimaryLight = Color(0xFFf4f9fd) // -> MainBackground

        val Secondary = Color(0xff707070) // -> MainCancel
        val SecondaryMedium = Color(0xffbbc3c7) // -> Inactivity
        val SecondaryLight = Color(0xfff4f4f4) // -> InactiveBackground

        val TextDefault = Secondary // -> TextColor
        val TextInactive = SecondaryMedium // -> InactiveText

        val Important = Color(0xffa37070)
        val ImportantMedium = Color(0xffd6b7b7) // -> ImportantBright
        val ImportantLight = Color(0xfff5e7e7)

        val Approved = Color(0xff6f9a80) // -> DoneDark, Done
        val ApprovedMedium = Color(0xffb0d8bf)
        val ApprovedLight = Color(0xffe9f4ed)

        val White = Color (0xffFFFFFF)


        // Special Design ElementsA
        val Divider = PrimaryLight200 // Devider Line between elements
        val BackgroundOverlay = SecondaryMedium

        // border definitions
        fun borderPrimary(active: Boolean) = BorderStroke(1.dp, if (active) Primary else PrimaryLight200)
        fun borderImportant() = BorderStroke(1.dp, Important)
        fun borderApproved() = BorderStroke(1.dp, Approved)
        fun borderDefault() = BorderStroke(1.dp, Secondary)
    }
}

@Composable
fun ButtonDefaults.morePrimary() = buttonColors(
    contentColor = MoreColors.PrimaryLight,
    backgroundColor = MoreColors.Primary,
    disabledContentColor = MoreColors.PrimaryMedium,
    disabledBackgroundColor = MoreColors.PrimaryLight200
)

@Composable
fun ButtonDefaults.moreSecondary() = buttonColors(
    contentColor = MoreColors.SecondaryLight,
    backgroundColor = MoreColors.Secondary,
    disabledContentColor = MoreColors.SecondaryLight,
    disabledBackgroundColor = MoreColors.SecondaryMedium
)

@Composable
fun ButtonDefaults.moreSecondary2() = buttonColors(
    contentColor = MoreColors.Secondary,
    backgroundColor = MoreColors.PrimaryLight,
    disabledContentColor = MoreColors.SecondaryMedium,
    disabledBackgroundColor = MoreColors.SecondaryLight
)

@Composable
fun ButtonDefaults.moreImportant() = buttonColors(
    contentColor = MoreColors.ImportantLight,
    backgroundColor = MoreColors.Important,
    disabledContentColor = MoreColors.ImportantLight,
    disabledBackgroundColor = MoreColors.ImportantMedium
)

@Composable
fun ButtonDefaults.moreApproved() = buttonColors(
    contentColor = MoreColors.ApprovedLight,
    backgroundColor = MoreColors.Approved,
    disabledContentColor = MoreColors.ApprovedLight,
    disabledBackgroundColor = MoreColors.ApprovedMedium,

)

