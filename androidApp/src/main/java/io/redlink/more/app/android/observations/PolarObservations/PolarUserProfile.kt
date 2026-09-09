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
package io.redlink.more.app.android.observations.PolarObservations

import android.content.Context
import com.polar.sdk.api.model.PolarFirstTimeUseConfig
import io.redlink.more.app.android.MoreApplication
import java.time.LocalDate

/**
 * Demographics collected once at consent time and fed to the Polar device's first-time-use setup.
 * The device derives HR zones and calorie estimates from them, so a study participant's own values
 * have to reach the device rather than a placeholder.
 *
 * Stored in SharedPreferences: this outlives a single study run and is needed before any repository
 * is available during device setup.
 */
data class PolarUserProfile(
    val gender: Gender,
    val age: Int,
    val heightCm: Int,
    val weightKg: Int
) {
    enum class Gender(val displayName: String) {
        FEMALE("Female"),
        MALE("Male");

        val polarGender: PolarFirstTimeUseConfig.Gender
            get() = when (this) {
                FEMALE -> PolarFirstTimeUseConfig.Gender.FEMALE
                MALE -> PolarFirstTimeUseConfig.Gender.MALE
            }
    }

    /** Approximate birth date derived from age (Jan 1 of the birth year). */
    val birthDate: LocalDate
        get() = LocalDate.of(LocalDate.now().year - age, 1, 1)

    companion object {
        private const val PREFS_NAME = "polar_profile"
        private const val KEY_GENDER = "gender"
        private const val KEY_AGE = "age"
        private const val KEY_HEIGHT = "heightCm"
        private const val KEY_WEIGHT = "weightKg"

        private fun prefs(context: Context) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun load(): PolarUserProfile? {
            val ctx = MoreApplication.appContext ?: return null
            val prefs = prefs(ctx)
            val genderRaw = prefs.getString(KEY_GENDER, null) ?: return null
            val gender = runCatching { Gender.valueOf(genderRaw) }.getOrNull() ?: return null
            if (!prefs.contains(KEY_AGE) || !prefs.contains(KEY_HEIGHT) || !prefs.contains(KEY_WEIGHT)) return null
            return PolarUserProfile(
                gender = gender,
                age = prefs.getInt(KEY_AGE, 0),
                heightCm = prefs.getInt(KEY_HEIGHT, 0),
                weightKg = prefs.getInt(KEY_WEIGHT, 0)
            )
        }

        fun save(profile: PolarUserProfile) {
            val ctx = MoreApplication.appContext ?: return
            prefs(ctx).edit()
                .putString(KEY_GENDER, profile.gender.name)
                .putInt(KEY_AGE, profile.age)
                .putInt(KEY_HEIGHT, profile.heightCm)
                .putInt(KEY_WEIGHT, profile.weightKg)
                .apply()
        }
    }
}
