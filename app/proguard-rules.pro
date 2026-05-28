# Default rules for ProGuard
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
# Keep Room entities
-keep class com.levelone.smoothlivetranscribe.data.db.** { *; }
