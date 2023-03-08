package io.redlink.more.more_app_mutliplatform.android.activities.observations.questionnaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground

class QuestionnaireActivity: ComponentActivity() {
    private val questionnaireViewModel = QuestionnaireViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestionnaireView(questionnaireViewModel)
        }
    }
}

@Composable
fun QuestionnaireView(model: QuestionnaireViewModel){
    MoreBackground(rightCornerContent = {
        Icon(Icons.Default.Clear, contentDescription = getString(R.string.more_observation_close))
    }) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth(0.8f)
        ) {
            QuestionnaireHeader(model)
            QuestionnaireQuestionAnswer(model = model)
        }
        QuestionnaireButtons(model = model)
    }
}