package io.redlink.more.app.android.activities.healthPage

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.rememberCoroutineScope
import androidx.health.connect.client.time.TimeRangeFilter
//import io.redlink.more.app.android.datastreaming.workers.RawDataUploadWorker
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Composable
fun HealthView(navController: NavController,viewModel: HealthViewModel,healthConnectManager: HealthConnectManager) {


    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.HEALTH_DATA.routeWithParameters())
    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
    )
    val  permissionsLauncher = rememberLauncherForActivityResult(healthConnectManager.requestPermissionsActivityContract()) {
    }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,


    ) {
        Text(
            text = "Health Permissions",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).align(Alignment.CenterHorizontally)

        )
        Text(
            text = "Please allow us   health data access, we wont share your data with anybody  "
            ,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Please allow us to get data from your activities",
                modifier = Modifier.weight(1f)
            )


        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Button(
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.Black,
                    containerColor = Color.Green,
                    disabledContentColor = Color.Gray,
                    disabledContainerColor = Color.LightGray
                ),
                onClick = {
                    val connectIntent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                    activity.startActivity(connectIntent)

                }
            ) {
                Text(text = "Connect Health Kit")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = "Please allow us to read data in the background so we can send real time adaptive intervetions")

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Button(colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                containerColor = Color.Green,
                disabledContentColor = Color.Gray,
                disabledContainerColor = Color.LightGray
            ),
                onClick = {
                    val backgroundReadPermissions = setOf(PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)
                    permissionsLauncher.launch(backgroundReadPermissions)
                }
            ) {
                Text(text = "Allow Background data read")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
                ){
            Button(colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color.Blue,
                disabledContentColor = Color.Gray,
                disabledContainerColor = Color.LightGray
            ),
                onClick = {
                    scope.launch {
                        val end = ZonedDateTime.now().withNano(0)
                        val start = end.minusDays(1)

                        val stepsRecords = healthConnectManager.readData<StepsRecord>(
                            TimeRangeFilter.between(start.toInstant(), end.toInstant())
                        )

                        val totalSteps = stepsRecords.sumOf { it.count }
                        println("ReadData"+ "Total steps in last 24h: $totalSteps")
                    }
                },
            ) {
                Text(text = "Read Data")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Button(colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color.Blue,
                disabledContentColor = Color.Gray,
                disabledContainerColor = Color.LightGray
            ),
                onClick = {
                    scope.launch {
                       healthConnectManager.writeSteps()
                    }
                },
            ) {
                Text(text = "Write Data")
            }
        }

    }
}
