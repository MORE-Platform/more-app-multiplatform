# Keep SLF4J classes
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Keep Google Play Services Location classes
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Polar SDK classes if you're using it
-keep class com.polar.** { *; }
-dontwarn com.polar.**

# Keep RxJava classes
-keep class io.reactivex.** { *; }
-dontwarn io.reactivex.**

# Keep Realm classes
-keep class io.realm.** { *; }
-dontwarn io.realm.**

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Napier logging classes
-keep class io.github.aakira.napier.** { *; }
-dontwarn io.github.aakira.napier.**

# General rule for missing classes
-dontwarn java.lang.invoke.**
-dontwarn org.slf4j.impl.StaticLoggerBinder


# Joda Time
-keep class org.joda.time.** { *; }
-keep class org.joda.convert.** { *; }
-dontwarn org.joda.time.**
-dontwarn org.joda.convert.**

# Keep Joda Time annotations
-keepattributes *Annotation*
-keep @org.joda.convert.FromString class *
-keep @org.joda.convert.ToString class *

# Keep methods annotated with Joda Convert annotations
-keepclassmembers class * {
    @org.joda.convert.FromString *;
    @org.joda.convert.ToString *;
}

# Keep shared KMM module classes (needed for Koin reflection-based DI)
-keep class io.redlink.more.** { *; }
-keepnames class io.redlink.more.** { *; }

# Keep Koin DI classes
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Ktor HTTP client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Room database and entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# Keep Gson serialization
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}