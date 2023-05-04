import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.tasks.ObservationDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.toDate
import io.redlink.more.app.android.shared_composables.*
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun ObservationDetailsView(
    viewModel: ObservationDetailsViewModel,
    navController: NavController
) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.OBSERVATION_DETAILS.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                HeaderTitle(
                    title = viewModel.observationDetailsModel.value.observationTitle,
                    modifier = Modifier
                        .weight(0.65f)
                        .padding(vertical = 11.dp)
                )
            }
            BasicText(
                text = viewModel.observationDetailsModel.value.observationType,
                color = MoreColors.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )

            TimeframeDays(
                viewModel.observationDetailsModel.value.start.toDate(),
                viewModel.observationDetailsModel.value.end.toDate(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            TimeframeHours(
                viewModel.observationDetailsModel.value.start.toDate(),
                viewModel.observationDetailsModel.value.end.toDate(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Accordion(
                title = getStringResource(id = R.string.participant_information),
                description = viewModel.observationDetailsModel.value.participantInformation,
                hasCheck = false,
                hasPreview = false,
                isOpen = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}