package io.redlink.more.app.android.observations.HealthKit.DataFormatter

import androidx.health.connect.client.records.ExerciseSessionRecord
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExerciseSessionData(
    record: ExerciseSessionRecord,
    val distance: Double,
    val calories: Double
) {

    val start: String
    val end: String
    val workoutType: String

    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm")
            .withLocale(Locale.US)
            .withZone(ZoneOffset.UTC)

        start = formatter.format(record.startTime)
        end = formatter.format(record.endTime)

        // Map exercise type using the enum
        workoutType = ExerciseType.fromId(record.exerciseType).displayName
    }
}

enum class ExerciseType(val typeId: Int, val displayName: String) {
    OTHER_WORKOUT(0, "Other Workout"),
    BADMINTON(2, "Badminton"),
    BASEBALL(4, "Baseball"),
    BASKETBALL(5, "Basketball"),
    BIKING(8, "Biking"),
    BIKING_STATIONARY(9, "Biking (Stationary)"),
    BOOT_CAMP(10, "Boot Camp"),
    BOXING(11, "Boxing"),
    CALISTHENICS(13, "Calisthenics"),
    CRICKET(14, "Cricket"),
    DANCING(16, "Dancing"),
    ELLIPTICAL(25, "Elliptical"),
    EXERCISE_CLASS(26, "Exercise Class"),
    FENCING(27, "Fencing"),
    FOOTBALL_AMERICAN(28, "Football (American)"),
    FOOTBALL_AUSTRALIAN(29, "Football (Australian)"),
    FRISBEE_DISC(31, "Frisbee/Disc"),
    GOLF(32, "Golf"),
    GUIDED_BREATHING(33, "Guided Breathing"),
    GYMNASTICS(34, "Gymnastics"),
    HANDBALL(35, "Handball"),
    HIGH_INTENSITY_INTERVAL_TRAINING(36, "HIIT"),
    HIKING(37, "Hiking"),
    ICE_HOCKEY(38, "Ice Hockey"),
    ICE_SKATING(39, "Ice Skating"),
    MARTIAL_ARTS(44, "Martial Arts"),
    PADDLING(46, "Paddling"),
    PARAGLIDING(47, "Paragliding"),
    PILATES(48, "Pilates"),
    RACQUETBALL(50, "Racquetball"),
    ROCK_CLIMBING(51, "Rock Climbing"),
    ROLLER_HOCKEY(52, "Roller Hockey"),
    ROWING(53, "Rowing"),
    ROWING_MACHINE(54, "Rowing Machine"),
    RUGBY(55, "Rugby"),
    RUNNING(56, "Running"),
    RUNNING_TREADMILL(57, "Running (Treadmill)"),
    SAILING(58, "Sailing"),
    SCUBA_DIVING(59, "Scuba Diving"),
    SKATING(60, "Skating"),
    SKIING(61, "Skiing"),
    SNOWBOARDING(62, "Snowboarding"),
    SNOWSHOEING(63, "Snowshoeing"),
    SOCCER(64, "Soccer"),
    SOFTBALL(65, "Softball"),
    SQUASH(66, "Squash"),
    STAIR_CLIMBING(68, "Stair Climbing"),
    STAIR_CLIMBING_MACHINE(69, "Stair Climbing (Machine)"),
    STRENGTH_TRAINING(70, "Strength Training"),
    STRETCHING(71, "Stretching"),
    SURFING(72, "Surfing"),
    SWIMMING_OPEN_WATER(73, "Swimming (Open Water)"),
    SWIMMING_POOL(74, "Swimming (Pool)"),
    TABLE_TENNIS(75, "Table Tennis"),
    TENNIS(76, "Tennis"),
    VOLLEYBALL(78, "Volleyball"),
    WALKING(79, "Walking"),
    WATER_POLO(80, "Water Polo"),
    WEIGHTLIFTING(81, "Weightlifting"),
    WHEELCHAIR(82, "Wheelchair"),
    YOGA(83, "Yoga");

    companion object {
        fun fromId(id: Int): ExerciseType {
            return values().find { it.typeId == id } ?: OTHER_WORKOUT
        }
    }
}
