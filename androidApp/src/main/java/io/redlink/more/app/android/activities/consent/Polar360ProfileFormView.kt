package io.redlink.more.app.android.activities.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.observations.Polar.Polar360UserProfile
import io.redlink.more.app.android.theme.MoreColors

@Composable
fun Polar360ProfileFormView(onComplete: () -> Unit) {
    val genders = Polar360UserProfile.Gender.entries
    var selectedGenderIndex by remember { mutableIntStateOf(0) }
    var ageText by remember { mutableStateOf("30") }
    var heightText by remember { mutableStateOf("170") }
    var weightText by remember { mutableStateOf("70") }

    val age = ageText.toIntOrNull()
    val heightCm = heightText.toIntOrNull()
    val weightKg = weightText.toIntOrNull()

    val isValid = age != null && age in 1..120
            && heightCm != null && heightCm in 50..250
            && weightKg != null && weightKg in 20..300

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Polar 360 Setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MoreColors.Primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Please provide your physical information for accurate sensor calibration.",
                fontSize = 14.sp,
                color = MoreColors.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))

            Text("Gender", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                genders.forEachIndexed { index, gender ->
                    val isSelected = selectedGenderIndex == index
                    val genderColor = if (gender == Polar360UserProfile.Gender.FEMALE)
                        Color(0xFFD63384) else Color(0xFF1976D2)
                    val shape = if (index == 0)
                        RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                    else
                        RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                    Button(
                        onClick = { selectedGenderIndex = index },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = shape,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isSelected) genderColor else Color(0xFFEEEEEE),
                            contentColor = if (isSelected) Color.White else Color(0xFF444444)
                        ),
                        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                    ) {
                        Text(gender.displayName, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                label = { Text("Age (years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = age == null || age !in 1..120,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MoreColors.Primary,
                    focusedLabelColor = MoreColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it.filter { c -> c.isDigit() } },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = heightCm == null || heightCm !in 50..250,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MoreColors.Primary,
                    focusedLabelColor = MoreColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it.filter { c -> c.isDigit() } },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = weightKg == null || weightKg !in 20..300,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MoreColors.Primary,
                    focusedLabelColor = MoreColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val profile = Polar360UserProfile(
                    gender = genders[selectedGenderIndex],
                    age = age!!,
                    heightCm = heightCm!!,
                    weightKg = weightKg!!
                )
                Polar360UserProfile.save(profile)
                onComplete()
            },
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MoreColors.Primary,
                contentColor = MoreColors.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Continue")
        }
    }
}
