package io.redlink.more.app.android.observations.Polar

import android.content.Context
import com.polar.sdk.api.model.PolarFirstTimeUseConfig
import io.redlink.more.app.android.MoreApplication
import java.util.Calendar

data class Polar360UserProfile(
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
    val birthDate: java.util.Date
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - age)
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            return cal.time
        }

    companion object {
        private const val PREFS_NAME = "polar360_profile"
        private const val KEY_GENDER = "gender"
        private const val KEY_AGE = "age"
        private const val KEY_HEIGHT = "heightCm"
        private const val KEY_WEIGHT = "weightKg"

        private fun prefs(context: Context) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun load(): Polar360UserProfile? {
            val ctx = MoreApplication.appContext ?: return null
            val prefs = prefs(ctx)
            val genderRaw = prefs.getString(KEY_GENDER, null) ?: return null
            val gender = runCatching { Gender.valueOf(genderRaw) }.getOrNull() ?: return null
            if (!prefs.contains(KEY_AGE) || !prefs.contains(KEY_HEIGHT) || !prefs.contains(KEY_WEIGHT)) return null
            return Polar360UserProfile(
                gender = gender,
                age = prefs.getInt(KEY_AGE, 0),
                heightCm = prefs.getInt(KEY_HEIGHT, 0),
                weightKg = prefs.getInt(KEY_WEIGHT, 0)
            )
        }

        fun save(profile: Polar360UserProfile) {
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
