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