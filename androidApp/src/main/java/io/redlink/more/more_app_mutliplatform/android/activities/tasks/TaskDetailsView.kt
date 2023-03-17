package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redlink.more.more_app_mutliplatform.Greeting
import io.redlink.more.more_app_mutliplatform.android.activities.main.GreetingView
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MorePlatformTheme

class ContentActivity: ComponentActivity() {
    private val viewModel = TaskDetailsViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoreBackground {
                TaskDetailsView(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TaskDetailsView(viewModel: TaskDetailsViewModel) {
    Text(text = viewModel.getDataPointCount("").toString())
}

@Preview
@Composable
fun DefaultPreview() {
    MorePlatformTheme {
        TaskDetailsView(viewModel = TaskDetailsViewModel())
    }
}