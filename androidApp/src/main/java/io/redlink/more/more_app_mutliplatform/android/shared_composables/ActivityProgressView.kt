package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.layout.*
import io.redlink.more.more_app_mutliplatform.android.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun ActivityProgressView(modifier: Modifier = Modifier, finishedTasks: Int, totalTasks: Int, headline: String = getStringResource(id = R.string.more_main_completed_tasks)){
    val percent = if(totalTasks > 0) finishedTasks.toFloat() / totalTasks.toFloat() else 0f
    Column(
        modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(
                text = headline,
                color = Color.Gray,
                maxLines = 1,
                modifier = Modifier.weight(0.8f)
            )
            Text(
                text = "${(percent * 100).toInt()}%",
                color = MoreColors.Main,
                maxLines = 1,
            )
        }

        LinearProgressIndicator(
            progress = percent,
            color = MoreColors.Main,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(2.dp)
                .clip(RoundedCornerShape(20))
        )
    }
}